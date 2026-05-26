package com.audine.dedalo.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.audine.dedalo.auth.ui.LoginScreen
import com.audine.dedalo.core.ui.components.ImageViewerScreen
import com.audine.dedalo.core.ui.components.MainScreen
import com.audine.dedalo.core.ui.splash.SplashScreen
import com.audine.dedalo.projects.ui.detail.ProjectDetailScreen

@Composable
fun NavGraph(rootNavController: NavHostController) {
    NavHost(
        navController = rootNavController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
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
            LoginScreen(
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
                }
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
