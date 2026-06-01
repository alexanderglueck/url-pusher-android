package com.alexanderglueck.urlpusher.domain.repository

import com.alexanderglueck.urlpusher.domain.model.Device

interface DevicesRepository {
    suspend fun list(): Result<List<Device>>
    suspend fun selectDevice(deviceId: Long): Result<Unit>
    suspend fun onFcmTokenRefreshed(token: String)
}
