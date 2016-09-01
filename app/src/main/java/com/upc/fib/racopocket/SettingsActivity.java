package com.upc.fib.racopocket;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.View;


public class SettingsActivity extends PreferenceActivity
{

    ListPreference language;
    CheckBoxPreference automaticUpdate;
    Preference networkUsage, updateData, email, github;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        public void onSharedPreferenceChanged(String key) {
            switch (key) {
                case "enableAutomaticUpdates":
                    

            }
        }

    }

}