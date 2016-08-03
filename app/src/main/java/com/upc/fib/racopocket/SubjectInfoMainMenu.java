package com.upc.fib.racopocket;

import android.content.ContentValues;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SubjectInfoMainMenu extends Fragment
{
    AutoCompleteTextView subjectSelector;
    Button buttonSearch;
    TextView subjectName, subjectData;
    ImageView connectionStatus;
    ProgressBar progressBar;

    String subjectsListData;
    String subjectDataWithCode;
    String fetchSubjectData = "false";

    ArrayList<Pair<String, String>> subjects_name = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.subject_info_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subjectName = (TextView) view.findViewById(R.id.subjectName);
        subjectData = (TextView) view.findViewById(R.id.subjectData);
        buttonSearch = (Button) view.findViewById(R.id.queryButton);
        connectionStatus = (ImageView) view.findViewById(R.id.connection);
        subjectSelector = (AutoCompleteTextView) view.findViewById(R.id.subjectSelector);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);

        Boolean fetchData = false;
        try {
            FileInputStream fi = getContext().openFileInput("subjectsList.json");
            BufferedInputStream bi = new BufferedInputStream(fi);
            StringBuilder buffer = new StringBuilder();

            while (bi.available() != 0) {
                char c = (char) bi.read();
                buffer.append(c);
            }

            fi.close();
            bi.close();

            subjectsListData = buffer.toString();

        } catch (FileNotFoundException e) {
            Log.i("FILE", "File does not exist, proceeding to get subjectsList.json");
            fetchData = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get File if it does not exist and parse data
        new GetSubjectsInfo().execute(fetchData);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectName = subjectSelector.getText().toString();
                if (subjectName.length() == 0) {
                    Toast.makeText(getActivity(), "Type something to search!", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean found = false;
                    for (int i = 0; i < subjects_name.size() && !found; i++) {
                        if (subjects_name.get(i).getFirst().equals(subjectName)) {
                            found = true;
                            String subjectCode = subjects_name.get(i).getSecond();

                            fetchSubjectData = "false";
                            try {
                                FileInputStream fi = getContext().openFileInput("subject_" + subjectCode + ".json");
                                BufferedInputStream bi = new BufferedInputStream(fi);
                                StringBuilder buffer = new StringBuilder();

                                while (bi.available() != 0) {
                                    char c = (char) bi.read();
                                    buffer.append(c);
                                }

                                fi.close();
                                bi.close();

                                subjectDataWithCode = buffer.toString();

                            } catch (FileNotFoundException e) {
                                Log.i("FILE", "File does not exist, proceeding to get subject_" + subjectCode + ".json");
                                fetchSubjectData = "true";
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            new GetSubjectInfoWithCode().execute(fetchSubjectData, subjectCode);
                        }
                    }
                }
            }
        });
    }

    public class GetSubjectsInfo extends AsyncTask<Boolean, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Boolean... params) {

            Boolean fetchData = params[0];

            if (fetchData) {
                try {
                    URL url = new URL("https://raco.fib.upc.edu/api/assignatures/llista.json");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder buffer = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            buffer.append(line).append('\n');
                        }

                        // Store data in file subjectsList.json
                        subjectsListData = buffer.toString();
                        try {
                            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("subjectsList.json"), "UTF-8"));
                            out.write(subjectsListData);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return "OK";

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                }
            } else {
                return "Already Fetched";
            }

            return null;

        }

        @Override
        protected void onPostExecute (String response) {

            super.onPostExecute(response);

            if (response == null) {
                subjectName.setText(getResources().getString(R.string.connection_problems));
            } else {
                try {
                    JSONArray allSubjectsJSON = new JSONArray(subjectsListData);
                    for (int i = 0; i < allSubjectsJSON.length(); i++) {
                        JSONObject singleSubjectJSON = allSubjectsJSON.getJSONObject(i);
                        // Key = subject code, Value = subject name
                        Pair<String, String> aux = new Pair<>(singleSubjectJSON.getString("idAssig"), singleSubjectJSON.getString("codi_upc"));
                        subjects_name.add(aux);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayList<String> subjectsAcronyms = new ArrayList<>();
            for (int i = 0; i < subjects_name.size(); i++) {
                subjectsAcronyms.add(subjects_name.get(i).getFirst());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, subjectsAcronyms);
            subjectSelector.setAdapter(adapter);

            progressBar.setVisibility(View.GONE);

        }
    }

    public class GetSubjectInfoWithCode extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            subjectName.setText("");
            subjectData.setText("");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String fetchData = params[0];
            String subjectCode = params[1];

            if (fetchData.equals("true")) {
                try {
                    URL url = new URL("https://raco.fib.upc.edu/api/assignatures/info.json?codi_upc=" + subjectCode);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append('\n');
                        }
                        bufferedReader.close();

                        subjectDataWithCode = stringBuilder.toString();
                        try {
                            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("subject_" + subjectCode + ".json"), "UTF-8"));
                            out.write(subjectDataWithCode);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return "OK";

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                }
            } else {
                return "Already Fetched";
            }

            return null;

        }

        @Override
        protected void onPostExecute(String response) {

            super.onPostExecute(response);

            if (response == null) {
                connectionStatus.setImageResource(R.drawable.connection);
            } else {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(subjectDataWithCode).nextValue();
                    String data;

                    data = object.getString("nom") + "\n";
                    subjectName.append(data);

                    JSONArray teachers = object.getJSONArray("professors");
                    for (int i = 0; i < teachers.length(); i++) {
                        JSONObject teacher = teachers.getJSONObject(i);
                        data = "- " + "<b>" + teacher.getString("nom") + ":</b> <br>\t\t" + teacher.getString("email") + "<br><br>";
                        subjectData.append(Html.fromHtml(data));
                    }

                    data = "<br><b>" + getString(R.string.credits) + ":</b> " + object.getInt("credits") + "<br><br>";
                    subjectData.append(Html.fromHtml(data));

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

                progressBar.setVisibility(View.GONE);
            }

        }
    }

    // Simplified Pair Implementation
    public class Pair<A, B> {
        private A first;
        private B second;

        public Pair(A first, B second) {
            super();
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return this.first;
        }

        public B getSecond() {
            return this.second;
        }
    }

}