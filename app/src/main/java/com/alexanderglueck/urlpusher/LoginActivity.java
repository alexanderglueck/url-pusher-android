package com.alexanderglueck.urlpusher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String KEY_STATUS = "status";
    private static final String TAG = "LoginActivity";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMPTY = "";
    private EditText etUsername;
    private EditText etPassword;
    private String username;
    private String password;
    private ProgressDialog pDialog;
    private SessionHandler session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            loadDashboard();
        }
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);

        Button register = findViewById(R.id.register);
        Button login = findViewById(R.id.login);

        //Launch Registration screen when Register Button is clicked
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Retrieve the data entered in the edit texts
                username = etUsername.getText().toString().toLowerCase().trim();
                password = etPassword.getText().toString().trim();
                if (validateInputs()) {
                    login();
                }
            }
        });
    }

    /**
     * Launch Dashboard Activity on Successful Login
     */
    private void loadDashboard() {

        Intent i = new Intent(getApplicationContext(), MainActivity.class);

        if (getIntent().getExtras() != null) {

            Log.d(TAG, "per extra erhalten, dh von zu ");


            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);

                if (key.equals(Constants.INTENT_EXTRA_NOTIFICATION)) {
                    // notification received from start
                    //

                    Notification notification = (Notification) getIntent().getExtras().getSerializable(Constants.INTENT_EXTRA_NOTIFICATION);

                    if (notification != null) {
                        i.putExtra(Constants.INTENT_EXTRA_NOTIFICATION, notification);
                    }
                }
            }
        }


        startActivity(i);
        finish();

    }

    /**
     * Display Progress bar while Logging in
     */

    private void displayLoader() {
        pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setMessage("Logging In.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    private void login() {
        displayLoader();
        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_USERNAME, username);
            request.put(KEY_PASSWORD, password);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, Constants.URL_LOGIN, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();
                        boolean openDeviceSelection = false;
                        try {
                            //Check if user got logged in successfully

                            Log.d(TAG, response.toString());
                            if (response.getInt("id") > 0) {

                                int userId = response.getInt("id");
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE);

                                if (userId != sharedPreferences.getInt(Constants.LAST_SIGNED_IN_USER_ID, 0)) {
                                    Log.d(TAG, "anderer user");

                                    // anderer user hat sich eingeloggt als zuvor

                                    // die jetzt eingeloggte id als zuletzte wegspeichern
                                    sharedPreferences.edit().putInt(Constants.LAST_SIGNED_IN_USER_ID, userId).commit();

                                    // reset last used device id, because new user
                                    sharedPreferences.edit().putInt(Constants.LAST_SIGNED_IN_DEVICE_ID, 0).commit();

                                    // weil ein anderer user eingeloggt, alten token ungültig machen, sonst erhält er pushes vom anderen
                                    String token = sharedPreferences.getString(Constants.FCM_TOKEN, "");
                                    if (!token.equals("")) {
                                        deleteToken(token);
                                    }


                                    // weil neuer user auch device selection öffnen
                                    // last used device id leeren to not reconnect with wrong
                                    sharedPreferences.edit().putInt(Constants.LAST_SIGNED_IN_DEVICE_ID, 0).commit();

                                    openDeviceSelection = true;

                                } else {
                                    Log.d(TAG, "gleicher user");
                                    // gleicher user hat nach login sich wieder eingeloggt

                                    // get list of devices
                                    // if last saved device id still exists
                                    // check if tokens match up, if same token
                                    // reselect this device. we are still set up


                                    // if last saved device id still exists
                                    // check if tokens match up, if different token,
                                    // expect the tolken to have changed
                                    // update the device token and push to server
                                    // a device token change should almost never happen

                                    // if the device id no longer exists,
                                    // open device chooser
                                    // save selected device id

                                    openDeviceSelection = true;
                                }

                                session.loginUser(response.getInt("id"), username, response.getString("name"), response.getString("api_token"));

                                if (openDeviceSelection) {
                                    openDeviceSelectionActivity();
                                } else {
                                    loadDashboard();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        response.getString(KEY_MESSAGE), Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();
                        Log.d("TSDFEERROR", "" + error.getMessage() + error.getLocalizedMessage() + error.toString());
                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        jsArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                15 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }

    private void openDeviceSelectionActivity() {
        Intent i = new Intent(getApplicationContext(), SelectDeviceActivity.class);

        startActivity(i);
        finish();

    }

    private void deleteToken(String token) {
        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put("token", token);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, Constants.URL_DESTROY_TOKEN, request, new Response.Listener<JSONObject>() {
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

    /**
     * Validates inputs and shows error if any
     *
     * @return
     */
    private boolean validateInputs() {
        if (KEY_EMPTY.equals(username)) {
            etUsername.setError("Username cannot be empty");
            etUsername.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(password)) {
            etPassword.setError("Password cannot be empty");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }
}
