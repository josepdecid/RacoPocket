package com.upc.fib.racopocket;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class NotificationsMainMenu extends Fragment
{
    TextView notifications;
    ProgressBar progressBar;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_notifications));
        return inflater.inflate(R.layout.notifications_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notifications = (TextView) view.findViewById(R.id.notifications);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        String token = TokensStorageHelpers.recoverTokens(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = TokensStorageHelpers.recoverTokens(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        new GetNotifications().execute();
    }

    private class GetNotifications extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            return FileHelpers.fetchDirectlyJSON(consumer, "https://raco.fib.upc.edu/api-v1/avisos.json");

        }

        @Override
        protected void onPostExecute(String response) {
            notifications.setText(response);
            progressBar.setVisibility(View.GONE);
        }

    }

}