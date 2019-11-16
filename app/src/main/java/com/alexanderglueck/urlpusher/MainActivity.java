package com.alexanderglueck.urlpusher;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.lang.ref.WeakReference;

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

        welcomeText.setText("Welcome " + user.getFullName());

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
    }

    private void notificationClicked(String url) {
        Log.d(TAG, "Notification clicked");
        Log.d(TAG, "URL: " + url);

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setData(Uri.parse(url));
        startActivity(i);
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

            if (mainActivity != null) {
                Bundle extras = intent.getExtras();
                if (extras != null && extras.containsKey(Constants.INTENT_EXTRA_URL)) {
                    mainActivity.notificationClicked(extras.getString(Constants.INTENT_EXTRA_URL));
                }
            }
        }
    }
}
