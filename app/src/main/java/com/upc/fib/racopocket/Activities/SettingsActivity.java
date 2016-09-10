package com.upc.fib.racopocket.Activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.LocaleUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment fragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        Preference language, updateAll, emailButton, gitHubButton;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            language = findPreference("language");
            updateAll = findPreference("applicationDataUpdate");
            emailButton = findPreference("emailReport");
            gitHubButton = findPreference("gitHubReport");

            language.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.nav_language).setItems(R.array.languages_array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            LocaleUtils localeUtils = new LocaleUtils(getActivity().getApplicationContext());
                            switch (which) {
                                case 1:
                                    localeUtils.setAndStoreLocale("es");
                                    break;
                                case 2:
                                    localeUtils.setAndStoreLocale("en");
                                    break;
                                default:
                                    localeUtils.setAndStoreLocale("ca");
                            }
                            Toast.makeText(getActivity(), getResources().getString(R.string.new_language), Toast.LENGTH_LONG).show();
                            getActivity().recreate();
                        }
                    });
                    builder.show();
                    return false;
                }
            });

            updateAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.update_data));
                    builder.setMessage(getResources().getString(R.string.update_data_text));
                    builder.setPositiveButton(getResources().getString(R.string.continue_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new UpdateAllFiles().execute();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return false;
                }
            });

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

        private class UpdateAllFiles extends AsyncTask<Void, Integer, String> {

            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

            @Override
            protected void onPreExecute() {
                progressDialog.setTitle(R.string.update_data_title);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setIndeterminate(false);
                progressDialog.setCancelable(false);
                progressDialog.setProgress(0);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                String token = PreferencesUtils.recoverStringPreference(getActivity().getApplicationContext(), "OAUTH_TOKEN");
                String secret = PreferencesUtils.recoverStringPreference(getActivity().getApplicationContext(), "OAUTH_TOKEN_SECRET");
                consumer.setTokenWithSecret(token, secret);
                FileUtils fileUtils = new FileUtils(getActivity().getApplicationContext(), consumer);

                int currentProgress = 0;
                if (200 != fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api-v1/info-personal.json", "info-personal.json")) {
                    return "ERROR";
                }
                publishProgress(++currentProgress);
                if (200 != fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api-v1/assignatures.json", "assignatures.json")) {
                    return "ERROR";
                }
                publishProgress(++currentProgress);
                if (200 != fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api-v1/horari-setmanal.json", "horari-setmanal.json")) {
                    return "ERROR";
                }
                publishProgress(++currentProgress);
                if (200 != fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api/assignatures/llista.json", "llista.json")) {
                    return "ERROR";
                }
                publishProgress(++currentProgress);
                fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api-v1/avisos.json", "avisos.json");
                publishProgress(++currentProgress);
                fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api-v1/calendari-portada.ics", "calendari-portada.ics");
                publishProgress(++currentProgress);
                fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api/aules/places-lliures.json", "places-lliures.json");
                publishProgress(++currentProgress);

                String subjects = fileUtils.readFileToString("llista.json");
                try {
                    JSONArray subjectsJSONArray = new JSONArray(subjects);
                    for (int i = 0; i < subjectsJSONArray.length(); i++) {
                        JSONObject subjectJSONObject = subjectsJSONArray.getJSONObject(i);
                        String subjectCode = subjectJSONObject.getString("codi_upc");
                        String filename = "subject_" + subjectCode + ".json";
                        if (200 != fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api/assignatures/info.json?codi_upc=" + subjectCode, filename)) {
                            fileUtils.deleteFile(filename);
                            return "ERROR";
                        }
                        publishProgress(++currentProgress);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                publishProgress(100);
                return "OK";
            }

            @Override
            public void onProgressUpdate(Integer... args) {
                progressDialog.setProgress(args[0]);
            }

            @Override
            protected void onPostExecute(String response) {
                progressDialog.dismiss();
                if (response.equals("ERROR")) {
                    Toast.makeText(getActivity(), R.string.update_data_unsuccessful, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.update_data_successful, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

}