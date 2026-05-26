package com.audine.dedalo.auth.data

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
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        authResult.user?.let { persistUser(it) }
    }

    suspend fun persistUser(firebaseUser: FirebaseUser) {
        userDao.clearCurrentUser()
        userDao.upsert(firebaseUser.toEntity().copy(isCurrentUser = true))
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
