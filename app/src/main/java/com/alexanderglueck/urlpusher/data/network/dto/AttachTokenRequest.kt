package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttachTokenRequest(val token: String)
