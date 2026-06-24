package com.audine.dedalo.projects.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audine.dedalo.auth.data.AuthRepository
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProjectsUiState {
    data object Loading : ProjectsUiState
    data class Success(val projects: List<Project>) : ProjectsUiState
    data class Error(val message: String) : ProjectsUiState
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun syncProjects() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            _isSyncing.value = true
            try {
                repository.syncMyProjectsToFirebase(userId)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    init {
        viewModelScope.launch {
            authRepository.currentUser.flatMapLatest { user ->
                if (user != null) {
                    combine(
                        repository.observeProjectsByUser(user.id),
                        _searchQuery
                    ) { projects, query -> toUiState(projects, query) }
                        .catch { e -> emit(ProjectsUiState.Error(e.message ?: "Error al cargar obras")) }
                } else {
                    flowOf(ProjectsUiState.Error("Usuario no autenticado"))
                }
            }.collect { state -> _uiState.value = state }
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
