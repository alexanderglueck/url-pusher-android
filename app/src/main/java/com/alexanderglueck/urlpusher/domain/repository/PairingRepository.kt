package com.alexanderglueck.urlpusher.domain.repository

interface PairingRepository {
    suspend fun pair(rawScannedValue: String): Result<Unit>
}

sealed class PairingException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    object InvalidQr : PairingException("invalid_qr")
    class MissingFcmToken(cause: Throwable) : PairingException("missing_fcm_token", cause)
    class Http(val code: Int, cause: Throwable) : PairingException("http_$code", cause)
    class Network(cause: Throwable) : PairingException("network", cause)
    class Unknown(cause: Throwable) : PairingException("unknown", cause)
}
