package com.alexanderglueck.urlpusher.domain.model

data class Device(
    val id: Long,
    val name: String,
    val canPush: Boolean,
)
