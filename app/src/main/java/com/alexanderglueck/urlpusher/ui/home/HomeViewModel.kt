package com.alexanderglueck.urlpusher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.domain.model.PushedUrl
import com.alexanderglueck.urlpusher.domain.repository.AuthRepository
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
)

data class FeedState(
    val items: List<PushedUrl> = emptyList(),
    val initialLoading: Boolean = true,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val errorRes: Int? = null,
    val nextCursor: String? = null,
    val deletingId: Long? = null,
)

enum class HomeToast { Pushed, PushFailed, Deleted, DeleteFailed }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository,
    private val urlsRepository: UrlsRepository,
) : ViewModel() {

    private val _feed = MutableStateFlow(FeedState())
    private val transient = MutableStateFlow(Transient())

    val state: StateFlow<HomeUiState> = combine(
        sessionStore.snapshot,
        _feed,
        transient,
    ) { snapshot, feed, t ->
        HomeUiState(
            userName = snapshot.user?.name.orEmpty(),
            activeDeviceName = feed.items
                .firstOrNull { it.deviceName != null }
                ?.deviceName
                ?: snapshot.activeDeviceId?.let { "Device $it" },
            feed = feed,
            pushing = t.pushing,
            toast = t.toast,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        refresh()
    }

    fun refresh() {
        if (_feed.value.refreshing) return
        val firstLoad = _feed.value.items.isEmpty()
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

    fun delete(id: Long) {
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

    fun changeDevice() = viewModelScope.launch { sessionStore.clearActiveDevice() }

    fun signOut() = viewModelScope.launch { authRepository.logout() }

    fun consumeToast() = transient.update { it.copy(toast = null) }

    private data class Transient(val pushing: Boolean = false, val toast: HomeToast? = null)
}
