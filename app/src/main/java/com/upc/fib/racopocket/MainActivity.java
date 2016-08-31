package com.upc.fib.racopocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setLocale();
        final Intent intent = nextActivity();

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }, 2000);
    }

    // Set application language
    private void setLocale()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        final String localeCode = sharedPreferences.getString("language", "en");

        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.getResources().updateConfiguration(configuration, this.getResources().getDisplayMetrics());
    }

    // Intent to login or main menu according if you are already logged in or not
    private Intent nextActivity()
    {
        if (TokensStorageHelpers.recoverTokens(getApplicationContext(), "OAUTH_TOKEN").equals("") || TokensStorageHelpers.recoverTokens(getApplicationContext(), "OAUTH_TOKEN_SECRET").equals("")) {
            return new Intent(MainActivity.this, LoginActivity.class);
        } else {
            return new Intent(MainActivity.this, MainMenuActivity.class);
        }
    }

}
