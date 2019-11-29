package com.alexanderglueck.urlpusher;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectDeviceActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private SessionHandler session;
    List<Device> movieList;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        session = new SessionHandler(getApplicationContext());

        movieList = new ArrayList<>();
        recyclerView = (RecyclerView)findViewById(R.id.deviceRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(),movieList);
        recyclerView.setAdapter(recyclerAdapter);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Device>> call = apiService.getDevices("Bearer " + session.getUserDetails().getApiToken());
        call.enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                movieList = response.body();
                Log.d("TAG","Response = "+movieList);
                recyclerAdapter.setMovieList(movieList);
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                Log.d("TAG","Response = "+t.toString());
            }
        });

        getDevices();
    }

    private void getDevices() {
        final JSONArray request = new JSONArray();

        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (com.android.volley.Request.Method.GET, Constants.URL_FETCH_DEVICES, request, new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        //Check if user got logged in successfully
                        Log.d(TAG, response.toString());
                        Log.d(TAG, "should have deleted token");

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonData = response.getJSONObject(i);
                                Log.d(TAG, jsonData.getString("name"));
                                Log.d(TAG, jsonData.getString("device_token"));
                                Log.d(TAG, "" + jsonData.getInt("id"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TSDFEERROR", "" + error.getMessage() + error.getLocalizedMessage() + error.toString());

                        NetworkResponse response = error.networkResponse;
                        String temp = "";
                        if (response != null && response.data != null) {
                            temp = new String(response.data, StandardCharsets.UTF_8);
                        }

                        Log.d("TSDFEERROR2", "" + temp);
                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + session.getUserDetails().getApiToken());
                return params;
            }
        };

        jsArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                15 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);

    }
}
