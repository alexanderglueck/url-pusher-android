package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CursorPage<T>(
    val data: List<T>,
    val meta: CursorMeta,
)

@Serializable
data class CursorMeta(
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("prev_cursor") val prevCursor: String? = null,
    @SerialName("per_page") val perPage: Int = 0,
)
