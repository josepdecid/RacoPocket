package com.upc.fib.racopocket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubjectInfoMainMenu extends Fragment
{
    EditText subjectSelector;
    Button buttonThread;
    TextView subjectName, subjectData;
    ProgressBar progressBar;
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

        subjectName = (TextView) view.findViewById(R.id.subjectName);
        subjectData = (TextView) view.findViewById(R.id.subjectData);

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
            subjectName.setText("\n");
            subjectData.setText("\n");
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

            progressBar.setVisibility(View.GONE);

            if (response == null) {
                subjectName.setText(getResources().getString(R.string.connection_problems));
            }
            else {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    String data;
                    //Name
                    data = object.getString("nom") + "\n";
                    subjectName.append(data);
                    //Teachers
                    JSONArray teachers = object.getJSONArray("professors");
                    for (int i = 0; i < teachers.length(); i++) {
                        JSONObject teacher = teachers.getJSONObject(i);
                        data = "- " + "<b>" + teacher.getString("nom") + ":</b> <br>\t\t" + teacher.getString("email") + "<br><br>";
                        subjectData.append(Html.fromHtml(data));
                    }
                    //Credits
                    data = "<br><b>" + getString(R.string.credits) + ":</b> " + object.getInt("credits") + "<br><br>";
                    subjectData.append(Html.fromHtml(data));
                    //Description
                    data = "<br><b>" + getString(R.string.description_objectives) + ":</b><br><br>";
                    subjectData.append(Html.fromHtml(data));
                    JSONArray descriptions = object.getJSONArray("descripcio");
                    for (int i = 0; i < descriptions.length(); i++) {
                        data = "- " + descriptions.getString(i) + "\n\n";
                        subjectData.append(data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
