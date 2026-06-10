package com.audine.dedalo.projects.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProjectsUiState {
    data object Loading : ProjectsUiState
    data class Success(val projects: List<Project>) : ProjectsUiState
    data class Error(val message: String) : ProjectsUiState
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            combine(
                repository.observeProjects(),
                _searchQuery
            ) { projects, query ->
                toUiState(projects, query)
            }
                .catch { e -> emit(ProjectsUiState.Error(e.message ?: "Error al cargar obras")) }
                .collect { state -> _uiState.value = state }
        }
    }

    private fun toUiState(projects: List<Project>, query: String): ProjectsUiState {
        return if (query.isBlank()) {
            ProjectsUiState.Success(projects)
        } else {
            ProjectsUiState.Success(
                projects.filter {
                    it.nombre.contains(query, ignoreCase = true) ||
                    it.direccion.contains(query, ignoreCase = true)
                }
            )
        }
    }
}
