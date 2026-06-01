package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataEnvelope<T>(val data: T)
