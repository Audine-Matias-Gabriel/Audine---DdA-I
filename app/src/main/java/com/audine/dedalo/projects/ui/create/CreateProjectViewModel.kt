package com.audine.dedalo.projects.ui.create

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.BuildConfig
import com.audine.dedalo.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.audine.dedalo.core.data.remote.LocationiqResponse
import com.audine.dedalo.core.data.remote.LocationiqService
import com.audine.dedalo.projects.data.ImageData
import com.audine.dedalo.projects.data.ImageType
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class SelectedImage(
    val uri: Uri,
    val type: ImageType = ImageType.PHOTO
)

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val locationiqService: LocationiqService,
    private val storage: FirebaseStorage,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion: StateFlow<String> = _direccion.asStateFlow()

    private val _fos = MutableStateFlow("")
    val fos: StateFlow<String> = _fos.asStateFlow()

    private val _fot = MutableStateFlow("")
    val fot: StateFlow<String> = _fot.asStateFlow()

    private var latitud = 0.0
    private var longitud = 0.0

    private val _selectedImages = MutableStateFlow<List<SelectedImage>>(emptyList())
    val selectedImages: StateFlow<List<SelectedImage>> = _selectedImages.asStateFlow()

    private val _suggestions = MutableStateFlow<List<LocationiqResponse>>(emptyList())
    val suggestions: StateFlow<List<LocationiqResponse>> = _suggestions.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        viewModelScope.launch {
            _direccion
                .debounce(2500)
                .filter { it.length >= 3 }
                .mapLatest { query ->
                    try {
                        locationiqService.autocomplete(BuildConfig.LOCATIONIQ_API_KEY, query)
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
                .collect { _suggestions.value = it }
        }
    }

    fun onNombreChange(value: String) { _nombre.value = value }
    fun onDireccionChange(value: String) {
        _direccion.value = value
        _suggestions.value = emptyList()
    }
    fun onFosChange(value: String) { _fos.value = value }
    fun onFotChange(value: String) { _fot.value = value }

    fun onSuggestionSelected(suggestion: LocationiqResponse) {
        _direccion.value = suggestion.displayName
        latitud = suggestion.lat.toDoubleOrNull() ?: 0.0
        longitud = suggestion.lon.toDoubleOrNull() ?: 0.0
        _suggestions.value = emptyList()
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    fun addImageUri(uri: Uri) {
        _selectedImages.value = _selectedImages.value + SelectedImage(uri = uri)
    }

    fun removeImageUri(uri: Uri) {
        _selectedImages.value = _selectedImages.value.filter { it.uri != uri }
    }

    fun setImageType(uri: Uri, type: ImageType) {
        _selectedImages.value = _selectedImages.value.map {
            if (it.uri == uri) it.copy(type = type) else it
        }
    }

    fun createProject() {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            try {
                val userId = authRepository.currentUserId
                    ?: throw IllegalStateException("Usuario no autenticado")
                val images = uploadImages()
                val project = Project(
                    nombre = _nombre.value.trim(),
                    direccion = _direccion.value.trim(),
                    latitud = latitud,
                    longitud = longitud,
                    fos = _fos.value.trim(),
                    fot = _fot.value.trim(),
                    images = images
                )
                repository.createProject(project, userId)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Error al guardar la obra"
            } finally {
                _isSaving.value = false
            }
        }
    }

    private suspend fun uploadImages(): List<ImageData> {
        if (_selectedImages.value.isEmpty()) return emptyList()
        val images = mutableListOf<ImageData>()
        for (selected in _selectedImages.value) {
            val ref = storage.reference.child("projects/${UUID.randomUUID()}.jpg")
            ref.putFile(selected.uri).await()
            val downloadUrl = ref.downloadUrl.await()
            images.add(ImageData(url = downloadUrl.toString(), type = selected.type))
        }
        return images
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
        suspendCoroutine { continuation ->
            addOnSuccessListener { result ->
                continuation.resume(result)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
}
