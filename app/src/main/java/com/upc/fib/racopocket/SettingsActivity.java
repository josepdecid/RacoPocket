package com.upc.fib.racopocket;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.upc.fib.racopocket.Utils.PreferencesUtils;

import java.util.Locale;

public class SettingsActivity extends PreferenceActivity
{
    private static Context appContext, context;
    Fragment preferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context = this;
        appContext = getApplicationContext();
        preferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(SettingsActivity.this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("language")) {
                        String localeCode = PreferencesUtils.recoverPreference(appContext, "language");
                        Locale locale = new Locale(localeCode);
                        Locale.setDefault(locale);
                        Configuration configuration = new Configuration();
                        configuration.locale = locale;
                        appContext.getResources().updateConfiguration(configuration, appContext.getResources().getDisplayMetrics());

                        //TODO: Intent to same activity to refresh language
//                        Intent intent = new Intent(SettingsActivity.context, SettingsActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                        startActivity(intent);
                    } else if (key.equals("GitHubReport")) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")));
                    }
                }
            });
        }

    }

}