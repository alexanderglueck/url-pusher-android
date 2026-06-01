package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    val id: Long,
    val name: String,
    @SerialName("can_push") val canPush: Boolean,
    @SerialName("created_at") val createdAt: String,
)
