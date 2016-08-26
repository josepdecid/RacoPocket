package com.upc.fib.racopocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get and set app language, english by default
        SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        final String language = sharedPreferences.getString("language", "en");
        Log.d("LANG_SET", language);
        setLocale(language);

        final Intent intent;
        // If tokens don't exist, go to Login, else go to MainMenu
        if (TokensStorageHelpers.recoverTokens(getApplicationContext(), "OAUTH_TOKEN").equals("") || TokensStorageHelpers.recoverTokens(getApplicationContext(), "OAUTH_TOKEN_SECRET").equals("")) {
            intent = new Intent(MainActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(MainActivity.this, MainMenuActivity.class);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }, 1000);
    }

    private void setLocale(String localeCode) {
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.getResources().updateConfiguration(configuration, this.getResources().getDisplayMetrics());
    }

}
