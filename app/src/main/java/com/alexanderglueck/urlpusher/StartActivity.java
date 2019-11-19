package com.alexanderglueck.urlpusher;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT));
            }
        }

        checkLogin();
    }

    private void checkLogin() {
        SessionHandler preferencesHelper = new SessionHandler(getApplicationContext());
        Intent activityIntent;

        if (preferencesHelper.isLoggedIn()) {
            Log.d(TAG, "in start, eingeloggt");

            activityIntent = new Intent(this, MainActivity.class);

            if (getIntent().getExtras() != null) {

                Log.d(TAG, "hat extras");

                for (String key : getIntent().getExtras().keySet()) {
                    Object value = getIntent().getExtras().get(key);
                    Log.d(TAG, "Key: " + key + " Value: " + value);

                    if (key.equals(Constants.INTENT_EXTRA_NOTIFICATION)) {
                        // notification received
                        // keep extras for main activity

                        // resend it to main activity
                        activityIntent.putExtra(Constants.INTENT_EXTRA_NOTIFICATION, getIntent().getExtras().getSerializable(Constants.INTENT_EXTRA_NOTIFICATION));
                    }
                }
            } else {
                Log.d(TAG, "hat keine extras ");
            }

        } else {
            Log.d(TAG, "in start, nicht eingeloggt");
            if (getIntent().getExtras() != null) {
                Log.d(TAG, "nicht eingeloggt aber notification clicked, derzeit discarded. nicht sagbar ob offen oder zu received");


            }
            activityIntent = new Intent(this, LoginActivity.class);
        }


        startActivity(activityIntent);
        finish();
    }
}
