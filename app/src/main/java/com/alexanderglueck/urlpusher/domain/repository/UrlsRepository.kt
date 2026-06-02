package com.alexanderglueck.urlpusher.domain.repository

import com.alexanderglueck.urlpusher.domain.model.PushedUrlPage

interface UrlsRepository {
    /** Push [url] to [deviceId], or to the active device when [deviceId] is null. */
    suspend fun push(url: String, deviceId: String? = null): Result<Unit>
    suspend fun list(cursor: String? = null): Result<PushedUrlPage>
    suspend fun delete(id: String): Result<Unit>
}
