package com.upc.fib.racopocket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ClassAvailabilityMainMenu extends Fragment
{
    TextView classAvailability;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_class_availability));
        return inflater.inflate(R.layout.class_availability_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        classAvailability = (TextView) view.findViewById(R.id.classAvailability);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

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

            return FileHelpers.fetchDirectlyJSON(null, "https://raco.fib.upc.edu/api/aules/places-lliures.json");

        }

        @Override
        protected void onPostExecute(String response) {
            classAvailability.setText(response);
            progressBar.setVisibility(View.GONE);
        }

    }

}
