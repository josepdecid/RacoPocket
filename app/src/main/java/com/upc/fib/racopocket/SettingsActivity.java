package com.upc.fib.racopocket;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.upc.fib.racopocket.Utils.PreferencesUtils;

import java.util.Locale;

public class SettingsActivity extends PreferenceActivity
{
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //TODO: Solve IllegalStateException error
        getFragmentManager().popBackStack();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        Preference networkUsage, emailButton, gitHubButton;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            networkUsage = findPreference("applicationUpdates");
            boolean automaticUpdate = PreferencesUtils.recoverBooleanPreference(getActivity().getApplicationContext(), "enableAutomaticUpdates");
            networkUsage.setEnabled(automaticUpdate);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
                {
                    if (key.equals("language")) {
                        String localeCode = PreferencesUtils.recoverStringPreference(getActivity().getApplicationContext(), "language");
                        Locale locale = new Locale(localeCode);
                        Locale.setDefault(locale);
                        Configuration configuration = new Configuration();
                        configuration.locale = locale;
                        getActivity().getApplicationContext().getResources().updateConfiguration(configuration, getActivity().getApplicationContext().getResources().getDisplayMetrics());
                        getActivity().recreate();
                    } else if (key.equals("enableAutomaticUpdates")) {
                        boolean enabled = PreferencesUtils.recoverBooleanPreference(getActivity().getApplicationContext(), "enableAutomaticUpdates");
                        networkUsage.setEnabled(enabled);
                    }
                }
            };
            preferences.registerOnSharedPreferenceChangeListener(listener);

            emailButton = findPreference("emailReport");
            emailButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "josep.de.cid@gmail.com", "pau.risa@gmail.com" });
                    intent.putExtra(Intent.EXTRA_SUBJECT, "[RacoPocket issue]");
                    startActivity(Intent.createChooser(intent, ""));
                    return false;
                }
            });

            gitHubButton = findPreference("gitHubReport");
            gitHubButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://github.com/JosepRivaille/RacoPocket/issues"));
                    startActivity(intent);
                    return false;
                }
            });

        }
    }
}