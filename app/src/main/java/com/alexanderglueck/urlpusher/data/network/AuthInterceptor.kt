package com.alexanderglueck.urlpusher.data.network

import android.util.Log
import com.alexanderglueck.urlpusher.data.auth.SessionStore
import com.alexanderglueck.urlpusher.data.auth.TokenStore
import com.alexanderglueck.urlpusher.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val sessionStore: SessionStore,
    @ApplicationScope private val scope: CoroutineScope,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.peek()
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .apply { if (!token.isNullOrBlank()) header("Authorization", "Bearer $token") }
            .build()
        val response = chain.proceed(request)
        if (response.code == 401 && !token.isNullOrBlank()) {
            Log.w(TAG, "401 from ${request.url} — wiping local session")
            scope.launch {
                tokenStore.clear()
                sessionStore.clearUser()
            }
        }
        return response
    }

    private companion object {
        const val TAG = "AuthInterceptor"
    }
}
