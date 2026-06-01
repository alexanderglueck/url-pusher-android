package com.alexanderglueck.urlpusher.ui.auth

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val errorRes: Int? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorRes = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorRes = null) }

    fun submit() {
        val current = _state.value
        if (current.submitting) return
        val emailError = if (current.email.isBlank()) com.alexanderglueck.urlpusher.R.string.login_error_empty_email else null
        val passwordError = if (current.password.isBlank()) com.alexanderglueck.urlpusher.R.string.login_error_empty_password else null
        val firstError = emailError ?: passwordError
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
                    errorRes = if (result.isSuccess) null else com.alexanderglueck.urlpusher.R.string.login_error_generic,
                )
            }
        }
    }
}
