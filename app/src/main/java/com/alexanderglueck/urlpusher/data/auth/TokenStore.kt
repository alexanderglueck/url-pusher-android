package com.alexanderglueck.urlpusher.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alexanderglueck.urlpusher.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope scope: CoroutineScope,
) {
    private val store = context.authDataStore
    private val cached = MutableStateFlow<String?>(null)

    val token: StateFlow<String?> = cached.asStateFlow()

    init {
        // Synchronous hydration so the AuthInterceptor never sees a spurious null
        // on the first request after process start. After this, the cache stays
        // up to date via the collect coroutine below — no more blocking.
        cached.value = runBlocking { store.data.first()[KEY_TOKEN] }
        scope.launch {
            store.data.collect { prefs -> cached.value = prefs[KEY_TOKEN] }
        }
    }

    fun peek(): String? = cached.value

    suspend fun save(token: String) {
        store.edit { it[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        store.edit { it.remove(KEY_TOKEN) }
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("api_token")
    }
}
