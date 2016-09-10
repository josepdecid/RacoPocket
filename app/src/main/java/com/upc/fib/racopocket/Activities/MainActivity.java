package com.upc.fib.racopocket.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.LocaleUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocaleUtils localeUtils = new LocaleUtils(getApplicationContext());
        localeUtils.setLocale();

        final Intent intent = nextActivity();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }, 2000);
    }

    private Intent nextActivity() {
        if (PreferencesUtils.recoverStringPreference(getApplicationContext(), "LOGIN_SUCCESSFUL").equals("OK")) {
            return new Intent(MainActivity.this, MainMenuActivity.class);
        } else {
            return new Intent(MainActivity.this, LoginActivity.class);
        }
    }

}
