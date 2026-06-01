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

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val errorRes: Int? = null,
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorRes = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorRes = null) }

    fun submit() {
        val current = _state.value
        if (current.submitting) return
        val firstError = when {
            current.email.isBlank() -> R.string.auth_error_empty_email
            current.password.isBlank() -> R.string.auth_error_empty_password
            else -> null
        }
        if (firstError != null) {
            _state.update { it.copy(errorRes = firstError) }
            return
        }
        _state.update { it.copy(submitting = true, errorRes = null) }
        viewModelScope.launch {
            val result = authRepository.login(
                email = current.email.trim(),
                password = current.password,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            )
            _state.update {
                it.copy(
                    submitting = false,
                    errorRes = if (result.isSuccess) null else R.string.signin_error_generic,
                )
            }
        }
    }
}
