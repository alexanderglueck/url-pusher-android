package com.alexanderglueck.urlpusher;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SelectDeviceActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private SessionHandler session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        session = new SessionHandler(getApplicationContext());

        getDevices();
    }

    private void getDevices() {
        JSONObject request = new JSONObject();

        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.URL_FETCH_DEVICES, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Check if user got logged in successfully
                        Log.d(TAG, response.toString());
                        Log.d(TAG, "should have deleted token");

                    }
                }, new Response.ErrorListener() {

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
