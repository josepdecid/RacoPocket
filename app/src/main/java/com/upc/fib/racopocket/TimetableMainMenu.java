package com.upc.fib.racopocket;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;

public class TimetableMainMenu extends Fragment
{
    TableLayout tableLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_timetable));
        return inflater.inflate(R.layout.timetable_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tableLayout = (TableLayout) view.findViewById(R.id.tableTimetable);
        setTimeTable();
    }

    private void setTimeTable() {

        String timetableData = FileHelpers.readFileToString(getContext().getApplicationContext(), "horari-setmanal.json");

        for (int i = 0; i < 12; i++) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 5f));
            for (int j = 0; j < 5; j++) {
                TextView textView = new TextView(getContext());
                textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 12f));
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                textView.setBackgroundColor(color);
                textView.setText("GRAU-EDA A5S102");
                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
        }
    }

}
