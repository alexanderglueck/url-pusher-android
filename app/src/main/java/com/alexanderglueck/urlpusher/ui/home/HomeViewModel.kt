package com.alexanderglueck.urlpusher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.domain.model.Device
import com.alexanderglueck.urlpusher.domain.model.PushedUrl
import com.alexanderglueck.urlpusher.domain.repository.AuthRepository
import com.alexanderglueck.urlpusher.domain.repository.DevicesRepository
import com.alexanderglueck.urlpusher.domain.repository.UrlsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val activeDeviceName: String? = null,
    val feed: FeedState = FeedState(),
    val pushing: Boolean = false,
    val toast: HomeToast? = null,
    val sendDialog: SendDialogState? = null,
)

/**
 * Drives the "push a link" dialog. The same dialog backs both the compose FAB
 * (editable URL) and the per-item "send to another device" action (fixed URL).
 */
data class SendDialogState(
    val url: String = "",
    val editableUrl: Boolean = true,
    val title: String? = null,
    val devices: List<Device> = emptyList(),
    val loadingDevices: Boolean = true,
    val selectedDeviceId: String? = null,
    val sending: Boolean = false,
) {
    val canSend: Boolean
        get() = !sending && selectedDeviceId != null && url.isNotBlank()
}

data class FeedState(
    val items: List<PushedUrl> = emptyList(),
    val initialLoading: Boolean = true,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val errorRes: Int? = null,
    val nextCursor: String? = null,
    val deletingId: String? = null,
)

enum class HomeToast { Pushed, PushFailed, Deleted, DeleteFailed }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository,
    private val urlsRepository: UrlsRepository,
    private val devicesRepository: DevicesRepository,
) : ViewModel() {

    private val _feed = MutableStateFlow(FeedState())
    private val transient = MutableStateFlow(Transient())
    private val _sendDialog = MutableStateFlow<SendDialogState?>(null)

    val state: StateFlow<HomeUiState> = combine(
        sessionStore.snapshot,
        _feed,
        transient,
        _sendDialog,
    ) { snapshot, feed, t, sendDialog ->
        HomeUiState(
            userName = snapshot.user?.name.orEmpty(),
            activeDeviceName = snapshot.activeDeviceName,
            feed = feed,
            pushing = t.pushing,
            toast = t.toast,
            sendDialog = sendDialog,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        refresh()
    }

    fun refresh() {
        if (_feed.value.refreshing) return
        // Only the very first load shows the full-screen spinner; an explicit
        // pull (even from the empty/error state) shows the pull-to-refresh
        // indicator instead of swapping the whole screen to a spinner.
        val firstLoad = _feed.value.initialLoading
        _feed.update { it.copy(refreshing = !firstLoad, initialLoading = firstLoad, errorRes = null) }
        viewModelScope.launch {
            urlsRepository.list(cursor = null)
                .onSuccess { page ->
                    _feed.update {
                        it.copy(
                            items = page.items,
                            nextCursor = page.nextCursor,
                            initialLoading = false,
                            refreshing = false,
                            errorRes = null,
                        )
                    }
                }
                .onFailure {
                    _feed.update {
                        it.copy(
                            initialLoading = false,
                            refreshing = false,
                            errorRes = com.alexanderglueck.urlpusher.R.string.home_feed_error,
                        )
                    }
                }
        }
    }

    fun loadMore() {
        val current = _feed.value
        if (current.loadingMore || current.refreshing || current.nextCursor == null) return
        _feed.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            urlsRepository.list(cursor = current.nextCursor)
                .onSuccess { page ->
                    _feed.update {
                        it.copy(
                            items = it.items + page.items,
                            nextCursor = page.nextCursor,
                            loadingMore = false,
                        )
                    }
                }
                .onFailure { _feed.update { it.copy(loadingMore = false) } }
        }
    }

    fun delete(id: String) {
        if (_feed.value.deletingId != null) return
        _feed.update { it.copy(deletingId = id) }
        viewModelScope.launch {
            urlsRepository.delete(id)
                .onSuccess {
                    _feed.update {
                        it.copy(
                            items = it.items.filterNot { item -> item.id == id },
                            deletingId = null,
                        )
                    }
                    transient.update { it.copy(toast = HomeToast.Deleted) }
                }
                .onFailure {
                    _feed.update { it.copy(deletingId = null) }
                    transient.update { it.copy(toast = HomeToast.DeleteFailed) }
                }
        }
    }

    fun pushSharedUrl(url: String) {
        if (transient.value.pushing) return
        transient.update { it.copy(pushing = true, toast = null) }
        viewModelScope.launch {
            urlsRepository.push(url)
                .onSuccess {
                    transient.update { it.copy(pushing = false, toast = HomeToast.Pushed) }
                    refresh()
                }
                .onFailure { transient.update { it.copy(pushing = false, toast = HomeToast.PushFailed) } }
        }
    }

    /** Open the compose dialog (FAB) with an empty, editable URL. */
    fun openCompose() {
        _sendDialog.value = SendDialogState(url = "", editableUrl = true)
        loadDevices()
    }

    /** Open the dialog to re-send an existing feed item to another device. */
    fun sendToAnotherDevice(item: PushedUrl) {
        _sendDialog.value = SendDialogState(url = item.url, editableUrl = false, title = item.title)
        loadDevices()
    }

    fun updateComposeUrl(url: String) {
        _sendDialog.update { it?.copy(url = url) }
    }

    fun selectTargetDevice(deviceId: String) {
        _sendDialog.update { it?.copy(selectedDeviceId = deviceId) }
    }

    fun dismissSendDialog() {
        _sendDialog.value = null
    }

    fun confirmSend() {
        val dialog = _sendDialog.value ?: return
        val deviceId = dialog.selectedDeviceId ?: return
        if (!dialog.canSend) return
        _sendDialog.update { it?.copy(sending = true) }
        viewModelScope.launch {
            urlsRepository.push(dialog.url.trim(), deviceId)
                .onSuccess {
                    _sendDialog.value = null
                    transient.update { it.copy(toast = HomeToast.Pushed) }
                    refresh()
                }
                .onFailure {
                    _sendDialog.update { it?.copy(sending = false) }
                    transient.update { it.copy(toast = HomeToast.PushFailed) }
                }
        }
    }

    private fun loadDevices() {
        viewModelScope.launch {
            val activeId = sessionStore.current().activeDeviceId
            devicesRepository.list()
                .onSuccess { devices ->
                    _sendDialog.update { current ->
                        current ?: return@update null
                        val default = devices.firstOrNull { it.id == activeId && it.canPush }
                            ?: devices.firstOrNull { it.canPush }
                        current.copy(
                            devices = devices,
                            loadingDevices = false,
                            selectedDeviceId = current.selectedDeviceId ?: default?.id,
                        )
                    }
                }
                .onFailure {
                    _sendDialog.update { it?.copy(loadingDevices = false) }
                    transient.update { it.copy(toast = HomeToast.PushFailed) }
                }
        }
    }

    fun changeDevice() = viewModelScope.launch { sessionStore.clearActiveDevice() }

    fun signOut() = viewModelScope.launch { authRepository.logout() }

    fun consumeToast() = transient.update { it.copy(toast = null) }

    private data class Transient(val pushing: Boolean = false, val toast: HomeToast? = null)
}
