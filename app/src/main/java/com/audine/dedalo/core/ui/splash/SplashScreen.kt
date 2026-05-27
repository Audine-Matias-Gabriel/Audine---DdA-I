package com.audine.dedalo.core.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.auth.domain.AuthUiState
import com.audine.dedalo.core.ui.theme.DedaloTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    uiState: AuthUiState,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentState by rememberUpdatedState(uiState)
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000L)
        snapshotFlow { currentState }
            .filter { it !is AuthUiState.Loading }
            .first()
            .let { state ->
                when (state) {
                    is AuthUiState.Authenticated -> onNavigateToMain()
                    else -> onNavigateToLogin()
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F3B4D)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "D",
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4942B),
            modifier = Modifier.alpha(alphaAnim)
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun SplashScreenLoadingPreview() {
    DedaloTheme { SplashScreen(uiState = AuthUiState.Loading, onNavigateToLogin = {}, onNavigateToMain = {}) }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun SplashScreenAuthenticatedPreview() {
    val user = UserEntity(id = "1", displayName = "Demo User")
    DedaloTheme { SplashScreen(uiState = AuthUiState.Authenticated(user), onNavigateToLogin = {}, onNavigateToMain = {}) }
}
