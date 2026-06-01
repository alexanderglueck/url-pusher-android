package com.alexanderglueck.urlpusher.domain.model

data class Device(
    val id: String,
    val name: String,
    val canPush: Boolean,
)
