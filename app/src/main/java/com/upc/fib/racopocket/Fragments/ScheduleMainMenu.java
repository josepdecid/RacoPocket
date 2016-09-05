package com.upc.fib.racopocket.Fragments;

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
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.text.ICalReader;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;


public class ScheduleMainMenu extends Fragment
{
    ImageButton update;
    ListView eventsList;
    ProgressBar progressBar;

    boolean workInProgress;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_schedule));

        View rootView = inflater.inflate(R.layout.schedule_main_menu, container, false);
        update = (ImageButton) rootView.findViewById(R.id.updateSchedule);
        eventsList = (ListView) rootView.findViewById(R.id.listViewSchedule);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarSchedule);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String token = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        workInProgress = false;

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!workInProgress) {
                    FileUtils.fileDelete(getContext().getApplicationContext(), "calendari-portada.ics");
                    new GetSchedule().execute();
                }
            }
        });

        new GetSchedule().execute();

    }

    private class GetSchedule extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            workInProgress = true;
        }

        @Override
        protected String doInBackground(Void... params)
        {
            if (!FileUtils.fileExists(getContext().getApplicationContext(), "calendari-portada.ics")) {
                FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/calendari-portada.ics", "calendari-portada.ics");
            }
            return FileUtils.readFileToString(getContext().getApplicationContext(), "calendari-portada.ics");
        }

        @Override
        protected void onPostExecute(String response)
        {
            parseICalReader();

            workInProgress = false;
            progressBar.setVisibility(View.GONE);
        }
    }

    private void parseICalReader()
    {
        ICalReader iCalReader = FileUtils.readFileToICal(getContext().getApplicationContext(), "calendari-portada.ics");

        if (iCalReader != null) {
            try {
                ICalendar iCalendar;
                List<Pair<String, String>> scheduleDate = new ArrayList<>();
                while ((iCalendar = iCalReader.readNext()) != null) {
                    for (VEvent event : iCalendar.getEvents()) {
                        String summary = event.getSummary().getValue();
                        String date = event.getDateStart().getValue().toString();
                        scheduleDate.add(new Pair<>(summary, date));
                    }
                }
                printSchedule(scheduleDate);
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

    private void printSchedule(final List<Pair<String, String>> scheduleDate)
    {
        final ArrayAdapter<Pair<String, String>> adapter = new ArrayAdapter<Pair<String, String>>(getContext(), R.layout.schedule_item_list, R.id.summarySchedule, scheduleDate) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.summarySchedule);
                TextView text2 = (TextView) view.findViewById(R.id.dateSchedule);

                String summary = scheduleDate.get(position).first;
                String date = scheduleDate.get(position).second;

                text1.setText(summary);
                text2.setText(date);

                return view;
            }
        };

        eventsList.setAdapter(adapter);
    }

}
