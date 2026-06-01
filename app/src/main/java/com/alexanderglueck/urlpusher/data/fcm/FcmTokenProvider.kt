package com.alexanderglueck.urlpusher.data.fcm

import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenProvider @Inject constructor(
    private val sessionStore: SessionStore,
) {
    suspend fun current(): String {
        val cached = sessionStore.current().fcmToken
        if (!cached.isNullOrBlank()) return cached
        val fresh = FirebaseMessaging.getInstance().token.await()
        sessionStore.saveFcmToken(fresh)
        return fresh
    }
}
