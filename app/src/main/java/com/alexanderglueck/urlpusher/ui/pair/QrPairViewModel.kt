package com.alexanderglueck.urlpusher.ui.pair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.R
import com.alexanderglueck.urlpusher.domain.repository.PairingException
import com.alexanderglueck.urlpusher.domain.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QrPairPhase { Idle, Scanning, Pairing }

data class QrPairUiState(
    val phase: QrPairPhase = QrPairPhase.Idle,
    val errorRes: Int? = null,
)

@HiltViewModel
class QrPairViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QrPairUiState())
    val state: StateFlow<QrPairUiState> = _state.asStateFlow()

    fun onScanStarted() = _state.update { it.copy(phase = QrPairPhase.Scanning, errorRes = null) }

    fun onScanCancelled() = _state.update { it.copy(phase = QrPairPhase.Idle) }

    fun onScanFailed() = _state.update {
        it.copy(phase = QrPairPhase.Idle, errorRes = R.string.qr_pair_error_scan)
    }

    fun onScanned(rawValue: String) {
        if (_state.value.phase == QrPairPhase.Pairing) return
        _state.update { it.copy(phase = QrPairPhase.Pairing, errorRes = null) }
        viewModelScope.launch {
            pairingRepository.pair(rawValue)
                .onFailure { err -> _state.update { it.copy(phase = QrPairPhase.Idle, errorRes = errorResFor(err)) } }
            // Success path: SessionState flips to Ready/NeedsDevice and the NavHost navigates away.
        }
    }

    private fun errorResFor(err: Throwable): Int = when (err) {
        PairingException.InvalidQr -> R.string.qr_pair_error_invalid
        is PairingException.MissingFcmToken -> R.string.qr_pair_error_no_fcm
        is PairingException.Http -> when (err.code) {
            401 -> R.string.qr_pair_error_unauthorized
            422 -> R.string.qr_pair_error_expired
            in 500..599 -> R.string.qr_pair_error_server
            else -> R.string.qr_pair_error_pair
        }
        is PairingException.Network -> R.string.qr_pair_error_network
        else -> R.string.qr_pair_error_pair
    }
}
