package com.alexanderglueck.urlpusher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alexanderglueck.urlpusher.responses.AttachTokenResponse;
import com.alexanderglueck.urlpusher.responses.PushUrlResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SessionHandler session;

    private NotificationBroadcastReceiver mNotificationBroadcastReceiver = null;
    private IntentFilter mIntentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mNotificationBroadcastReceiver = new NotificationBroadcastReceiver(this);
        this.mIntentFilter = new IntentFilter(Constants.ACTION_NOTIFICATION_RECEIVED);

        Button logTokenButton = findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        InstanceIdResult result = task.getResult();

                        if (result != null) {
                            // Get new Instance ID token
                            String token = result.getToken();

                            // Log and toast
                            String msg = getString(R.string.msg_token_fmt, token);
                            Log.d(TAG, msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.no_token, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        session = new SessionHandler(getApplicationContext());
        User user = session.getUserDetails();
        TextView welcomeText = findViewById(R.id.welcomeText);

        welcomeText.setText("Welcome " + user.getFullName() + " (" + user.getId() + ")");

        Button logoutBtn = findViewById(R.id.btnLogout);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logoutUser();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();

            }
        });


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
                        if (notification.getUserId() == session.getUserDetails().getId()) {
                            // could have clicked notification because was signed out and signed in with different user
                            // then user that sent the notification
                            Log.d(TAG, "extra: notificaiton user" + notification.getUserId() + " / session user" + session.getUserDetails().getId());
                            notificationClicked(notification.getUrl());
                        }
                    }
                }

                if (key.equals(Constants.INTENT_EXTRA_SHARE)) {
                    // url was shared to app
                    Log.d(TAG, "url received: " + value);
                    pushUrl(getIntent().getStringExtra(Constants.INTENT_EXTRA_SHARE));
                }
            }
        }
    }

    private void notificationClicked(String url) {
        Log.d(TAG, "Notification clicked");
        Log.d(TAG, "URL: " + url);

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void pushUrl(String url) {
        session = new SessionHandler(getApplicationContext());
        User user = session.getUserDetails();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE);
        int lastSavedDeviceId = sharedPreferences.getInt(Constants.LAST_SIGNED_IN_DEVICE_ID, 0);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<PushUrlResponse> pushUrlCall = apiService.pushUrl("Bearer " + user.getApiToken(), lastSavedDeviceId, url);
        pushUrlCall.enqueue(new Callback<PushUrlResponse>() {
            @Override
            public void onResponse(Call<PushUrlResponse> call, Response<PushUrlResponse> response) {
                Toast.makeText(MainActivity.this, "URL pushed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<PushUrlResponse> call, Throwable t) {
                Log.d("TAG", "Response = " + t.toString());
                Toast.makeText(MainActivity.this, "URL pushed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mNotificationBroadcastReceiver, this.mIntentFilter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationBroadcastReceiver);
        super.onPause();
    }

    private class NotificationBroadcastReceiver extends BroadcastReceiver {

        WeakReference<MainActivity> mMainActivity;

        public NotificationBroadcastReceiver(MainActivity mainActivity) {
            this.mMainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity mainActivity = mMainActivity.get();

            Log.d(TAG, "per receiver erhalten, dh eingeloggt und offen");

            if (mainActivity != null) {
                Bundle extras = intent.getExtras();
                if (extras != null && extras.containsKey(Constants.INTENT_EXTRA_NOTIFICATION)) {

                    Notification notification = (Notification) extras.getSerializable(Constants.INTENT_EXTRA_NOTIFICATION);

                    if (notification != null) {
                        if (notification.getUserId() == session.getUserDetails().getId()) {
                            // could have clicked notification because was signed out and signed in with different user
                            // then user that sent the notification
                            // here in the listener we should always be signed in with the correct user
                            // however, if app open, and somehow old token still valid with new user, check anyway

                            Log.d(TAG, "receiver: notificaiton user" + notification.getUserId() + " / session user" + session.getUserDetails().getId());

                            mainActivity.notificationClicked(notification.getUrl());
                        }
                    }
                }
            }
        }
    }
}
