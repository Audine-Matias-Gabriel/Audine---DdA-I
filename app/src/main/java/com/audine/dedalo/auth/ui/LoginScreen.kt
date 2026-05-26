package com.audine.dedalo.auth.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.audine.dedalo.auth.domain.AuthUiState
import com.audine.dedalo.core.data.remote.AuthConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onSignInWithGoogle: (idToken: String) -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Dedalo",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Gestión de obras",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val googleIdOption = GetSignInWithGoogleOption.Builder(AuthConfig.WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = CredentialManager.create(context)
                                .getCredential(request, activity!!)
                            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                            onSignInWithGoogle(credential.idToken)
                        } catch (_: CancellationException) {
                            // User cancelled dialog
                        } catch (_: Exception) {
                            // Handled by ViewModel -> UiState.Error
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar sesión con Google",
                        style = MaterialTheme.typography.labelLarge
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
