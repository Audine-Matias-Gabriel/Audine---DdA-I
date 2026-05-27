package com.audine.dedalo.auth.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audine.dedalo.auth.domain.AuthUiState
import com.audine.dedalo.core.ui.theme.DedaloTheme
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result ->
        Log.d("Auth", "FirebaseUI resultCode=${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("Auth", "FirebaseUI login exitoso, usuario autenticado por FirebaseAuth")
        } else {
            Log.d("Auth", "FirebaseUI cancelado o error: ${result.idpResponse?.error?.message}")
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F3B4D),
                        Color(0xFF1A5E6E),
                        Color(0xFFD4942B)
                    ),
                    start = Offset.Zero,
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = "D",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4942B)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Dedalo",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Gestión de obras",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        // ... in LoginScreen.kt

                        onClick = {
                            Log.d("Auth", "Iniciando FirebaseUI Auth")
                            val intent = AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(
                                    listOf(AuthUI.IdpConfig.GoogleBuilder().build())
                                )
                                .setIsSmartLockEnabled(false)
                                .setAlwaysShowSignInMethodScreen(true) // Add this line
                                .build()
                            signInLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F3B4D)
                        ),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF0F3B4D),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Iniciar sesión con Google",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (uiState is AuthUiState.Error) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun LoginScreenUnauthenticatedPreview() {
    DedaloTheme { LoginScreen(uiState = AuthUiState.Unauthenticated, onLoginSuccess = {}) }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun LoginScreenLoadingPreview() {
    DedaloTheme { LoginScreen(uiState = AuthUiState.Loading, onLoginSuccess = {}) }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun LoginScreenErrorPreview() {
    DedaloTheme { LoginScreen(uiState = AuthUiState.Error("Error de autenticación"), onLoginSuccess = {}) }
}
