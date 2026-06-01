package com.alexanderglueck.urlpusher.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.domain.model.Device
import com.alexanderglueck.urlpusher.domain.repository.DevicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevicePickerUiState(
    val loading: Boolean = true,
    val devices: List<Device> = emptyList(),
    val error: String? = null,
    val selecting: String? = null,
)

@HiltViewModel
class DevicePickerViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DevicePickerUiState())
    val state: StateFlow<DevicePickerUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            devicesRepository.list()
                .onSuccess { list -> _state.update { it.copy(loading = false, devices = list) } }
                .onFailure { err -> _state.update { it.copy(loading = false, error = err.message ?: "error") } }
        }
    }

    fun select(deviceId: String) {
        if (_state.value.selecting != null) return
        _state.update { it.copy(selecting = deviceId, error = null) }
        viewModelScope.launch {
            devicesRepository.selectDevice(deviceId)
                .onFailure { err -> _state.update { it.copy(selecting = null, error = err.message ?: "error") } }
        }
    }
}
