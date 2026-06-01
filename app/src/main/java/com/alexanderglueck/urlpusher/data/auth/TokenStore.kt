package com.alexanderglueck.urlpusher.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store = context.authDataStore

    val token: Flow<String?> = store.data.map { it[KEY_TOKEN] }

    suspend fun peek(): String? = store.data.map { it[KEY_TOKEN] }.first()

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
