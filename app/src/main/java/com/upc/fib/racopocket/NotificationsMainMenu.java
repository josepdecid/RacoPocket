package com.upc.fib.racopocket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class NotificationsMainMenu extends Fragment
{
    TextView lastUpdate;
    ImageButton update;
    ImageView connectionProblem;
    ListView listView;
    ProgressBar progressBar;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_notifications));
        return inflater.inflate(R.layout.notifications_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastUpdate = (TextView) view.findViewById(R.id.lastUpdate);
        update = (ImageButton) view.findViewById(R.id.update);
        connectionProblem = (ImageView) view.findViewById(R.id.connection);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        listView = (ListView) view.findViewById(R.id.listView);

        String token = TokensStorageHelpers.recoverTokens(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = TokensStorageHelpers.recoverTokens(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileHelpers.fileDelete(getContext().getApplicationContext(), "places-lliures.json");
                new GetNotifications().execute();
            }
        });

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
            if (!FileHelpers.fileExists(getContext().getApplicationContext(), "avisos.json")) {
                FileHelpers.fetchAndStoreJSONFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/avisos.json", "avisos.json");
            }
            return FileHelpers.readFileToString(getContext().getApplicationContext(), "avisos.json");
        }

        @Override
        protected void onPostExecute(String response) {
            String mySubjects = FileHelpers.readFileToString(getContext().getApplicationContext(), "assignatures.json");
            List<String> subjectsId = new ArrayList<>();
            try {
                JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
                for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                    JSONObject subjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                    subjectsId.add(subjectJSONObject.getString("idAssig"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject subjectsNotifications = new JSONObject(response);
                for (int i = 0; i < subjectsId.size(); i++) {
                    // Each subject
                    JSONArray iSubjectNotifications = subjectsNotifications.getJSONArray(subjectsId.get(i));
                    //final ArrayList<String> specificSubjectNotificationsArray = new ArrayList<>();
                    for (int j = 0; j < iSubjectNotifications.length(); j++) {
                        // Each subject notifications
                        // specificSubjectNotificationsArray.add(iSubjectNotifications.get(j));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Set and display the Subjects Array
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, subjectsId);
            listView.setAdapter(adapter);

            progressBar.setVisibility(View.GONE);
        }

    }

}