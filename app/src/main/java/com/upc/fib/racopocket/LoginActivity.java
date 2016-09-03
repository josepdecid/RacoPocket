package com.upc.fib.racopocket;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.app.Activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity
{
    Button signInButton;
    ProgressBar progressBar;

    boolean workInProgress;

    // Consumer object with the Consumer key and Consumer Secret
    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
    // OauthProvider initialize with URLs
    OAuthProvider provider = new DefaultOAuthProvider(Constants.REQUEST_URL, Constants.ACCESS_URL, Constants.AUTHORIZE_URL);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = (Button) findViewById(R.id.btn_login);
        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);

        workInProgress = false;

        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorLogo), PorterDuff.Mode.MULTIPLY);

        signInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!workInProgress) {
                    new AskForRequestTokenAsync().execute();
                }
            }
        });
    }

    // When back from authorizing requestToken, recover them and start accessToken exchange
    public void onResume()
    {
        super.onResume();
        Uri uri = this.getIntent().getData();

        String token = PreferencesUtils.recoverPreference(getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverPreference(getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        // This is the case when we receive a token
        if (uri != null && uri.toString().startsWith(Constants.CALLBACK)) {
            Log.i("OAuth", "Callback received : " + uri);
            Log.i("OAuth", "Retrieving Access Token");
            new AskForAccessTokenAsync().execute();
        }
    }

    private class AskForRequestTokenAsync extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            workInProgress = true;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try {
                Log.i("OAuth", "Retrieving request token from Raco servers");
                String authURL = provider.retrieveRequestToken(consumer, Constants.CALLBACK);
                PreferencesUtils.storePreference(getApplicationContext(), consumer.getToken(), consumer.getTokenSecret());
                Log.i("OAuth", "Popping a browser with the authorize URL : " + authURL);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
            } catch (Exception e) {
                Log.d("OAuth", "Request token failed");
                Toast.makeText(LoginActivity.this, "Something went wrong, try it again", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void response)
        {
            workInProgress = false;
            progressBar.setVisibility(View.GONE);
        }

    }

    private class AskForAccessTokenAsync extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            workInProgress = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                provider.retrieveAccessToken(consumer, null);
                PreferencesUtils.storePreference(getApplicationContext(), consumer.getToken(), consumer.getTokenSecret());
            } catch (Exception e) {
                Log.d("OAuth", "Access token failed");
                Toast.makeText(LoginActivity.this, "Something went wrong, try it again", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void response)
        {
            progressBar.setVisibility(View.GONE);
            workInProgress = false;
            new GetStudentInfo().execute();
        }
    }

    private class GetStudentInfo extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            workInProgress = true;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            // Personal Data
            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/info-personal.json", "info-personal.json");
            // Personal Subjects
            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/assignatures.json", "assignatures.json");
            // Timetable Data
            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/horari-setmanal.json", "horari-setmanal.json");
            // Notifications Data
            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/avisos.json", "avisos.json");
            // Schedule Data
            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/calendari-portada.ics", "calendari-portada.ics");
            // Class Availability Data
            FileUtils.fetchAndStoreFile(getApplicationContext(), null, "https://raco.fib.upc.edu/api/aules/places-lliures.json", "places-lliures.json");
            // Subjects List
            FileUtils.fetchAndStoreFile(getApplicationContext(), null, "https://raco.fib.upc.edu/api/assignatures/llista.json", "llista.json");
            // Subjects Data
            String subjects = FileUtils.readFileToString(getApplicationContext(), "llista.json");
            try {
                JSONArray subjectsJSONArray = new JSONArray(subjects);
                if (subjectsJSONArray != null) {
                    for (int i = 0; i < subjectsJSONArray.length(); i++) {
                        JSONObject subjectJSONObject = subjectsJSONArray.getJSONObject(i);
                        String subjectCode = subjectJSONObject.getString("codi_upc");
                        String filename = "subject_" + subjectCode + ".json";
                        if (!FileUtils.fileExists(getApplicationContext(), filename)) {
                            FileUtils.fetchAndStoreFile(getApplicationContext(), null, "https://raco.fib.upc.edu/api/assignatures/info.json?codi_upc=" + subjectCode, filename);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void response)
        {
            workInProgress = false;
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

}

