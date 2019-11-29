package com.alexanderglueck.urlpusher;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiInterface {
    @GET("devices")
    Call<List<Device>> getDevices(@Header("Authorization") String authHeader);

    @POST("attach-token")
    Call<List<Device>> attachToken(@Header("Authorization") String authHeader);

    @POST("remove-token")
    Call<List<Device>> removeToken(@Header("Authorization") String authHeader);

    @POST("session")
    Call<Session> login();
}
