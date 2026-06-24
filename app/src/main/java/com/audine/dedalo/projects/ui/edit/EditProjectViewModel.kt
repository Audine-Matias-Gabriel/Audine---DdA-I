package com.audine.dedalo.projects.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProjectViewModel @Inject constructor(
    private val repository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val obraId: String = savedStateHandle.get<String>("obraId") ?: ""

    private var originalProject: Project? = null

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion: StateFlow<String> = _direccion.asStateFlow()

    private val _fos = MutableStateFlow("")
    val fos: StateFlow<String> = _fos.asStateFlow()

    private val _fot = MutableStateFlow("")
    val fot: StateFlow<String> = _fot.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        viewModelScope.launch {
            val project = repository.observeProjectWithStages(obraId)
                .map { it }
                .catch { _saveError.value = it.message }
                .first()
            if (project != null) {
                originalProject = project
                _nombre.value = project.nombre
                _direccion.value = project.direccion
                _fos.value = project.fos
                _fot.value = project.fot
            }
        }
    }

    fun onNombreChange(value: String) { _nombre.value = value }
    fun onDireccionChange(value: String) { _direccion.value = value }
    fun onFosChange(value: String) { _fos.value = value }
    fun onFotChange(value: String) { _fot.value = value }

    fun save() {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            try {
                val original = originalProject
                repository.updateProject(
                    Project(
                        id = obraId,
                        userId = original?.userId ?: "",
                        nombre = _nombre.value.trim(),
                        direccion = _direccion.value.trim(),
                        latitud = original?.latitud ?: 0.0,
                        longitud = original?.longitud ?: 0.0,
                        fos = _fos.value.trim(),
                        fot = _fot.value.trim(),
                        images = original?.images ?: emptyList(),
                        createdAt = original?.createdAt ?: System.currentTimeMillis()
                    )
                )
                _saveSuccess.value = true
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Error al guardar"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
