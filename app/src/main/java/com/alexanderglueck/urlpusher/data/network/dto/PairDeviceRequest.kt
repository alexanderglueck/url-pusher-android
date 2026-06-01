package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PairDeviceRequest(
    val code: String,
    val name: String,
    val token: String,
)
