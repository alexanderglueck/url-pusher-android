package com.alexanderglueck.urlpusher.data.repository

import android.net.Uri
import android.os.Build
import android.util.Log
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.data.auth.TokenStore
import com.alexanderglueck.urlpusher.data.fcm.FcmTokenProvider
import com.alexanderglueck.urlpusher.data.network.ApiService
import com.alexanderglueck.urlpusher.data.network.dto.PairDeviceRequest
import com.alexanderglueck.urlpusher.domain.model.User
import com.alexanderglueck.urlpusher.domain.repository.PairingException
import com.alexanderglueck.urlpusher.domain.repository.PairingRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPairingRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val sessionStore: SessionStore,
    private val fcmTokenProvider: FcmTokenProvider,
) : PairingRepository {

    override suspend fun pair(rawScannedValue: String): Result<Unit> {
        val code = extractPairingCode(rawScannedValue)
            ?: return Result.failure(PairingException.InvalidQr)

        val fcmToken = runCatching { fcmTokenProvider.current() }
            .getOrElse { err ->
                Log.w(TAG, "FCM token unavailable", err)
                return Result.failure(PairingException.MissingFcmToken(err))
            }

        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        return runCatching {
            val response = api.pair(PairDeviceRequest(code = code, name = deviceName, token = fcmToken))
            response.token?.takeIf { it.isNotBlank() }?.let { tokenStore.save(it) }
            sessionStore.saveActiveDevice(response.device.id, response.device.name)
            if (sessionStore.current().user == null) {
                val me = api.me().data
                sessionStore.saveUser(User(me.id, me.name, me.email))
            }
        }.recoverCatching { err ->
            Log.w(TAG, "Pair call failed", err)
            throw when (err) {
                is HttpException -> PairingException.Http(err.code(), err)
                is IOException -> PairingException.Network(err)
                else -> PairingException.Unknown(err)
            }
        }
    }

    private fun extractPairingCode(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        // Hierarchical URIs (http://, https://, urlpusher://...) expose query/path APIs;
        // opaque URIs (urlpusher:abc, mailto:foo) throw on getQueryParameter — fall back to raw.
        val parsed = runCatching { Uri.parse(trimmed) }.getOrNull()
        if (parsed != null && parsed.scheme != null && parsed.isHierarchical) {
            runCatching { parsed.getQueryParameter("code") }
                .getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
            parsed.lastPathSegment?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return trimmed
    }

    private companion object {
        const val TAG = "Pairing"
    }
}
