package com.alexanderglueck.urlpusher.ui.auth

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.R
import com.alexanderglueck.urlpusher.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val submitting: Boolean = false,
    val errorRes: Int? = null,
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, errorRes = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorRes = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorRes = null) }
    fun onPasswordConfirmChange(value: String) =
        _state.update { it.copy(passwordConfirmation = value, errorRes = null) }

    fun submit() {
        val current = _state.value
        if (current.submitting) return
        val error = when {
            current.name.isBlank() -> R.string.signup_error_empty_name
            current.email.isBlank() -> R.string.login_error_empty_email
            current.password.isBlank() -> R.string.login_error_empty_password
            current.password.length < 8 -> R.string.signup_error_short_password
            current.password != current.passwordConfirmation -> R.string.signup_error_password_mismatch
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(errorRes = error) }
            return
        }
        _state.update { it.copy(submitting = true, errorRes = null) }
        viewModelScope.launch {
            val result = authRepository.register(
                name = current.name.trim(),
                email = current.email.trim(),
                password = current.password,
                passwordConfirmation = current.passwordConfirmation,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            )
            _state.update {
                it.copy(
                    submitting = false,
                    errorRes = if (result.isSuccess) null else R.string.signup_error_generic,
                )
            }
        }
    }
}
