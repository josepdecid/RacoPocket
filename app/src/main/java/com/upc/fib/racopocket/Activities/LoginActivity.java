package com.upc.fib.racopocket.Activities;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity
{
    Button loginButton;
    ProgressBar progressBar;

    Boolean workInProgress;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
    OAuthProvider provider = new DefaultOAuthProvider(Constants.REQUEST_URL, Constants.ACCESS_URL, Constants.AUTHORIZE_URL);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        workInProgress = false;

        loginButton = (Button) findViewById(R.id.buttonLogin);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (!workInProgress)
                    new AskForRequestTokenAsync().execute();
            }
        });
    }

    // When back from authorizing requestToken, recover them and start accessToken exchange
    public void onResume()
    {
        super.onResume();
        Uri uri = this.getIntent().getData();

        String token = PreferencesUtils.recoverStringPreference(getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverStringPreference(getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        // This is the case when we receive a token
        if (uri != null && uri.toString().startsWith(Constants.CALLBACK)) {
            Log.i("OAuth", "Callback received : " + uri);
            Log.i("OAuth", "Retrieving Access Token");
            new AskForAccessTokenAsync().execute();
        }
    }

    private class AskForRequestTokenAsync extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            workInProgress = true;
        }

        @Override
        protected String doInBackground(Void... params)
        {
            try {
                Log.i("OAuth", "Retrieving request token from Raco servers");
                String authURL = provider.retrieveRequestToken(consumer, Constants.CALLBACK);
                PreferencesUtils.storeTokens(getApplicationContext(), consumer.getToken(), consumer.getTokenSecret());
                Log.i("OAuth", "Popping a browser with the authorize URL : " + authURL);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
            } catch (Exception e) {
                Log.d("OAuth", "Request token failed");
                return "ERROR";
            }

            return "OK";
        }

        @Override
        protected void onPostExecute(String response)
        {
            if (response.equals("ERROR"))
                Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
            workInProgress = false;
        }

    }

    private class AskForAccessTokenAsync extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            workInProgress = true;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try {
                provider.retrieveAccessToken(consumer, null);
                PreferencesUtils.storeTokens(getApplicationContext(), consumer.getToken(), consumer.getTokenSecret());
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
            new GetStudentInfo().execute();
        }
    }

    private class GetStudentInfo extends AsyncTask<Void, Integer, String>
    {
        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute()
        {
            progressDialog.setTitle(R.string.login_title);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params)
        {
            int currentProgress = 0;

            if (200 != FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/info-personal.json", "info-personal.json"))
                return "ERROR";
            publishProgress(++currentProgress);
            if (200 != FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/assignatures.json", "assignatures.json"))
                return "ERROR";
            publishProgress(++currentProgress);
            if (200 != FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/horari-setmanal.json", "horari-setmanal.json"))
                return "ERROR";
            publishProgress(++currentProgress);
            if (200 != FileUtils.fetchAndStoreFile(getApplicationContext(), null, "https://raco.fib.upc.edu/api/assignatures/llista.json", "llista.json"))
                return "ERROR";
            publishProgress(++currentProgress);

            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/avisos.json", "avisos.json");
            publishProgress(++currentProgress);
            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/calendari-portada.ics", "calendari-portada.ics");
            publishProgress(++currentProgress);
            FileUtils.fetchAndStoreFile(getApplicationContext(), null, "https://raco.fib.upc.edu/api/aules/places-lliures.json", "places-lliures.json");
            publishProgress(++currentProgress);

            String subjects = FileUtils.readFileToString(getApplicationContext(), "llista.json");
            try {
                JSONArray subjectsJSONArray = new JSONArray(subjects);
                for (int i = 0; i < subjectsJSONArray.length(); i++) {
                    JSONObject subjectJSONObject = subjectsJSONArray.getJSONObject(i);
                    String subjectCode = subjectJSONObject.getString("codi_upc");
                    String filename = "subject_" + subjectCode + ".json";
                    if (!FileUtils.checkFileExists(getApplicationContext(), filename)) {
                        if (200 != FileUtils.fetchAndStoreFile(getApplicationContext(), null, "https://raco.fib.upc.edu/api/assignatures/info.json?codi_upc=" + subjectCode, filename)) {
                            FileUtils.deleteFile(getApplicationContext(), filename);
                            return "ERROR";
                        }
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
        public void onProgressUpdate(Integer... args)
        {
            progressDialog.setProgress(args[0]);
        }

        @Override
        protected void onPostExecute(String response)
        {
            if (response.equals("ERROR")) {
                Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                PreferencesUtils.removeTokens(getApplicationContext());
                FileUtils.deleteFile(getApplicationContext(), "info-personal.json");
                FileUtils.deleteFile(getApplicationContext(), "assignatures.json");
                FileUtils.deleteFile(getApplicationContext(), "horari-setmanal.json");
                FileUtils.deleteFile(getApplicationContext(), "llista.json");
            } else {
                Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
            workInProgress = false;
            progressDialog.dismiss();
        }
    }

}

