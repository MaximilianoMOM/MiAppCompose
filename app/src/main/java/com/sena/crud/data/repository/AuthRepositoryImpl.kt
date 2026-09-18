package com.sena.crud.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.sena.crud.domain.model.User
import com.sena.crud.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(User(id = firebaseUser.uid, email = firebaseUser.email.orEmpty(), displayName = firebaseUser.displayName))
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, pass: String): Result<User> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        val firebaseUser = result.user ?: throw Exception("Usuario nulo tras login exitoso")
        Result.success(User(id = firebaseUser.uid, email = firebaseUser.email.orEmpty(), displayName = firebaseUser.displayName))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(email: String, pass: String, name: String): Result<User> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        val firebaseUser = result.user ?: throw Exception("Usuario nulo tras registro exitoso")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        firebaseUser.updateProfile(profileUpdates).await()
        Result.success(User(id = firebaseUser.uid, email = firebaseUser.email.orEmpty(), displayName = name))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
