package com.alexanderglueck.urlpusher;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectDeviceActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private SessionHandler session;
    List<Device> deviceList;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        session = new SessionHandler(getApplicationContext());

        deviceList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.deviceRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), deviceList);
        recyclerView.setAdapter(recyclerAdapter);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Device>> call = apiService.getDevices("Bearer " + session.getUserDetails().getApiToken());
        call.enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                deviceList = response.body();
                Log.d("TAG", "Response = " + deviceList);
                recyclerAdapter.setMovieList(deviceList);
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                Log.d("TAG", "Response = " + t.toString());
            }
        });
    }
}
