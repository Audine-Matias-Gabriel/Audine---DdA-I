package com.audine.dedalo.auth.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val scope: CoroutineScope
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
            if (firebaseUser != null) {
                launch { persistUser(firebaseUser) }
                trySend(AuthState.Authenticated(firebaseUser.toEntity()))
            } else {
                trySend(AuthState.Unauthenticated)
            }
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

    suspend fun updatePhotoUrl(userId: String, photoUrl: String) {
        userDao.updatePhotoUrl(userId, photoUrl)
        firestore.collection("users").document(userId)
            .update("photoUrl", photoUrl).await()
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

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isLoggedIn: Boolean
        get() = auth.currentUser != null
}
