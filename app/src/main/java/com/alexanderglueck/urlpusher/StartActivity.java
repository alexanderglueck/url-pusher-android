package com.alexanderglueck.urlpusher;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

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

            Intent intent = getIntent();

            if (intent.getExtras() != null) {
                Log.d(TAG, "per extra erhalten, dh von zu ");


                String action = intent.getAction();
                String type = intent.getType();

                if (Intent.ACTION_SEND.equals(action) && type != null) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (sharedText != null) {
                        if (Patterns.WEB_URL.matcher(sharedText).matches()) {
                            activityIntent.putExtra(Constants.INTENT_EXTRA_SHARE, sharedText);
                        }
                    }
                } else {

                    Log.d(TAG, "hat extras");

                    for (String key : intent.getExtras().keySet()) {
                        Object value = intent.getExtras().get(key);
                        Log.d(TAG, "Key: " + key + " Value: " + value);

                        if (key.equals(Constants.INTENT_EXTRA_NOTIFICATION)) {
                            // notification received
                            // keep extras for main activity

                            // resend it to main activity
                            activityIntent.putExtra(Constants.INTENT_EXTRA_NOTIFICATION, intent.getExtras().getSerializable(Constants.INTENT_EXTRA_NOTIFICATION));
                        }
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
