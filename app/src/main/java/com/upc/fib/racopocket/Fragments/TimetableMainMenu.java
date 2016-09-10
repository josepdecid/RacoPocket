package com.upc.fib.racopocket.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.upc.fib.racopocket.Activities.MainMenuActivity;
import com.upc.fib.racopocket.Models.TimetableSubjectModel;
import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.ColorScheme;
import com.upc.fib.racopocket.Utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class TimetableMainMenu extends Fragment {

    ImageButton previousDay, nextDay;
    TextView currentDayText;
    ListView listView;
    ProgressBar progressBar;

    int currentDay;
    HashMap<String, Integer> colorSchemeMap;
    ArrayList<ArrayList<TimetableSubjectModel>> classroomsInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_timetable));
        View rootView = inflater.inflate(R.layout.timetable_main_menu, container, false);

        nextDay = (ImageButton) rootView.findViewById(R.id.nextDayTimetable);
        previousDay = (ImageButton) rootView.findViewById(R.id.previousDayTimetable);
        listView = (ListView) rootView.findViewById(R.id.listViewTimetable);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarTimetable);
        currentDayText = (TextView) rootView.findViewById(R.id.currentDatTimetable);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        classroomsInfo = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            classroomsInfo.add(new ArrayList<TimetableSubjectModel>());

        currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        if (currentDay == 0 || currentDay == 6)
            currentDay = 1;

        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDay--;
                if (currentDay == 0) currentDay = 5;
                writeWeekDay();
                printTimetable();
            }
        });

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDay++;
                if (currentDay == 6) currentDay = 1;
                writeWeekDay();
                printTimetable();
            }
        });

        writeWeekDay();
        colorSchemeMap = new ColorScheme(getContext()).setColorsToSubjects();
        new GetTimetableData().execute();
    }

    private void writeWeekDay() {
        String day;
        switch (currentDay) {
            case 1:
                day = getResources().getString(R.string.monday);
                break;
            case 2:
                day = getResources().getString(R.string.tuesday);
                break;
            case 3:
                day = getResources().getString(R.string.wednesday);
                break;
            case 4:
                day = getResources().getString(R.string.thursday);
                break;
            case 5:
                day = getResources().getString(R.string.friday);
                break;
            default:
                day = "";
        }
        currentDayText.setText(day);
    }

    private class GetTimetableData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            FileUtils fileUtils = new FileUtils(getContext().getApplicationContext(), null);
            return fileUtils.readFileToString("horari-setmanal.json");
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    JSONArray timetableJSONArray = new JSONArray(response);
                    for (int i = 0; i < timetableJSONArray.length(); i++) {
                        JSONObject currentClass = timetableJSONArray.getJSONObject(i);
                        String subject = currentClass.getString("Assig");
                        String group = currentClass.getString("Grup") + currentClass.getString("Tipus");
                        String timeStart = currentClass.getString("HoraInici") + "-" + currentClass.getString("HoraFi") + "h";
                        String classroom = currentClass.getString("Aules");
                        classroom = classroom.replace("[", "");
                        classroom = classroom.replace("]", "");
                        classroom = classroom.replace("\"", "");
                        classroom = classroom.replace(",", ", ");
                        classroomsInfo.get(currentClass.getInt("Dia") - 1).add(new TimetableSubjectModel(subject, group, timeStart, classroom));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            printTimetable();
            progressBar.setVisibility(View.GONE);
        }

    }

    void printTimetable() {
        final ArrayAdapter<TimetableSubjectModel> adapter = new ArrayAdapter<TimetableSubjectModel>(getContext(), R.layout.timetable_item_list, R.id.timetableStartTime, classroomsInfo.get(currentDay - 1)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.timetableStartTime);
                TextView text2 = (TextView) view.findViewById(R.id.timetableName);
                TextView text3 = (TextView) view.findViewById(R.id.timetableClassroom);

                String subject = classroomsInfo.get(currentDay - 1).get(position).getName();

                String name = subject + " " + classroomsInfo.get(currentDay - 1).get(position).getGroup();
                String startTime = classroomsInfo.get(currentDay - 1).get(position).getStartTime();
                String classroom = classroomsInfo.get(currentDay - 1).get(position).getClassroom();

                text1.setText(startTime);
                text2.setText(name);
                text3.setText(classroom);

                int color = colorSchemeMap.get(subject);
                view.setBackgroundColor(color);

                return view;
            }
        };
        listView.setAdapter(adapter);
    }

}
