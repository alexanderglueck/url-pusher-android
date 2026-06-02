package com.alexanderglueck.urlpusher.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
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
    val activeDeviceId: String?,
    val activeDeviceName: String?,
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
            activeDeviceName = prefs[KEY_DEVICE_NAME],
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

    suspend fun saveActiveDevice(deviceId: String, deviceName: String) {
        store.edit {
            it[KEY_DEVICE_ID] = deviceId
            it[KEY_DEVICE_NAME] = deviceName
        }
    }

    suspend fun clearActiveDevice() {
        store.edit {
            it.remove(KEY_DEVICE_ID)
            it.remove(KEY_DEVICE_NAME)
        }
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
            it.remove(KEY_DEVICE_NAME)
        }
    }

    private companion object {
        // Suffixed keys: IDs migrated from numeric to ULID strings. The old
        // Int/Long-typed values under the unsuffixed names are abandoned (reading
        // them as String would throw), so a pre-migration session reads as
        // signed-out and the user re-authenticates — matching the server cutover.
        val KEY_USER_ID = stringPreferencesKey("user_id_ulid")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_DEVICE_ID = stringPreferencesKey("active_device_id_ulid")
        val KEY_DEVICE_NAME = stringPreferencesKey("active_device_name")
        val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
    }
}
