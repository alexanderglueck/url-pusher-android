package com.alexanderglueck.urlpusher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.alexanderglueck.urlpusher.responses.RemoveTokenResponse;
import com.alexanderglueck.urlpusher.responses.SessionResponse;

import retrofit2.Call;
import retrofit2.Callback;

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
    private SessionResponse sessionResponse;


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

        Button login = findViewById(R.id.login);

        //Launch Registration screen when Register Button is clicked

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

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SessionResponse> call = apiService.login(username, password);
        call.enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, retrofit2.Response<SessionResponse> response) {
                sessionResponse = response.body();
                Log.d("TAG", "Response = " + sessionResponse.getName() + sessionResponse.getId() + sessionResponse.getApiToken());

                boolean openDeviceSelection = false;
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE);

                if (sessionResponse.getId() != sharedPreferences.getInt(Constants.LAST_SIGNED_IN_USER_ID, 0)) {
                    Log.d(TAG, "anderer user");

                    // anderer user hat sich eingeloggt als zuvor

                    // die jetzt eingeloggte id als zuletzte wegspeichern
                    sharedPreferences.edit().putInt(Constants.LAST_SIGNED_IN_USER_ID, sessionResponse.getId()).commit();

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

                session.loginUser(sessionResponse.getId(), username, sessionResponse.getName(), sessionResponse.getApiToken());

                pDialog.dismiss();

                if (openDeviceSelection) {
                    openDeviceSelectionActivity();
                } else {
                    loadDashboard();
                }

            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Log.d("TAG", "Response = " + t.toString());
                pDialog.dismiss();
            }
        });
    }

    private void openDeviceSelectionActivity() {
        Intent i = new Intent(getApplicationContext(), SelectDeviceActivity.class);

        startActivity(i);
        finish();

    }

    private void deleteToken(String token) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RemoveTokenResponse> call = apiService.removeToken("Bearer " + sessionResponse.getApiToken(), token);
        call.enqueue(new Callback<RemoveTokenResponse>() {
            @Override
            public void onResponse(Call<RemoveTokenResponse> call, retrofit2.Response<RemoveTokenResponse> response) {
                Log.d(TAG, "token deleted");
            }

            @Override
            public void onFailure(Call<RemoveTokenResponse> call, Throwable t) {
                Log.d("TAG", "Response = " + t.toString());
            }
        });
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
