package com.upc.fib.racopocket.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.upc.fib.racopocket.Activities.MainMenuActivity;
import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SubjectInfoMainMenu extends Fragment
{
    AutoCompleteTextView subjectSelector;
    TextView subjectName, subjectData, subjectBibliography;
    ProgressBar progressBar;

    String currentCode;
    ArrayList<Pair<String, String>> subjects_name = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_subject_info));
        View rootView = inflater.inflate(R.layout.subject_info_main_menu, container, false);

        subjectName = (TextView) rootView.findViewById(R.id.subjectName);
        subjectData = (TextView) rootView.findViewById(R.id.subjectData);
        subjectBibliography = (TextView) rootView.findViewById(R.id.subjectBibliography);
        subjectSelector = (AutoCompleteTextView) rootView.findViewById(R.id.subjectSelector);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enable bibliography redirect onClick
        subjectBibliography.setClickable(true);
        subjectBibliography.setMovementMethod(LinkMovementMethod.getInstance());

        subjectSelector.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        loadSubjectsList();
    }

    private void performSearch()
    {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

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
            if (!found)
                Toast.makeText(getActivity(), getResources().getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
        }

        subjectSelector.setText("");
    }


    private void loadSubjectsList()
    {
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

    private void showSubjectInfo(String fileName)
    {
        subjectName.setText("");
        subjectData.setText("");
        subjectBibliography.setText("");

        String subjectInfo = FileUtils.readFileToString(getContext().getApplicationContext(), fileName);

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