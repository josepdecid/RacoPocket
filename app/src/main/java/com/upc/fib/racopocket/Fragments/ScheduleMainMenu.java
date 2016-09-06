package com.upc.fib.racopocket.Fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.upc.fib.racopocket.Activities.MainMenuActivity;
import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.ColorScheme;
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.text.ICalReader;
import biweekly.property.DateStart;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;


public class ScheduleMainMenu extends Fragment
{
    ImageButton update;
    ListView eventsList;
    ProgressBar progressBar;

    boolean workInProgress;

    HashMap<String, Integer> colorSchemeMap;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_schedule));

        View rootView = inflater.inflate(R.layout.schedule_main_menu, container, false);
        update = (ImageButton) rootView.findViewById(R.id.updateSchedule);
        eventsList = (ListView) rootView.findViewById(R.id.listViewSchedule);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarSchedule);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        String token = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        workInProgress = false;

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (!workInProgress) {
                    new GetSchedule().execute(true);
                }
            }
        });

        colorSchemeMap = new ColorScheme(getContext()).setColorsToSubjects();
        new GetSchedule().execute(false);

    }

    private class GetSchedule extends AsyncTask<Boolean, Void, ICalReader>
    {
        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            workInProgress = true;
        }

        @Override
        protected ICalReader doInBackground(Boolean... params)
        {
            Boolean forceUpdate = params[0];
            if (forceUpdate || !FileUtils.fileExists(getContext().getApplicationContext(), "calendari-portada.ics"))
                FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/calendari-portada.ics", "calendari-portada.ics");
            else {
                if (PreferencesUtils.preferenceExists(getContext().getApplicationContext(), "enableAutomaticUpdates"))
                    if (PreferencesUtils.recoverBooleanPreference(getContext().getApplicationContext(), "enableAutomaticUpdates"))
                        FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/calendari-portada.ics", "calendari-portada.ics");
            }

            return FileUtils.readFileToICal(getContext().getApplicationContext(), "calendari-portada.ics");
        }

        @Override
        protected void onPostExecute(ICalReader response)
        {
            if (response != null)
                parseICalReader(response);
            workInProgress = false;
            progressBar.setVisibility(View.GONE);
        }
    }

    private void parseICalReader(ICalReader iCalReader)
    {
        try {
            ICalendar iCalendar;
            final List<Pair<String, DateStart>> scheduleData = new ArrayList<>();
            while ((iCalendar = iCalReader.readNext()) != null) {
                for (VEvent event : iCalendar.getEvents()) {
                    String summary = event.getSummary().getValue();
                    DateStart date = event.getDateStart();
                    if (date.getValue().after(new Date()))
                        scheduleData.add(new Pair<>(summary, date));
                }
            }

            Collections.sort(scheduleData, new Comparator<Pair<String, DateStart>>() {
                public int compare(Pair<String, DateStart> o1, Pair<String, DateStart> o2)
                {
                    return o1.second.getValue().compareTo(o2.second.getValue());
                }
            });

            final ArrayAdapter<Pair<String, DateStart>> adapter = new ArrayAdapter<Pair<String, DateStart>>(getContext(), R.layout.schedule_item_list, R.id.summarySchedule, scheduleData) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(R.id.summarySchedule);
                    TextView text2 = (TextView) view.findViewById(R.id.dateSchedule);

                    String summary = scheduleData.get(position).first;
                    String date = scheduleData.get(position).second.getValue().toString();

                    text1.setText(summary);
                    text2.setText(date);

                    int color = Color.parseColor("#EFEFEF");
                    for (Map.Entry<String, Integer> entry : colorSchemeMap.entrySet()) {
                        if (summary.toLowerCase().contains(entry.getKey().toLowerCase())) {
                            color = entry.getValue();
                            break;
                        }
                    }
                    view.setBackgroundColor(color);

                    return view;
                }
            };

            eventsList.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                iCalReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}