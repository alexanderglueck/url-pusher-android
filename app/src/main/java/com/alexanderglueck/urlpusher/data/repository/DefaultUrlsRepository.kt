package com.alexanderglueck.urlpusher.data.repository

import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.data.network.ApiService
import com.alexanderglueck.urlpusher.data.network.dto.UrlStoreRequest
import com.alexanderglueck.urlpusher.domain.repository.UrlsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUrlsRepository @Inject constructor(
    private val api: ApiService,
    private val sessionStore: SessionStore,
) : UrlsRepository {

    override suspend fun push(url: String): Result<Unit> = runCatching {
        val deviceId = sessionStore.current().activeDeviceId
            ?: error("no_device")
        val response = api.pushUrl(UrlStoreRequest(url, deviceId))
        if (!response.isSuccessful) error("push_failed_${response.code()}")
    }
}
