package com.alexanderglueck.urlpusher;

import com.alexanderglueck.urlpusher.responses.AttachTokenResponse;
import com.alexanderglueck.urlpusher.responses.RemoveTokenResponse;
import com.alexanderglueck.urlpusher.responses.SessionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiInterface {
    @GET("devices")
    Call<List<Device>> getDevices(@Header("Authorization") String authHeader);

    @FormUrlEncoded
    @POST("attach-token")
    Call<AttachTokenResponse> attachToken(@Header("Authorization") String authHeader, @Field("id") int id, @Field("token") String token);

    @FormUrlEncoded
    @POST("remove-token")
    Call<RemoveTokenResponse> removeToken(@Header("Authorization") String authHeader, @Field("token") String token);

    @FormUrlEncoded
    @POST("session")
    Call<SessionResponse> login(@Field("email") String email, @Field("password") String password);
}
