package com.alexanderglueck.urlpusher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


public class StartActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLogin();
    }

    private void checkLogin() {
        PreferencesHelper preferencesHelper = new PreferencesHelper(this);
        Intent activityIntent;

        if (preferencesHelper.hasApiToken()) {
            activityIntent = new Intent(this, MainActivity.class);
        } else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }
}
