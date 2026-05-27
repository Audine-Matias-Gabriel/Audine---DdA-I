package com.audine.dedalo.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.audine.dedalo.DedaloApp
import com.audine.dedalo.chat.ui.ChatScreen
import com.audine.dedalo.core.di.ViewModelFactory
import com.audine.dedalo.core.navigation.Routes
import com.audine.dedalo.profile.ui.ProfileScreen
import com.audine.dedalo.core.ui.theme.DedaloTheme
import com.audine.dedalo.projects.ui.list.ProjectsListScreen
import com.audine.dedalo.projects.ui.list.ProjectsViewModel

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun MainScreen(
    onNavigateToProjectDetail: (String) -> Unit,
    onNavigateToImageViewer: (String) -> Unit,
    onCreateProject: () -> Unit
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem(Routes.PROJECTS, "Obras", { Icon(Icons.Default.Build, contentDescription = "Obras") }),
        BottomNavItem(Routes.CHAT, "Chat", { Icon(Icons.Default.Forum, contentDescription = "Chat") }),
        BottomNavItem(Routes.PROFILE, "Perfil", { Icon(Icons.Default.Person, contentDescription = "Perfil") })
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = item.icon,
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            innerNavController.navigate(item.route) {
                                popUpTo(innerNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
                val app = LocalContext.current.applicationContext as DedaloApp
                val viewModel: ProjectsViewModel = viewModel(
                    factory = ViewModelFactory { ProjectsViewModel(app.container.projectRepository) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ProjectsListScreen(
                    uiState = uiState,
                    onCreateProject = onCreateProject,
                    onNavigateToProjectDetail = onNavigateToProjectDetail
                )
            }
            composable(Routes.CHAT) { ChatScreen() }
            composable(Routes.PROFILE) { ProfileScreen() }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, device = "id:pixel_6")
@Composable
private fun MainScreenPreview() {
    DedaloTheme { MainScreen(onNavigateToProjectDetail = {}, onNavigateToImageViewer = {}, onCreateProject = {}) }
}
