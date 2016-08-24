package com.upc.fib.racopocket;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {

    Button signInButton;
    ProgressBar progressBar;

    // Consumer object with the Consumer key and Consumer Secret
    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    // Oauth initialize with URLs
    OAuthProvider provider = new DefaultOAuthProvider(Constants.REQUEST_URL, Constants.ACCESS_URL, Constants.AUTHORIZE_URL);
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);

        signInButton = (Button) findViewById(R.id.btn_login);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AskForRequestTokenAsync().execute();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // Don't close immediately on back pressed
    private Boolean exit = false;

    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);
        }
    }

    public void onResume() {
        super.onResume();
        Uri uri = this.getIntent().getData();

        String token = TokensStorage.recoverTokens(getApplicationContext(), "OAUTH_TOKEN");
        String secret = TokensStorage.recoverTokens(getApplicationContext(), "OAUTH_TOKEN_SECRET");

        consumer.setTokenWithSecret(token, secret);

        // This is the case when we receive token
        if (uri != null && uri.toString().startsWith(Constants.CALLBACK)) {
            Log.i("OAuth", "Callback received : " + uri);
            Log.i("OAuth", "Retrieving Access Token");
            new AskForAccessTokenAsync().execute();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://raco/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.upc.fib.racopocket/raco/raco/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://raco/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.upc.fib.racopocket/raco/raco/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class AskForRequestTokenAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Log.i("OAuth", "Retrieving request token from Raco servers");
                String authURL = provider.retrieveRequestToken(consumer, Constants.CALLBACK);
                TokensStorage.storeTokens(getApplicationContext(), consumer.getToken(), consumer.getTokenSecret());
                Log.i("OAuth", "Popping a browser with the authorize URL : " + authURL);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "Failed consumer or callback data", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            progressBar.setVisibility(View.GONE);
        }

    }

    private class AskForAccessTokenAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                provider.retrieveAccessToken(consumer, null);
                TokensStorage.storeTokens(getApplicationContext(), consumer.getToken(), consumer.getTokenSecret());

                String token = TokensStorage.recoverTokens(getApplicationContext(), "OAUTH_TOKEN");
                String secret = TokensStorage.recoverTokens(getApplicationContext(), "OAUTH_TOKEN_SECRET");

                consumer.setTokenWithSecret(token, secret);
                Log.i("OAuth", "OAUTH STAGE TWO OK!");
            } catch (Exception e) {
                Log.d("OAuth", "Access token failed");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            progressBar.setVisibility(View.GONE);
            new GetStudentInfo().execute();
        }

    }

    private class GetStudentInfo extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            StringBuilder stringBuilder = new StringBuilder();
            try {
                URL url = new URL("https://raco.fib.upc.edu/api-v1/info-personal.json");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                consumer.sign(urlConnection);
                try {
                    urlConnection.connect();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }
                    bufferedReader.close();

                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.i("OAuth", "" + e.getMessage());
                return null;
            }

            JSONObject jObject = null;
            try {
                jObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                Toast.makeText(LoginActivity.this, "Json null!", Toast.LENGTH_SHORT).show();
            }
            if (jObject != null) {
                try {
                    return jObject.getString("nom");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
            intent.putExtra("name", response);
            startActivity(intent);
        }

    }

}

