package com.audine.dedalo.projects.ui.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.core.data.remote.SupabaseStorageHelper
import com.audine.dedalo.projects.data.ImageData
import com.audine.dedalo.projects.data.ImageType
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import com.audine.dedalo.projects.data.Stage
import com.audine.dedalo.projects.data.StageStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface ProjectDetailUiState {
    data object Loading : ProjectDetailUiState
    data class Success(val project: Project) : ProjectDetailUiState
    data class Error(val message: String) : ProjectDetailUiState
}

sealed interface ImageDestination {
    data object Project : ImageDestination
    data class Stage(val stageId: String, val stageName: String) : ImageDestination
}

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val supabaseStorageHelper: SupabaseStorageHelper,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val obraId: String = savedStateHandle.get<String>("obraId") ?: ""

    private val _uiState = MutableStateFlow<ProjectDetailUiState>(ProjectDetailUiState.Loading)
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    private val _pendingImageUri = MutableStateFlow<Uri?>(null)
    val pendingImageUri: StateFlow<Uri?> = _pendingImageUri.asStateFlow()

    private val _showAddStageDialog = MutableStateFlow(false)
    val showAddStageDialog: StateFlow<Boolean> = _showAddStageDialog.asStateFlow()

    private val _stageStatusDialog = MutableStateFlow<Stage?>(null)
    val stageStatusDialog: StateFlow<Stage?> = _stageStatusDialog.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProjectWithStages(obraId)
                .map<Project?, ProjectDetailUiState> { project ->
                    if (project != null) ProjectDetailUiState.Success(project)
                    else ProjectDetailUiState.Error("Obra no encontrada")
                }
                .catch { e -> emit(ProjectDetailUiState.Error(e.message ?: "Error al cargar obra")) }
                .collect { state -> _uiState.value = state }
        }
    }

    fun addStage(nombre: String) {
        viewModelScope.launch {
            try {
                val project = (_uiState.value as? ProjectDetailUiState.Success)?.project ?: return@launch
                val userId = authRepository.currentUserId ?: return@launch
                val nextPos = (project.stages.maxOfOrNull { it.posicion } ?: 0) + 1
                val stage = Stage(nombre = nombre, posicion = nextPos)
                repository.createStage(obraId, stage, userId)
                _showAddStageDialog.value = false
            } catch (_: Exception) { }
        }
    }

    fun updateStageStatus(stage: Stage, newStatus: StageStatus) {
        viewModelScope.launch {
            try {
                repository.updateStage(obraId, stage.copy(estado = newStatus))
                _stageStatusDialog.value = null
            } catch (_: Exception) { }
        }
    }

    fun onImagePicked(uri: Uri) {
        _pendingImageUri.value = uri
    }

    fun cancelImageDestination() {
        _pendingImageUri.value = null
    }

    fun confirmAddImage(destination: ImageDestination) {
        val uri = _pendingImageUri.value ?: return
        _pendingImageUri.value = null
        viewModelScope.launch {
            try {
                val downloadUrl = uploadImage(uri)
                val imageData = ImageData(url = downloadUrl, type = ImageType.PHOTO)
                when (destination) {
                    is ImageDestination.Project -> addImageToProject(imageData)
                    is ImageDestination.Stage -> addImageToStage(destination.stageId, imageData)
                }
            } catch (_: Exception) { }
        }
    }

    fun showAddStageDialog() {
        _showAddStageDialog.value = true
    }

    fun hideAddStageDialog() {
        _showAddStageDialog.value = false
    }

    fun showStageStatusDialog(stage: Stage) {
        _stageStatusDialog.value = stage
    }

    fun hideStageStatusDialog() {
        _stageStatusDialog.value = null
    }

    private suspend fun addImageToProject(imageData: ImageData) {
        val project = (_uiState.value as? ProjectDetailUiState.Success)?.project ?: return
        repository.updateProject(
            project.copy(images = project.images + imageData)
        )
    }

    private suspend fun addImageToStage(stageId: String, imageData: ImageData) {
        val project = (_uiState.value as? ProjectDetailUiState.Success)?.project ?: return
        val stage = project.stages.find { it.id == stageId } ?: return
        repository.updateStage(
            obraId, stage.copy(images = stage.images + imageData)
        )
    }

    private suspend fun uploadImage(uri: Uri): String {
        val path = "projects/$obraId/${UUID.randomUUID()}.jpg"
        return supabaseStorageHelper.uploadImage(uri, path)
    }
}
