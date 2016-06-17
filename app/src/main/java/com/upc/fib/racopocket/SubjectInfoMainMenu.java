package com.upc.fib.racopocket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubjectInfoMainMenu extends Fragment
{
    EditText subjectSelector;
    TextView responseView;
    ProgressBar progressBar;
    static final String API_KEY = "2222347f-468e-4167-8fab-a4aefac3db46";
    static final String API_URL = "http://raco.fib.upc.edu";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.subject_info_main_menu, container, false);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        subjectSelector = (EditText) getView().findViewById(R.id.subjectSelector);
        responseView = (TextView) getView().findViewById(R.id.responseView);
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);

        Button queryButton = (Button) getView().findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RetrieveFeedTask().execute();
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        private String subjectCode = subjectSelector.getText().toString();

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        protected String doInBackground(Void... urls) {

            try {
                URL url = new URL(API_URL + "api/assignatures/info.json?codi_upc=" + subjectCode + "&apiKey=" + API_KEY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) !=  null) {
                        stringBuilder.append(line).append('\n');
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally {
                    urlConnection.disconnect();
                }
            }
            catch (Exception e) {
                Log.e("Error", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "There was an error!";
            } progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            responseView.setText(response);
        }
    }

}
