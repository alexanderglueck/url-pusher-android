package com.alexanderglueck.urlpusher.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alexanderglueck.urlpusher.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(@NonNull String token) {
        sendRegistrationToServer(token);
    }

    /**
     * Called when message is received. Only if the app is in the foreground. If the app is in the
     * background or not running at all, a default notification is created. A click on this default
     * notification is then handled in the MainActivity.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleMessage(Map<String, String> data) {
        if (data.containsKey(Constants.NOTIFICATION_URL_KEY)) {
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_NOTIFICATION_RECEIVED);
            intent.putExtra(Constants.INTENT_EXTRA_URL, data.get(Constants.NOTIFICATION_URL_KEY));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * Persist token to third-party servers.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO:  Send to server if the user is already logged in and we have a device_id to update.
        //  Keep on device until a user signs in and we can create a new device with this token or update an existing one.

        Log.d(TAG, "Refreshed token: " + token);
    }
}
