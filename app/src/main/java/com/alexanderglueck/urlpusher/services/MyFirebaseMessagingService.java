package com.alexanderglueck.urlpusher.services;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alexanderglueck.urlpusher.Constants;
import com.alexanderglueck.urlpusher.LoginActivity;
import com.alexanderglueck.urlpusher.MainActivity;
import com.alexanderglueck.urlpusher.Notification;
import com.alexanderglueck.urlpusher.R;
import com.alexanderglueck.urlpusher.SessionHandler;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
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
            SessionHandler helper = new SessionHandler(getApplicationContext());

            Notification notification = new Notification();
            notification.setTitle(data.get(Constants.NOTIFICATION_URL_KEY));
            notification.setUrl(data.get(Constants.NOTIFICATION_URL_KEY));
            notification.setUserId(Integer.parseInt("" + data.get(Constants.NOTIFICATION_USER_ID_KEY)));

            String title;

            if (helper.isLoggedIn()) {
                if (notification.getUserId() != helper.getUserDetails().getId()) {
                    // do not process notification for another user. dont even show notification
                    return;
                }
                // logged in, url can be shown
                // and logged in user is notification recipient
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_NOTIFICATION_RECEIVED);
                intent.putExtra(Constants.INTENT_EXTRA_NOTIFICATION, notification);
                //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                title = data.get(Constants.NOTIFICATION_URL_KEY);

                if (applicationInForeground()) {
                    // eingeloggt und offen
                    Log.d(TAG, "eingeloggt und offen");

                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                } else {
                    Log.d(TAG, "eingeloggt und zu");

                    // eingeloggt und zu
                    sendNotification(getString(R.string.app_name), title, notification, true);
                }

            } else {
                title = "Sign in to access pushed URL";
                // not logged in, hide url
                notification.setTitle(title);

                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_NOTIFICATION_RECEIVED);
                intent.putExtra(Constants.INTENT_EXTRA_NOTIFICATION, notification);
                // LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


                if (applicationInForeground()) {
                    // nicht eingeloggt und offen
                    // kann nur sein wenn in login oder register maske
                    // wenn jetzt eine notification reinkommt machen wir trotzdem eine notification
                    Log.d(TAG, "ausgeloggt und offen");

                    sendNotification(getString(R.string.new_incoming_link), title, notification, false);

                } else {
                    Log.d(TAG, "ausgeloggt und zu");

                    // nicht eingeloggt und zu
                    sendNotification(getString(R.string.new_incoming_link), title, notification, false);
                }
            }


        }
    }

    private boolean applicationInForeground() {
        boolean isActivityFound = false;

        ActivityManager activityManager2 = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager2.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                isActivityFound = true;
            }
        }

        return isActivityFound;
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

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE);

        sharedPreferences.edit().putString(Constants.FCM_TOKEN, token).commit();

        SessionHandler helper = new SessionHandler(getApplicationContext());

        if (helper.isLoggedIn()) {
            // update current connected device with new token
            // update device on server
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageTitle, String messageBody, Notification notification, boolean signedIn) {
        Intent intent;

        if (signedIn) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.putExtra(Constants.INTENT_EXTRA_NOTIFICATION, notification);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
