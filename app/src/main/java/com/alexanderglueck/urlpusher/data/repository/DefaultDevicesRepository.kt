package com.alexanderglueck.urlpusher.data.repository

import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.data.network.ApiService
import com.alexanderglueck.urlpusher.data.network.dto.AttachTokenRequest
import com.alexanderglueck.urlpusher.domain.model.Device
import com.alexanderglueck.urlpusher.domain.repository.DevicesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDevicesRepository @Inject constructor(
    private val api: ApiService,
    private val sessionStore: SessionStore,
) : DevicesRepository {

    override suspend fun list(): Result<List<Device>> = runCatching {
        api.listDevices().data.map { Device(it.id, it.name, it.canPush) }
    }

    override suspend fun selectDevice(device: Device): Result<Unit> = runCatching {
        val fcm = sessionStore.current().fcmToken
        if (!fcm.isNullOrBlank()) {
            api.attachToken(device.id, AttachTokenRequest(fcm))
        }
        sessionStore.saveActiveDevice(device.id, device.name)
    }

    override suspend fun onFcmTokenRefreshed(token: String) {
        sessionStore.saveFcmToken(token)
        val snapshot = sessionStore.current()
        val deviceId = snapshot.activeDeviceId ?: return
        if (snapshot.user == null) return
        runCatching { api.attachToken(deviceId, AttachTokenRequest(token)) }
    }
}
