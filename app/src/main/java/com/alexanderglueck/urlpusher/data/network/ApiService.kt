package com.alexanderglueck.urlpusher.data.network

import com.alexanderglueck.urlpusher.data.network.dto.AttachTokenRequest
import com.alexanderglueck.urlpusher.data.network.dto.DataEnvelope
import com.alexanderglueck.urlpusher.data.network.dto.DeviceDto
import com.alexanderglueck.urlpusher.data.network.dto.LoginRequest
import com.alexanderglueck.urlpusher.data.network.dto.LoginResponse
import com.alexanderglueck.urlpusher.data.network.dto.UrlStoreRequest
import com.alexanderglueck.urlpusher.data.network.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("auth/me")
    suspend fun me(): DataEnvelope<UserDto>

    @GET("devices")
    suspend fun listDevices(): DataEnvelope<List<DeviceDto>>

    @POST("devices/{device}/token")
    suspend fun attachToken(
        @Path("device") deviceId: String,
        @Body body: AttachTokenRequest,
    ): DataEnvelope<DeviceDto>

    @DELETE("devices/{device}/token")
    suspend fun removeToken(@Path("device") deviceId: String): Response<Unit>

    @POST("urls")
    suspend fun pushUrl(@Body body: UrlStoreRequest): Response<Unit>
}
