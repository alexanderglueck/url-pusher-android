package com.alexanderglueck.urlpusher.domain.repository

import com.alexanderglueck.urlpusher.domain.model.PushedUrlPage

interface UrlsRepository {
    suspend fun push(url: String): Result<Unit>
    suspend fun list(cursor: String? = null): Result<PushedUrlPage>
    suspend fun delete(id: Long): Result<Unit>
}
