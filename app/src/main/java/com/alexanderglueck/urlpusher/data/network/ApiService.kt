package com.alexanderglueck.urlpusher.data.network

import com.alexanderglueck.urlpusher.data.network.dto.AttachTokenRequest
import com.alexanderglueck.urlpusher.data.network.dto.CursorPage
import com.alexanderglueck.urlpusher.data.network.dto.DataEnvelope
import com.alexanderglueck.urlpusher.data.network.dto.DeviceDto
import com.alexanderglueck.urlpusher.data.network.dto.LoginRequest
import com.alexanderglueck.urlpusher.data.network.dto.LoginResponse
import com.alexanderglueck.urlpusher.data.network.dto.PairDeviceRequest
import com.alexanderglueck.urlpusher.data.network.dto.PairDeviceResponse
import com.alexanderglueck.urlpusher.data.network.dto.RegisterRequest
import com.alexanderglueck.urlpusher.data.network.dto.UrlDto
import com.alexanderglueck.urlpusher.data.network.dto.UrlStoreRequest
import com.alexanderglueck.urlpusher.data.network.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): LoginResponse

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
        @Path("device") deviceId: Long,
        @Body body: AttachTokenRequest,
    ): DataEnvelope<DeviceDto>

    @DELETE("devices/{device}/token")
    suspend fun removeToken(@Path("device") deviceId: Long): Response<Unit>

    @POST("devices/pair")
    suspend fun pair(@Body body: PairDeviceRequest): PairDeviceResponse

    @POST("urls")
    suspend fun pushUrl(@Body body: UrlStoreRequest): Response<Unit>

    @GET("urls")
    suspend fun listUrls(@Query("cursor") cursor: String? = null): CursorPage<UrlDto>

    @DELETE("urls/{url}")
    suspend fun deleteUrl(@Path("url") urlId: Long): Response<Unit>
}
