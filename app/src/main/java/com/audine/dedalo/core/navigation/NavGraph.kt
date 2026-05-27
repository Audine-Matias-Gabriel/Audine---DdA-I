package com.audine.dedalo.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.audine.dedalo.DedaloApp
import com.audine.dedalo.auth.ui.AuthViewModel
import com.audine.dedalo.auth.ui.LoginScreen
import com.audine.dedalo.core.di.ViewModelFactory
import com.audine.dedalo.core.ui.components.ImageViewerScreen
import com.audine.dedalo.core.ui.components.MainScreen
import com.audine.dedalo.core.ui.splash.SplashScreen
import com.audine.dedalo.projects.ui.create.CreateProjectScreen
import com.audine.dedalo.projects.ui.create.CreateProjectViewModel
import com.audine.dedalo.projects.ui.detail.ProjectDetailScreen

@Composable
fun NavGraph(rootNavController: NavHostController) {
    val app = LocalContext.current.applicationContext as DedaloApp
    val authViewModel: AuthViewModel = viewModel(
        key = "auth",
        factory = ViewModelFactory { AuthViewModel(app.container.authRepository) }
    )

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
                onSignInWithGoogle = { idToken -> authViewModel.signInWithGoogle(idToken) },
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
                }
            )
        }
        composable(Routes.CREATE_PROJECT) {
            val app = LocalContext.current.applicationContext as DedaloApp
            val viewModel: CreateProjectViewModel = viewModel(
                factory = ViewModelFactory {
                    CreateProjectViewModel(
                        repository = app.container.projectRepository,
                        locationiqService = app.container.locationiqService,
                        storage = app.container.storage
                    )
                }
            )
            CreateProjectScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
        composable(Routes.PROJECT_DETAIL) {
            ProjectDetailScreen(
                onNavigateBack = { rootNavController.popBackStack() },
                onNavigateToImageViewer = { url ->
                    rootNavController.navigate(Routes.imageViewer(url))
                }
            )
        }
        composable(Routes.IMAGE_VIEWER) {
            ImageViewerScreen(
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
    }
}
