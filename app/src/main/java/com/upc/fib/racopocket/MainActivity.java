package com.upc.fib.racopocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        final String language = sharedPreferences.getString("language", "en");
        Log.d("LANG_SET", language);
        setLocale(language);

        final Intent intent;
        if (TokensStorage.recoverTokens(getApplicationContext(), "OAUTH_TOKEN").equals("")
                || TokensStorage.recoverTokens(getApplicationContext(), "OAUTH_TOKEN_SECRET").equals("")) {
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
        }, 2000);
    }

    private void setLocale(String localeCode) {
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.getResources().updateConfiguration(configuration, this.getResources().getDisplayMetrics());
    }

}
