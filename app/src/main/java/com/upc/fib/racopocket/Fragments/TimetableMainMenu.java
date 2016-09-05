package com.upc.fib.racopocket.Fragments;

import android.graphics.Color;
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
import com.upc.fib.racopocket.Models.TimetableModel;
import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.OnSwipeTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class TimetableMainMenu extends Fragment
{
    ImageButton previousDay, nextDay;
    TextView currentDayText;
    ListView listView;
    ProgressBar progressBar;

    int currentDay;
    HashMap<String, Integer> colorScheme;
    ArrayList<ArrayList<TimetableModel>> classroomsInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_timetable));
        View rootView = inflater.inflate(R.layout.timetable_main_menu, container, false);

        nextDay = (ImageButton) rootView.findViewById(R.id.nextDayTimetable);
        previousDay = (ImageButton) rootView.findViewById(R.id.previousDayTimetable);
        listView = (ListView) rootView.findViewById(R.id.listViewTimetable);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarTimetable);
        currentDayText = (TextView) rootView.findViewById(R.id.currentDatTimetable);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        classroomsInfo = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            classroomsInfo.add(new ArrayList<TimetableModel>());

        currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        if (currentDay == 0 || currentDay == 6)
            currentDay = 1;

        writeWeekDay();
        setColorScheme();

        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                currentDay--;
                if (currentDay == 0) currentDay = 5;
                writeWeekDay();
                printTimetable();
            }
        });

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                currentDay++;
                if (currentDay == 6) currentDay = 1;
                writeWeekDay();
                printTimetable();
            }
        });

        view.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeLeft()
            {
                currentDay--;
                if (currentDay == 0) currentDay = 5;
                writeWeekDay();
                printTimetable();
            }

            @Override
            public void onSwipeRight()
            {
                currentDay++;
                if (currentDay == 6) currentDay = 1;
                writeWeekDay();
                printTimetable();
            }
        });

        new GetTimetableData().execute();

    }

    private void writeWeekDay()
    {
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

    private void setColorScheme()
    {
        int[] colors = {
                Color.parseColor("#DFE9C6"),
                Color.parseColor("#FFF3BA"),
                Color.parseColor("#FFD2A7"),
                Color.parseColor("#BDDCE9"),
                Color.parseColor("#DDBFE4"),
                Color.parseColor("#F4828C"),
                Color.parseColor("#BD8B5A"),
                Color.parseColor("#EEABCA"),
                Color.parseColor("#C2BB63"),
                Color.parseColor("#297DB5"),
        };

        colorScheme = new HashMap<>();
        String mySubjects = FileUtils.readFileToString(getContext().getApplicationContext(), "assignatures.json");
        try {
            JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
            for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                JSONObject mySubjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                colorScheme.put(mySubjectJSONObject.getString("idAssig"), colors[i%colors.length]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class GetTimetableData extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params)
        {
            if (!FileUtils.fileExists(getContext().getApplicationContext(), "horari-setmanal.json"))
                FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), null, "https://raco.fib.upc.edu/api/aules/horari-setmanal.json" , "horari-setmanal.json");
            return FileUtils.readFileToString(getContext(), "horari-setmanal.json");
        }

        @Override
        protected void onPostExecute(String response)
        {
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
                        classroomsInfo.get(currentClass.getInt("Dia") - 1).add(new TimetableModel(subject, group, timeStart, classroom));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            printTimetable();
            progressBar.setVisibility(View.GONE);
        }
    }

    void printTimetable()
    {
        final ArrayAdapter<TimetableModel> adapter = new ArrayAdapter<TimetableModel>(getContext(), R.layout.timetable_item_list, R.id.timetableStartTime, classroomsInfo.get(currentDay - 1)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
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

                int color = colorScheme.get(subject);
                view.setBackgroundColor(color);

                return view;
            }
        };
        listView.setAdapter(adapter);
    }

}
