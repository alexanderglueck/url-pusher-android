package com.alexanderglueck.urlpusher.domain.repository

interface UrlsRepository {
    suspend fun push(url: String): Result<Unit>
}
