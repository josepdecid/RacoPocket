package com.upc.fib.racopocket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SubjectInfoMainMenu extends Fragment
{
    AutoCompleteTextView subjectSelector;
    Button buttonThread;
    TextView subjectName, subjectData;
    ProgressBar progressBar;

    ArrayList<String> subjects_name = new ArrayList<>();
    ArrayList<String> subjects_code = new ArrayList<>();

    static final String API_URL = "https://raco.fib.upc.edu/api/assignatures";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.subject_info_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: Check if data is already in the local DB
        new GetSubjectsInfo().execute();

        buttonThread = (Button) view.findViewById(R.id.queryButton);

        subjectName = (TextView) view.findViewById(R.id.subjectName);
        subjectData = (TextView) view.findViewById(R.id.subjectData);

        subjectSelector = (AutoCompleteTextView) view.findViewById(R.id.subjectSelector);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, subjects_name);
        subjectSelector.setAdapter(adapter);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        buttonThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectName = subjectSelector.getText().toString();
                if (subjectName.length() == 0) {
                    Toast.makeText(getActivity(), "Write something to serch!", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean found = false;
                    for (int i = 0; i < subjects_name.size() && !found; i++) {
                        if (subjects_name.get(i).equals(subjectName)) {
                            String subjectCode = subjects_code.get(i);
                            new GetSubjectInfoWithCode().execute(subjectCode);
                            found = true;
                        }
                    }
                    if (!found)
                        Toast.makeText(getActivity(), "This subject does not exists!", Toast.LENGTH_SHORT).show();
                }
                subjectSelector.setText("");
            }
        });

        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);


    }

    public class GetSubjectsInfo extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(API_URL + "/llista.json");
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
                    JSONArray allSubjectsJSON = new JSONArray(response);
                    String data;
                    for (int i = 0; i < allSubjectsJSON.length(); i++) {
                        JSONObject singleSubjectJSON = allSubjectsJSON.getJSONObject(i);
                        data = singleSubjectJSON.getString("idAssig") + " - " + singleSubjectJSON.getString("nom");
                        subjects_name.add(data);
                        data = singleSubjectJSON.getString("codi_upc");
                        subjects_code.add(data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //AsyncTask<Params, Progress, Result>
    public class GetSubjectInfoWithCode extends AsyncTask<String, Void, String> {

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
                URL url = new URL(API_URL + "/info.json?codi_upc=" + subjectCode);
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
