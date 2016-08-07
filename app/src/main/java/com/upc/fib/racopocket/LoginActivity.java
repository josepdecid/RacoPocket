package com.upc.fib.racopocket;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity {

    Button signInButton;
    ProgressBar progressBar;

    // Oauth initialize with api keys and URLs
    OAuthProvider provider = new DefaultOAuthProvider(
            "https://raco.fib.upc.edu/oauth/request_token",
            "https://raco.fib.upc.edu/oauth/access_token",
            "https://raco.fib.upc.edu/oauth/protected/authorize");

    String callback = "raco://raco";
    DefaultOAuthConsumer consumer = new DefaultOAuthConsumer(
            "2222347f-468e-4167-8fab-a4aefac3db46",
            "675afff8-da2c-43fa-aefb-28d673b03091");

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
                /*Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);*/
                new AskForRequestTokenAsync().execute();
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
        //recoverTokens();

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
                storeTokens(authURL);
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
                //storeTokens(true);
            } catch (Exception e) {
                Log.d("OAUTH", "Access token failed");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
            String requestToken = sharedPreferences.getString("request_token", "");
            String accessToken = "asd";
            consumer.setTokenWithSecret(requestToken, accessToken);
            progressBar.setVisibility(View.GONE);
        }

    }

    private void storeTokens(String authUrl) {
        SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("request_token", authUrl);
        editor.apply();
    }



}

