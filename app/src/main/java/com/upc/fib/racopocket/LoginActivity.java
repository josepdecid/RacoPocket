package com.upc.fib.racopocket;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {

    Button signInButton, nom;
    ProgressBar progressBar;

    // Consumer object with the Consumer key and Consumer Secret
    OAuthConsumer consumer = new DefaultOAuthConsumer(
            "2222347f-468e-4167-8fab-a4aefac3db46",
            "675afff8-da2c-43fa-aefb-28d673b03091");

    // Oauth initialize with URLs
    OAuthProvider provider = new DefaultOAuthProvider(
            "https://raco.fib.upc.edu/oauth/request_token",
            "https://raco.fib.upc.edu/oauth/access_token",
            "https://raco.fib.upc.edu/oauth/protected/authorize");

    String callback = "raco://raco";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);

        nom = (Button) findViewById(R.id.name);

        signInButton = (Button) findViewById(R.id.btn_login);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AskForRequestTokenAsync().execute();
            }
        });

        nom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getNom();
            }
        });
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
        recoverTokens();

        // This is the case when we receive token
        if (uri != null && uri.toString().startsWith(callback)) {
            new AskForAccessTokenAsync().execute();
        }
    }

    private class AskForRequestTokenAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            String authURL = null;
            try {
                authURL = provider.retrieveRequestToken(consumer, callback);
                storeTokens();
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "Failed consumer or callback data", Toast.LENGTH_SHORT).show();
            }

            return authURL;
        }

        @Override
        protected void onPostExecute(String authURL) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
            progressBar.setVisibility(View.GONE);
        }

    }

    private class AskForAccessTokenAsync extends  AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                provider.retrieveAccessToken(consumer, null);
                storeTokens();
            } catch (Exception e) {
                Log.d("OAUTH", "Access token failed");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            recoverTokens();
            progressBar.setVisibility(View.GONE);
        }

    }

    private void storeTokens() {
        SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", consumer.getToken());
        editor.putString("token_secret", consumer.getTokenSecret());
        editor.apply();
    }

    private void recoverTokens() {
        SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        String token_secret = sharedPreferences.getString("token_secret", "");
        consumer.setTokenWithSecret(token, token_secret);
    }

    public void getNom() {
        String json = askFor("https://raco.fib.upc.edu/api-v1/info-personal.json");
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(json);
        } catch (JSONException e) {
            Toast.makeText(LoginActivity.this, "Json null!", Toast.LENGTH_SHORT).show();
        }
        if (jObject != null) {
            try {
                Toast.makeText(LoginActivity.this, jObject.getString("nom"), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String askFor(String url) {
        try {
            URL u =  new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
            consumer.sign(urlConnection);
            try {
                urlConnection.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                bufferedReader.close();

                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.i("oauth", "" + e.getMessage());
            return null;
        }
    }



}

