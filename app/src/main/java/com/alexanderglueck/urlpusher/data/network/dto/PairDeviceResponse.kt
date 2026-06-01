package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PairDeviceResponse(
    val device: DeviceDto,
    val token: String? = null,
)
