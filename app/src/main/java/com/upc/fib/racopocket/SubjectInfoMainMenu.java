package com.upc.fib.racopocket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.upc.fib.racopocket.Utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SubjectInfoMainMenu extends Fragment
{
    AutoCompleteTextView subjectSelector;
    TextView subjectName, subjectData, subjectBibliography;
    ImageButton buttonSearch, dataError;
    ProgressBar progressBar;

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
        subjectSelector = (AutoCompleteTextView) view.findViewById(R.id.subjectSelector);

        buttonSearch = (ImageButton) view.findViewById(R.id.queryButton);
        dataError = (ImageButton) view.findViewById(R.id.dataError);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);

        // Enable bibliography redirect onClick
        subjectBibliography.setClickable(true);
        subjectBibliography.setMovementMethod(LinkMovementMethod.getInstance());
        // Load Subjects into our Autocomplete
        loadSubjectsList();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View v) {
                dataError.setVisibility(View.GONE);
                String subjectName = subjectSelector.getText().toString().toUpperCase();
                if (subjectName.length() == 0) {
                    Toast.makeText(getActivity(), R.string.empty_field, Toast.LENGTH_SHORT).show();
                } else {
                    Boolean found = false;
                    for (int i = 0; i < subjects_name.size() && !found; i++) {
                        if (subjects_name.get(i).first.equals(subjectName)) {

                            found = true;
                            currentCode = subjects_name.get(i).second;
                            String fileName = "subject_" + currentCode + ".json";

                            showSubjectInfo(fileName);

                        }
                    }
                }
            }
        });

    }

    private void loadSubjectsList() {

        String subjectsListData = FileUtils.readFileToString(getContext().getApplicationContext(), "llista.json");
        try {
            JSONArray subjectsJSONArray = new JSONArray(subjectsListData);
            for (int i = 0; i < subjectsJSONArray.length(); i++) {
                JSONObject subjectJSONObject = subjectsJSONArray.getJSONObject(i);
                // Key = subject_id, Value = upc_code
                String subjectId = subjectJSONObject.getString("idAssig");
                String upcCode = subjectJSONObject.getString("codi_upc");
                Pair<String, String> pair = new Pair<>(subjectId, upcCode);
                subjects_name.add(pair);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<String> subjectsId = new ArrayList<>();
        for (int i = 0; i < subjects_name.size(); i++) {
            subjectsId.add(subjects_name.get(i).first);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, subjectsId);
        subjectSelector.setAdapter(adapter);

    }

    private void showSubjectInfo(String fileName) {

        subjectName.setText("");
        subjectData.setText("");
        subjectBibliography.setText("");

        String subjectInfo = FileUtils.readFileToString(getContext().getApplicationContext(), fileName);

        subjectSelector.setText("");

        try {
            JSONObject object = new JSONObject(subjectInfo);
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

}