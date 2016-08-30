package com.upc.fib.racopocket;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TimetableMainMenu extends Fragment
{
    //ViewPager viewPager;
    ListView listView;
    ImageView connectionProblem;
    ProgressBar progressBar;

    HashMap<String, Integer> colorScheme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_timetable));
        return inflater.inflate(R.layout.timetable_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(R.id.listViewTimetable);
        connectionProblem = (ImageView) view.findViewById(R.id.connectionTimetable);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarTimetable);

        setColorScheme();

        new GetTimetableData().execute();
    }

    private void setColorScheme() {
        int[] colors = {
                Color.parseColor("#DFE9C6"),
                Color.parseColor("#FFF3BA"),
                Color.parseColor("#FFD2A7"),
                Color.parseColor("#BDDCE9"),
                Color.parseColor("#DDBFE4"),
        };

        colorScheme = new HashMap<>();
        String mySubjects = FileHelpers.readFileToString(getContext().getApplicationContext(), "assignatures.json");
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

    private class GetTimetableData extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!FileHelpers.fileExists(getContext().getApplicationContext(), "horari-setmanal.json")) {
                FileHelpers.fetchAndStoreJSONFile(getContext().getApplicationContext(), null, "https://raco.fib.upc.edu/api/aules/horari-setmanal.json" , "horari-setmanal.json");
            }

            return FileHelpers.readFileToString(getContext(), "horari-setmanal.json");

        }

        @Override
        protected void onPostExecute(String response) {

            if (response == null) {
                connectionProblem.setVisibility(View.VISIBLE);
            } else {
                final ArrayList<TimetableInfo> classroomsInfo = new ArrayList<>();
                try {
                    JSONArray timetableJSONArray = new JSONArray(response);
                    for (int i = 0; i < timetableJSONArray.length(); i++) {
                        JSONObject currentClass = timetableJSONArray.getJSONObject(i);
                        String subject = currentClass.getString("Assig");
                        String group = currentClass.getString("Grup") + currentClass.getString("Tipus");
                        String timeStart = currentClass.getString("HoraInici") + "-" + currentClass.getString("HoraFi") + "h";
                        String classroom = currentClass.getString("Aules");
                        classroomsInfo.add(new TimetableInfo(subject, group, timeStart, classroom));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final ArrayAdapter<TimetableInfo> adapter = new ArrayAdapter<TimetableInfo>(getContext(), R.layout.timetable_item_list, R.id.timetableStartTime, classroomsInfo) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(R.id.timetableStartTime);
                        TextView text2 = (TextView) view.findViewById(R.id.timetableName);
                        TextView text3 = (TextView) view.findViewById(R.id.timetableClassroom);

                        String subject = classroomsInfo.get(position).getName();

                        String name = subject + " " + classroomsInfo.get(position).getGroup();
                        String startTime = classroomsInfo.get(position).getStartTime();
                        String classroom = classroomsInfo.get(position).getClassroom();

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

            progressBar.setVisibility(View.GONE);
        }

    }

    private class TimetableInfo {
        String name;
        String group;
        String startTime;
        String classroom;

        TimetableInfo(String name, String group, String startTime, String classroom) {
            this.name = name;
            this.group = group;
            this.startTime = startTime;
            this.classroom = classroom;
        }

        public String getName() {
            return this.name;
        }

        public String getStartTime() {
            return this.startTime;
        }

        public String getClassroom() {
            return this.classroom;
        }

        public String getGroup() {
            return this.group;
        }

    }


}
