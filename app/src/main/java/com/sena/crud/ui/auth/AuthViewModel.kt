package com.sena.crud.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.crud.domain.model.User
import com.sena.crud.domain.usecase.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Authenticated(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authUseCases.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.value = AuthUiState.Authenticated(user)
                } else {
                    _uiState.value = AuthUiState.Idle
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor, rellene todos los campos.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authUseCases.login(email, pass).fold(
                onSuccess = { user -> _uiState.value = AuthUiState.Authenticated(user) },
                onFailure = { error -> _uiState.value = AuthUiState.Error(error.message ?: "Error al iniciar sesión") }
            )
        }
    }

    fun register(email: String, pass: String, confirmPass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor, rellene todos los campos.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("El formato del correo electrónico no es válido.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authUseCases.register(email, pass, name).fold(
                onSuccess = { user -> _uiState.value = AuthUiState.Authenticated(user) },
                onFailure = { error -> _uiState.value = AuthUiState.Error(error.message ?: "Error en el registro") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCases.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
