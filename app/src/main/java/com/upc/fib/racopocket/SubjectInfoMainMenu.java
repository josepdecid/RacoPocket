package com.upc.fib.racopocket;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SubjectInfoMainMenu extends Fragment
{
    AutoCompleteTextView subjectSelector;
    Button buttonSearch;
    TextView subjectName, subjectData, subjectBibliography;
    ImageButton dataRefresh, dataRemove, dataError;
    ProgressBar progressBar;

    String subjectsListData;
    String subjectDataWithCode;
    String currentCode;

    ArrayList<Pair<String, String>> subjects_name = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_subject_info));
        return inflater.inflate(R.layout.subject_info_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subjectName = (TextView) view.findViewById(R.id.subjectName);
        subjectData = (TextView) view.findViewById(R.id.subjectData);
        subjectBibliography = (TextView) view.findViewById(R.id.subjectBibliography);
        buttonSearch = (Button) view.findViewById(R.id.queryButton);
        subjectSelector = (AutoCompleteTextView) view.findViewById(R.id.subjectSelector);

        dataRefresh = (ImageButton) view.findViewById(R.id.dataRefresh);
        dataRemove = (ImageButton) view.findViewById(R.id.dataRemove);
        dataError = (ImageButton) view.findViewById(R.id.dataError);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);

        // Enable bibliography redirect onClick
        subjectBibliography.setClickable(true);
        subjectBibliography.setMovementMethod(LinkMovementMethod.getInstance());

        Boolean fetchData = false;
        try {
            InputStream inputStream = getContext().openFileInput("subjectsList.json");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                subjectsListData = stringBuilder.toString();
            }

        } catch (FileNotFoundException e) {
            Log.i("FILE", "File does not exist, proceeding to get subjectsList.json");
            fetchData = true;
        } catch (IOException e) {
            Log.e("FILE", "Can not read file: " + e.toString());
            e.printStackTrace();
        }

        // Get File if it does not exist and parse data
        new GetSubjectsInfo().execute(fetchData);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideImageButtons();
                String subjectName = subjectSelector.getText().toString().toUpperCase();
                if (subjectName.length() == 0) {
                    Toast.makeText(getActivity(), R.string.empty_field, Toast.LENGTH_SHORT).show();
                } else {
                    Boolean found = false;
                    for (int i = 0; i < subjects_name.size() && !found; i++) {
                        if (subjects_name.get(i).getFirst().equals(subjectName)) {
                            found = true;
                            currentCode = subjects_name.get(i).getSecond();

                            String fetchSubjectData = "false";
                            try {
                                InputStream inputStream = getContext().openFileInput("subject_" + currentCode + ".json");
                                if (inputStream != null) {
                                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                    String receiveString;
                                    StringBuilder stringBuilder = new StringBuilder();

                                    while ((receiveString = bufferedReader.readLine()) != null) {
                                        stringBuilder.append(receiveString);
                                    }

                                    inputStream.close();
                                    subjectDataWithCode = stringBuilder.toString();
                                }

                            } catch (FileNotFoundException e) {
                                Log.i("FILE", "File does not exist, proceeding to get subject_" + currentCode + ".json");
                                fetchSubjectData = "true";
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            new GetSubjectInfoWithCode().execute(fetchSubjectData);
                        }
                    }
                }
            }
        });

        dataRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetSubjectInfoWithCode().execute("true");
            }
        });

        dataRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(getContext().getFilesDir(), "subject_" + currentCode + ".json");
                if (file.delete()) {
                    hideImageButtons();
                    Toast.makeText(getContext(), "Data removed successfully", Toast.LENGTH_SHORT).show();
                    subjectName.setText("");
                    subjectData.setText("");
                    subjectBibliography.setText("");
                } else {
                    Toast.makeText(getContext(), "Error while trying to remove the data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void hideImageButtons() {
        dataError.setVisibility(View.GONE);
        dataRefresh.setVisibility(View.GONE);
        dataRemove.setVisibility(View.GONE);
    }

    public class GetSubjectsInfo extends AsyncTask<Boolean, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideImageButtons();
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
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("subjectsList.json", Context.MODE_PRIVATE));
                            outputStreamWriter.write(subjectsListData);
                            outputStreamWriter.close();
                        } catch (IOException e) {
                            Log.e("FILE", "File write failed: " + e.toString());
                        }

                        return "OK";

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                    return null;
                }
            }

            return "Already Fetched";

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
            subjectBibliography.setText("");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String fetchData = params[0];

            if (fetchData.equals("true")) {
                try {
                    URL url = new URL("https://raco.fib.upc.edu/api/assignatures/info.json?codi_upc=" + currentCode);
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
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("subject_" + currentCode + ".json", Context.MODE_PRIVATE));
                            outputStreamWriter.write(subjectDataWithCode);
                            outputStreamWriter.close();
                        } catch (IOException e) {
                            Log.e("FILE", "File write failed: " + e.toString());
                        }

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                    return null;
                }
            } else {
                return "Already Fetched";
            }

            return "OK";

        }

        @Override
        protected void onPostExecute(String response) {

            super.onPostExecute(response);

            if (response == null) {
                subjectName.setText(getResources().getString(R.string.connection_problems));
                dataError.setVisibility(View.VISIBLE);
            } else {
                subjectSelector.setText("");
                dataRefresh.setVisibility(View.VISIBLE);
                dataRemove.setVisibility(View.VISIBLE);

                try {
                    JSONObject object = (JSONObject) new JSONTokener(subjectDataWithCode).nextValue();
                    String data;

                    data = object.getString("nom") + "\n";
                    subjectName.append(data);

                    JSONArray teachers = object.getJSONArray("professors");
                    if (teachers != null) {
                        for (int i = 0; i < teachers.length(); i++) {
                            JSONObject teacher = teachers.getJSONObject(i);
                            data = "- " + "<b>" + teacher.getString("nom") + ":</b> <br>\t\t" + teacher.getString("email") + "<br><br>";
                            subjectData.append(Html.fromHtml(data));
                        }
                    }

                    data = "<br><b>" + getResources().getString(R.string.credits) + ":</b> " + object.getInt("credits") + "<br><br>";
                    subjectData.append(Html.fromHtml(data));

                    data = "<br><b>" + getResources().getString(R.string.description_objectives) + ":</b><br><br>";
                    subjectData.append(Html.fromHtml(data));

                    data = object.getString("objectius");
                    if (!data.equals("null")) {
                        data = "<i>" + data + "</i><br><br>";
                        subjectData.append(Html.fromHtml(data));
                    }

                    JSONArray descriptions = object.getJSONArray("descripcio");
                    if (descriptions != null) {
                        for (int i = 0; i < descriptions.length(); i++) {
                            data = "- " + descriptions.getString(i) + "\n\n";
                            subjectData.append(data);
                        }
                    }

                    data = "<br><b>" + getResources().getString(R.string.bibliography) + ":</b><br><br>";
                    subjectBibliography.append(Html.fromHtml(data));

                    JSONArray bibliography = object.getJSONArray("bibliografia");
                    if (bibliography != null) {
                        for (int i = 0; i < bibliography.length(); i++) {
                            JSONObject book = bibliography.getJSONObject(i);
                            data = "- " + "<a href=\"" + book.getString("url") + "\">" + book.getString("text") + "</a><br><br>";
                            subjectBibliography.append(Html.fromHtml(data));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            progressBar.setVisibility(View.GONE);

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

        public A getFirst() { return this.first; }

        public B getSecond() { return this.second; }
    }

}