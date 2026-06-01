package com.alexanderglueck.urlpusher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.data.auth.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SessionState { Loading, SignedOut, NeedsDevice, Ready }

@HiltViewModel
class SessionViewModel @Inject constructor(
    tokenStore: TokenStore,
    sessionStore: SessionStore,
) : ViewModel() {

    val state: StateFlow<SessionState> =
        combine(tokenStore.token, sessionStore.snapshot) { token, snapshot ->
            when {
                token.isNullOrBlank() || snapshot.user == null -> SessionState.SignedOut
                snapshot.activeDeviceId.isNullOrBlank() -> SessionState.NeedsDevice
                else -> SessionState.Ready
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)
}
