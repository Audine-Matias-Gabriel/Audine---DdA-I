package com.audine.dedalo.core.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.audine.dedalo.auth.ui.AuthViewModel
import com.audine.dedalo.auth.ui.LoginScreen
import com.audine.dedalo.core.ui.components.ImageViewerScreen
import com.audine.dedalo.core.ui.components.MainScreen
import com.audine.dedalo.core.ui.splash.SplashScreen
import com.audine.dedalo.projects.ui.create.CreateProjectScreen
import com.audine.dedalo.projects.ui.create.CreateProjectViewModel
import com.audine.dedalo.projects.ui.detail.ProjectDetailScreen
import com.audine.dedalo.projects.ui.detail.ProjectDetailViewModel
import com.audine.dedalo.projects.ui.edit.EditProjectScreen
import com.audine.dedalo.projects.ui.edit.EditProjectViewModel

@Composable
fun NavGraph(rootNavController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = rootNavController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
            SplashScreen(
                uiState = uiState,
                onNavigateToLogin = {
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    rootNavController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
            LoginScreen(
                uiState = uiState,
                onLoginSuccess = {
                    rootNavController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToProjectDetail = { obraId ->
                    rootNavController.navigate(Routes.projectDetail(obraId))
                },
                onNavigateToImageViewer = { url ->
                    rootNavController.navigate(Routes.imageViewer(url))
                },
                onCreateProject = {
                    rootNavController.navigate(Routes.CREATE_PROJECT)
                },
                onSignOut = {
                    authViewModel.signOut()
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CREATE_PROJECT) {
            val viewModel: CreateProjectViewModel = hiltViewModel()
            CreateProjectScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
        composable(
            route = Routes.PROJECT_DETAIL,
            arguments = listOf(navArgument("obraId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val obraId = backStackEntry.arguments?.getString("obraId") ?: ""
            val viewModel: ProjectDetailViewModel = hiltViewModel()
            ProjectDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() },
                onNavigateToEdit = { rootNavController.navigate(Routes.editProject(obraId)) },
                onNavigateToImageViewer = { url ->
                    rootNavController.navigate(Routes.imageViewer(url))
                },
                onOpenInMap = { lat, lon ->
                    val uri = Uri.parse("geo:${lat},${lon}?q=${lat},${lon}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            )
        }
        composable(
            route = Routes.EDIT_PROJECT,
            arguments = listOf(navArgument("obraId") { type = NavType.StringType })
        ) {
            val viewModel: EditProjectViewModel = hiltViewModel()
            EditProjectScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
        composable(
            route = Routes.IMAGE_VIEWER,
            arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: return@composable
            ImageViewerScreen(
                imageUrl = imageUrl,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
    }
}
