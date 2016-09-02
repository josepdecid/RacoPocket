package com.upc.fib.racopocket;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.upc.fib.racopocket.Utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ClassAvailabilityMainMenu extends Fragment
{
    TextView lastUpdate, connectionProblemText, header1ClassAvailability, header2ClassAvailability;
    ImageButton update;
    ListView listView;
    ProgressBar progressBar;
    LinearLayout classAvailabilityInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_class_availability));
        View rootView = inflater.inflate(R.layout.class_availability_main_menu, container, false);

        lastUpdate = (TextView) rootView.findViewById(R.id.lastUpdateClassAvailability);
        update = (ImageButton) rootView.findViewById(R.id.updateNotifications);
        connectionProblemText = (TextView) rootView.findViewById(R.id.connectionProblemTextClassAvailability);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        listView = (ListView) rootView.findViewById(R.id.listView);
        classAvailabilityInfo = (LinearLayout) rootView.findViewById(R.id.class_info_linear_layout);
        header1ClassAvailability = (TextView) rootView.findViewById(R.id.header1ClassAvailability);
        header2ClassAvailability = (TextView) rootView.findViewById(R.id.header2ClassAvailability);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionProblemText.setVisibility(View.GONE);
                FileUtils.fileDelete(getContext().getApplicationContext(), "places-lliures.json");
                new GetClassroomsInfo().execute();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                WebView webView = new WebView(getContext());
                String building;
                if (position < 10) building = "a5";
                else if (position < 13) building = "b5";
                else building = "c6";
                webView.loadUrl("https://raco.fib.upc.edu/mapa_ocupades.php?mod=" + building);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(webView);
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });

        new GetClassroomsInfo().execute();
    }

    private class GetClassroomsInfo extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!FileUtils.fileExists(getContext().getApplicationContext(), "places-lliures.json")) {
                FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), null, "https://raco.fib.upc.edu/api/aules/places-lliures.json" , "places-lliures.json");
            }

            return FileUtils.readFileToString(getContext(), "places-lliures.json");

        }

        @Override
        protected void onPostExecute(String response) {

            if (response == null) {
                connectionProblemText.setVisibility(View.VISIBLE);
            } else {
                final ArrayList<ClassroomInfo> classroomsInfo = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String lastUpdateText = getResources().getString(R.string.last_update) + ": " + jsonObject.getString("update");
                    lastUpdate.setText(lastUpdateText);
                    lastUpdate.setVisibility(View.VISIBLE);
                    update.setVisibility(View.VISIBLE);

                    JSONArray classroomsJSONArray = jsonObject.getJSONArray("aules");
                    for (int i = 0; i < classroomsJSONArray.length(); i++) {
                        String name = classroomsJSONArray.getJSONObject(i).getString("nom");
                        int availability = classroomsJSONArray.getJSONObject(i).getInt("places");
                        classroomsInfo.add(new ClassroomInfo(name, availability));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final ArrayAdapter<ClassroomInfo> adapter = new ArrayAdapter<ClassroomInfo>(getContext(), R.layout.class_availability_item_list, R.id.classroomNameClassAvailability, classroomsInfo) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(R.id.classroomNameClassAvailability);
                        TextView text2 = (TextView) view.findViewById(R.id.availabilityClassAvailability);

                        String name = classroomsInfo.get(position).getName().toUpperCase();
                        int availability = classroomsInfo.get(position).getAvailability();

                        text1.setText(name);
                        text2.setText(String.valueOf(availability));

                        int statusColor;
                        if (availability == 0) {
                            statusColor = ContextCompat.getColor(getContext(), R.color.not_available);
                        } else if (availability < 5) {
                            statusColor = ContextCompat.getColor(getContext(), R.color.quite_not_available);
                        } else {
                            statusColor = ContextCompat.getColor(getContext(), R.color.available);
                        }

                        view.setBackgroundColor(statusColor);
                        return view;
                    }
                };

                listView.setAdapter(adapter);
            }

            progressBar.setVisibility(View.GONE);
            header1ClassAvailability.setVisibility(View.VISIBLE);
            header2ClassAvailability.setVisibility(View.VISIBLE);
            classAvailabilityInfo.setVisibility(View.VISIBLE);
        }

    }

    private class ClassroomInfo {
        String name;
        int availability;

        ClassroomInfo(String name, int availability) {
            this.name = name;
            this.availability = availability;
        }

        public String getName() {
            return this.name;
        }

        public int getAvailability() {
            return this.availability;
        }

    }

}
