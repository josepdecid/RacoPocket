package com.upc.fib.racopocket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TimetableMainMenu extends Fragment
{
    TextView timetable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_timetable));
        return inflater.inflate(R.layout.timetable_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timetable = (TextView) view.findViewById(R.id.timetable);
        setTimeTable();
    }

    private void setTimeTable() {

        String timetableData = FileHelpers.readFileToString(getContext().getApplicationContext(), "horari-setmanal.json");
        timetable.setText(timetableData);

    }

}
