package com.audine.dedalo.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.audine.dedalo.chat.ui.ChatScreen
import com.audine.dedalo.chat.ui.ChatViewModel
import com.audine.dedalo.core.navigation.Routes
import com.audine.dedalo.core.ui.theme.DedaloTheme
import com.audine.dedalo.profile.ui.ProfileScreen
import com.audine.dedalo.profile.ui.ProfileViewModel
import com.audine.dedalo.projects.ui.list.ProjectsListScreen
import com.audine.dedalo.projects.ui.list.ProjectsViewModel

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    onNavigateToProjectDetail: (String) -> Unit,
    onNavigateToImageViewer: (String) -> Unit,
    onCreateProject: () -> Unit,
    onSignOut: () -> Unit
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem(Routes.PROJECTS, "Obras", Icons.Default.Build),
        BottomNavItem(Routes.CHAT, "Chat", Icons.Default.Forum),
        BottomNavItem(Routes.PROFILE, "Perfil", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            innerNavController.navigate(item.route) {
                                popUpTo(innerNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Routes.PROJECTS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.PROJECTS) {
                val viewModel: ProjectsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                ProjectsListScreen(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onCreateProject = onCreateProject,
                    onNavigateToProjectDetail = onNavigateToProjectDetail,
                    isSyncing = isSyncing,
                    onSync = viewModel::syncProjects
                )
            }
            composable(Routes.CHAT) {
                val viewModel: ChatViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ChatScreen(
                    uiState = uiState,
                    onSendMessage = viewModel::sendMessage,
                    onClearHistory = viewModel::clearHistory,
                    onClearError = viewModel::clearError
                )
            }
            composable(Routes.PROFILE) {
                val viewModel: ProfileViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                ProfileScreen(
                    uiState = uiState,
                    onUploadGalleryImage = viewModel::uploadGalleryImage,
                    onUploadAvatar = viewModel::uploadAvatar,
                    onClearUploadError = viewModel::clearUploadError,
                    onSignOut = onSignOut,
                    onNavigateToImageViewer = onNavigateToImageViewer,
                    isSyncing = isSyncing,
                    onSyncProjects = viewModel::syncProjects
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, device = "id:pixel_6")
@Composable
private fun MainScreenPreview() {
    DedaloTheme {
        MainScreen(
            onNavigateToProjectDetail = {},
            onNavigateToImageViewer = {},
            onCreateProject = {},
            onSignOut = {}
        )
    }
}
