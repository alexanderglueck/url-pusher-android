package com.alexanderglueck.urlpusher;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesHelper {

    private Context context;
    private SharedPreferences sharedPreferences;

    public PreferencesHelper(Context context) {
        this.context = context;

        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE);
    }

    public boolean hasApiToken() {
        return sharedPreferences.getString(Constants.SHARED_PREFERENCES_API_TOKEN, "").length() > 0;
    }

    public boolean setApiToken(String apiToken) {
        return sharedPreferences.edit().putString(Constants.SHARED_PREFERENCES_API_TOKEN, apiToken).commit();
    }
}
