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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SubjectInfoMainMenu extends Fragment
{
    EditText subjectSelector;
    Button buttonThread;
    TextView responseView;
    ProgressBar progressBar;
    static final String API_CUSTOMER_KEY = "2222347f-468e-4167-8fab-a4aefac3db46";
    static final String API_SECRET_KEY = "675afff8-da2c-43fa-aefb-28d673b03091";
    static final String API_URL = "https://raco.fib.upc.edu/api/assignatures/info.json?codi_upc=";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.subject_info_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subjectSelector = (EditText) view.findViewById(R.id.subjectSelector);
        buttonThread = (Button) view.findViewById(R.id.queryButton);
        responseView = (TextView) view.findViewById(R.id.responseView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        buttonThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectCode = subjectSelector.getText().toString();
                new HttpAsyncTask().execute(subjectCode);
            }
        });

        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);
    }



    //AsyncTask<Params, Progress, Result>
    public class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... codes) {
            String subjectCode = codes[0];

            try {
                URL url = new URL(API_URL + subjectCode);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response == null) {
                response = "Unable to connect";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            responseView.setText(response);
        }
    }


}
