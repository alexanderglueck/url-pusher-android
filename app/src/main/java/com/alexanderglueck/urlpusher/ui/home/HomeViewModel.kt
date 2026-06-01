package com.alexanderglueck.urlpusher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.domain.repository.AuthRepository
import com.alexanderglueck.urlpusher.domain.repository.DevicesRepository
import com.alexanderglueck.urlpusher.domain.repository.UrlsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val activeDeviceName: String? = null,
    val pushing: Boolean = false,
    val toast: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository,
    private val devicesRepository: DevicesRepository,
    private val urlsRepository: UrlsRepository,
) : ViewModel() {

    private val deviceNames = MutableStateFlow<Map<String, String>>(emptyMap())
    private val transient = MutableStateFlow(TransientState())

    val state: StateFlow<HomeUiState> = combine(
        sessionStore.snapshot,
        deviceNames,
        transient,
    ) { snapshot, names, transientState ->
        HomeUiState(
            userName = snapshot.user?.name ?: "",
            activeDeviceName = snapshot.activeDeviceId?.let { names[it] },
            pushing = transientState.pushing,
            toast = transientState.toast,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        viewModelScope.launch {
            devicesRepository.list().onSuccess { list ->
                deviceNames.value = list.associate { it.id to it.name }
            }
        }
    }

    fun pushSharedUrl(url: String) {
        if (transient.value.pushing) return
        transient.update { it.copy(pushing = true, toast = null) }
        viewModelScope.launch {
            urlsRepository.push(url)
                .onSuccess { transient.update { it.copy(pushing = false, toast = TOAST_PUSHED) } }
                .onFailure { transient.update { it.copy(pushing = false, toast = TOAST_FAILED) } }
        }
    }

    fun changeDevice() = viewModelScope.launch {
        sessionStore.clearActiveDevice()
    }

    fun signOut() = viewModelScope.launch {
        authRepository.logout()
    }

    fun consumeToast() = transient.update { it.copy(toast = null) }

    private data class TransientState(val pushing: Boolean = false, val toast: String? = null)

    companion object {
        const val TOAST_PUSHED = "pushed"
        const val TOAST_FAILED = "failed"
    }
}
