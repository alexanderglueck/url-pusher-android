package com.alexanderglueck.urlpusher.data.repository

import android.os.Build
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.data.auth.TokenStore
import com.alexanderglueck.urlpusher.data.network.ApiService
import com.alexanderglueck.urlpusher.data.network.dto.LoginRequest
import com.alexanderglueck.urlpusher.domain.model.User
import com.alexanderglueck.urlpusher.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val sessionStore: SessionStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String, deviceName: String): Result<User> =
        runCatching {
            val deviceLabel = deviceName.ifBlank { "${Build.MANUFACTURER} ${Build.MODEL}" }
            val response = api.login(LoginRequest(email = email, password = password, deviceName = deviceLabel))
            tokenStore.save(response.token)
            val user = User(response.user.id, response.user.name, response.user.email)
            sessionStore.saveUser(user)
            user
        }

    override suspend fun logout(): Result<Unit> = runCatching {
        runCatching { api.logout() }
        tokenStore.clear()
        sessionStore.clearUser()
    }
}
