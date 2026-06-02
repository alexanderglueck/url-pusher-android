package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UrlDto(
    val id: String,
    val url: String,
    val title: String? = null,
    val image: String? = null,
    @SerialName("device_id") val deviceId: String? = null,
    val device: DeviceDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
