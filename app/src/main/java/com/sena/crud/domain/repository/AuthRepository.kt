package com.sena.crud.domain.repository

import com.sena.crud.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(email: String, pass: String, name: String): Result<User>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
}
