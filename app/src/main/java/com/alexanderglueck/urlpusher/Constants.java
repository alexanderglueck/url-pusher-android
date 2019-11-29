package com.alexanderglueck.urlpusher;

public class Constants {
    public static final String NOTIFICATION_URL_KEY = "url";

    public static final String ACTION_NOTIFICATION_RECEIVED = "com.alexanderglueck.urlpusher.NOTIFICATION_RECEIVED";

    public static final String SHARED_PREFERENCES_FILE = "settings";
    public static final String SHARED_PREFERENCES_API_TOKEN = "api_token";
    public static final String NOTIFICATION_USER_ID_KEY = "user_id";
    public static final String INTENT_EXTRA_NOTIFICATION = "notification";

    public static final String FCM_TOKEN = "fcm_token";

    public static final String LAST_SIGNED_IN_USER_ID = "last_user_id";
    public static final String LAST_SIGNED_IN_DEVICE_ID = "last_device_id";

    public static final String URL_LOGIN = "http://10.0.0.10:8080/api/session";
    public static final String URL_DESTROY_TOKEN = "http://10.0.0.10:8080/api/remove-token";
    public static final String URL_REGISTER = "http://10.0.0.10:8080/api/user";
    public static final String URL_FETCH_DEVICES = "http://10.0.0.10:8080/api/devices";

    public static final String BASE_URL = "http://10.0.0.10:8080/api/";
}
