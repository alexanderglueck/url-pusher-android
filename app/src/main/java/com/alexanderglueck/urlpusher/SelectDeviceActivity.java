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

    }
}
