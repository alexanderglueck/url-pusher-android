package com.alexanderglueck.urlpusher.data.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.alexanderglueck.urlpusher.MainActivity
import com.alexanderglueck.urlpusher.R
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.domain.repository.DevicesRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class UrlPusherMessagingService : FirebaseMessagingService() {

    @Inject lateinit var devicesRepository: DevicesRepository
    @Inject lateinit var sessionStore: SessionStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: ${token.take(12)}…")
        scope.launch { devicesRepository.onFcmTokenRefreshed(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(
            TAG,
            "onMessageReceived id=${message.messageId} from=${message.from} " +
                "dataKeys=${message.data.keys} hasNotification=${message.notification != null}",
        )

        val signedIn = runBlocking { sessionStore.current().user != null }

        val url = message.data["url"]
        val title = message.data["title"]?.takeIf { it.isNotBlank() }
            ?: message.notification?.title

        when {
            !signedIn -> {
                Log.d(TAG, "Not signed in — showing generic notification")
                showNotification(
                    title = getString(R.string.notification_signed_out_title),
                    body = getString(R.string.notification_signed_out_body),
                    url = null,
                )
            }
            url != null -> {
                showNotification(title = title ?: url, body = url, url = url)
            }
            message.notification != null -> {
                val body = message.notification?.body.orEmpty()
                Log.d(TAG, "No data.url — falling back to notification block")
                showNotification(title = title ?: getString(R.string.app_name), body = body, url = null)
            }
            else -> {
                Log.w(TAG, "Message has no data.url and no notification block — dropping")
            }
        }
    }

    private fun showNotification(title: String, body: String, url: String?) {
        val intent = if (url != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(ContextCompat.getColor(this, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService<NotificationManager>() ?: return
        manager.notify(Random.nextInt(), notification)
        Log.d(TAG, "notify() called title='$title'")
    }

    private companion object {
        const val TAG = "UrlPusherFcm"
    }
}
