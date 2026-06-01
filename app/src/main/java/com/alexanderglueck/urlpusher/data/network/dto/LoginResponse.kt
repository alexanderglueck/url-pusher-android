package com.alexanderglueck.urlpusher.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserDto,
)
