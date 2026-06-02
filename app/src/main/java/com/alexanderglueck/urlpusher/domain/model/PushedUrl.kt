package com.alexanderglueck.urlpusher.domain.model

data class PushedUrl(
    val id: String,
    val url: String,
    val title: String?,
    val imageUrl: String?,
    val deviceName: String?,
    val createdAt: String?,
)

data class PushedUrlPage(
    val items: List<PushedUrl>,
    val nextCursor: String?,
)
