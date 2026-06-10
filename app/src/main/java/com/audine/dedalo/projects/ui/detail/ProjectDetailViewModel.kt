package com.audine.dedalo.projects.ui.detail

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProjectDetailUiState {
    data object Loading : ProjectDetailUiState
    data class Success(val project: Project) : ProjectDetailUiState
    data class Error(val message: String) : ProjectDetailUiState
}

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val repository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val obraId: String = savedStateHandle.get<String>("obraId") ?: ""

    private val _uiState = MutableStateFlow<ProjectDetailUiState>(ProjectDetailUiState.Loading)
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

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
}
