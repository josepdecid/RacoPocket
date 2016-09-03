package com.upc.fib.racopocket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;


public class ScheduleMainMenu extends Fragment
{
    ImageButton update;
    ImageView connection;
    ListView eventsList;
    ProgressBar progressBar;

    Boolean workInProgress;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_schedule));
        return inflater.inflate(R.layout.schedule_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        update = (ImageButton) view.findViewById(R.id.updateSchedule);
        connection = (ImageView) view.findViewById(R.id.connectionSchedule);
        eventsList = (ListView) view.findViewById(R.id.listViewSchedule);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarSchedule);

        String token = PreferencesUtils.recoverPreference(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverPreference(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
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
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            workInProgress = true;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (FileUtils.fileExists(getContext().getApplicationContext(), "calendari-portada.ics")) {
                FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/calendari-portada.ics", "calendari-portada.ics");
            }
            return FileUtils.readFileToString(getContext().getApplicationContext(), "calendari-portada.ics");
        }

        @Override
        protected void onPostExecute(String response) {

            /*StringReader stringReader = new StringReader(response);
            CalendarBuilder builder = new CalendarBuilder();
            try {
                Calendar calendar = builder.build(stringReader);

                ArrayList<ScheduleModel> scheduleEventsList = new ArrayList<>();
                for (Object event : calendar.getComponents(Component.VEVENT)) {
                    Date dateStart = ((VEvent) event).getStartDate().getDate();
                    Date dateEnd = ((VEvent) event).getEndDate().getDate();
                }
            } catch (IOException | ParserException e) {
                e.printStackTrace();
            }*/

            workInProgress = false;
            progressBar.setVisibility(View.GONE);
        }
    }

}
