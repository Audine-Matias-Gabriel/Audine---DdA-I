package com.audine.dedalo.auth.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val userDao: UserDao
) {

    sealed class AuthState {
        data object Loading : AuthState()
        data object Unauthenticated : AuthState()
        data class Authenticated(val user: UserEntity) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val authStateFlow: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            trySend(
                if (firebaseUser != null) AuthState.Authenticated(firebaseUser.toEntity())
                else AuthState.Unauthenticated
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUser: Flow<UserEntity?> = userDao.observeCurrentUser()

    fun observeAuthState(): Flow<AuthState> = authStateFlow

    suspend fun signInWithGoogle(idToken: String) {
        Log.d("Auth", "signInWithGoogle en AuthRepository: creando credential y llamando a Firebase")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        Log.d("Auth", "Firebase signInWithCredential completado, user=${authResult.user?.uid}")
        authResult.user?.let {
            Log.d("Auth", "Usuario autenticado: ${it.uid}, email=${it.email}")
            persistUser(it)
        } ?: Log.w("Auth", "Firebase authResult.user es null")
    }

    suspend fun persistUser(firebaseUser: FirebaseUser) {
        Log.d("Auth", "persistUser: limpiando currentUser y guardando ${firebaseUser.uid}")
        userDao.clearCurrentUser()
        userDao.upsert(firebaseUser.toEntity().copy(isCurrentUser = true))
        Log.d("Auth", "persistUser completado")
    }

    suspend fun clearUser() {
        userDao.clearCurrentUser()
    }

    fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.toEntity() = UserEntity(
        id = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )

    val isLoggedIn: Boolean
        get() = auth.currentUser != null
}
