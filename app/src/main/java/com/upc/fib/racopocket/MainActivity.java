package com.upc.fib.racopocket;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.upc.fib.racopocket.Utils.PreferencesUtils;

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
        String localeCode = PreferencesUtils.recoverPreference(getApplicationContext(), "language");
        if (localeCode.equals(""))
            localeCode = "ca";

        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.getResources().updateConfiguration(configuration, this.getResources().getDisplayMetrics());
    }

    // Intent to login or main menu according if you are already logged in or not
    private Intent nextActivity()
    {
        if (PreferencesUtils.recoverPreference(getApplicationContext(), "OAUTH_TOKEN").equals("") || PreferencesUtils.recoverPreference(getApplicationContext(), "OAUTH_TOKEN_SECRET").equals("")) {
            return new Intent(MainActivity.this, LoginActivity.class);
        } else {
            return new Intent(MainActivity.this, MainMenuActivity.class);
        }
    }

}
