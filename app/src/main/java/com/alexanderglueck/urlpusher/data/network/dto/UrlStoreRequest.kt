package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UrlStoreRequest(
    val url: String,
    @SerialName("device_id") val deviceId: Long,
)
