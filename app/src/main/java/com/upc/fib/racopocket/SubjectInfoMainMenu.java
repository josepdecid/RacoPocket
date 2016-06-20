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

public class SubjectInfoMainMenu extends Fragment
{
    EditText subjectSelector;
    Button buttonThread;
    TextView responseView;
    ProgressBar progressBar;
    static final String API_KEY = "2222347f-468e-4167-8fab-a4aefac3db46";
    static final String API_URL = "http://raco.fib.upc.edu";

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
                new MyAsyncTask().execute(80);
            }
        });

        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);
    }


    //AsyncTask<Params, Progress, Result>
    public class MyAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            int max = params[0];

            for (int i = 0; i < max; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                publishProgress(i);
            }

            return "Finish";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int count = values[0];
            String text = "Count " + count;
            responseView.setText(text);
            responseView.setTextSize(count);

            progressBar.setProgress(count);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            responseView.append("\n" + s);
        }
    }


}
