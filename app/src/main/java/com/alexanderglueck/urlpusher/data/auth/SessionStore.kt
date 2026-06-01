package com.alexanderglueck.urlpusher.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alexanderglueck.urlpusher.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class SessionSnapshot(
    val user: User?,
    val activeDeviceId: Long?,
    val fcmToken: String?,
)

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store = context.authDataStore

    val snapshot: Flow<SessionSnapshot> = store.data.map { prefs ->
        val id = prefs[KEY_USER_ID]
        val name = prefs[KEY_USER_NAME]
        val email = prefs[KEY_USER_EMAIL]
        val user = if (id != null && name != null && email != null) User(id, name, email) else null
        SessionSnapshot(
            user = user,
            activeDeviceId = prefs[KEY_DEVICE_ID],
            fcmToken = prefs[KEY_FCM_TOKEN],
        )
    }

    suspend fun current(): SessionSnapshot = snapshot.first()

    suspend fun saveUser(user: User) {
        store.edit {
            it[KEY_USER_ID] = user.id
            it[KEY_USER_NAME] = user.name
            it[KEY_USER_EMAIL] = user.email
        }
    }

    suspend fun saveActiveDevice(deviceId: Long) {
        store.edit { it[KEY_DEVICE_ID] = deviceId }
    }

    suspend fun clearActiveDevice() {
        store.edit { it.remove(KEY_DEVICE_ID) }
    }

    suspend fun saveFcmToken(token: String) {
        store.edit { it[KEY_FCM_TOKEN] = token }
    }

    suspend fun clearUser() {
        store.edit {
            it.remove(KEY_USER_ID)
            it.remove(KEY_USER_NAME)
            it.remove(KEY_USER_EMAIL)
            it.remove(KEY_DEVICE_ID)
        }
    }

    private companion object {
        val KEY_USER_ID = intPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_DEVICE_ID = longPreferencesKey("active_device_id")
        val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
    }
}
