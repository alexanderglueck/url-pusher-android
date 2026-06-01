package com.alexanderglueck.urlpusher.domain.repository

import com.alexanderglueck.urlpusher.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String, deviceName: String): Result<User>
    suspend fun logout(): Result<Unit>
}
