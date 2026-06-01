package com.alexanderglueck.urlpusher.data.repository

import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.data.network.ApiService
import com.alexanderglueck.urlpusher.data.network.dto.UrlStoreRequest
import com.alexanderglueck.urlpusher.domain.model.PushedUrl
import com.alexanderglueck.urlpusher.domain.model.PushedUrlPage
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

    override suspend fun list(cursor: String?): Result<PushedUrlPage> = runCatching {
        val page = api.listUrls(cursor)
        PushedUrlPage(
            items = page.data.map { dto ->
                PushedUrl(
                    id = dto.id,
                    url = dto.url,
                    title = dto.title?.takeIf { it.isNotBlank() },
                    deviceName = dto.device?.name,
                    createdAt = dto.createdAt,
                )
            },
            nextCursor = page.meta.nextCursor,
        )
    }

    override suspend fun delete(id: Long): Result<Unit> = runCatching {
        val response = api.deleteUrl(id)
        if (!response.isSuccessful) error("delete_failed_${response.code()}")
    }
}
