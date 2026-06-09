package pe.kusicred.app.features.auth.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.kusicred.app.features.auth.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun checkAuthState(onLoggedIn: () -> Unit, onNotLoggedIn: () -> Unit) {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                onLoggedIn()
            } else {
                onNotLoggedIn()
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            if (email.isBlank() || pass.isBlank()) {
                _uiState.value = AuthUiState.Error("Por favor ingresa tu correo y contraseña")
                return@launch
            }

            val result = authRepository.login(email, pass)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            if (name.isBlank() || email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
                _uiState.value = AuthUiState.Error("Por favor completa todos los campos")
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _uiState.value = AuthUiState.Error("Ingresa un correo válido")
                return@launch
            }
            if (pass.length < 6) {
                _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres")
                return@launch
            }
            if (pass != confirmPass) {
                _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
                return@launch
            }

            val result = authRepository.registerUser(name, email, pass)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Error al registrar")
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}
