package com.upc.fib.racopocket;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;


public class SettingsMainMenu extends Fragment  {

    Spinner spinnerLanguage;
    Button commitSettings;
    String localeCode;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commitSettings = (Button) view.findViewById(R.id.commitSettings);
        commitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localeCode.length() != 0) {
                    setLocale(localeCode);
                    getActivity().finish();
                    Intent intent = new Intent(getContext(), MainMenuActivity.class);
                    startActivity(intent);
                }
            }
        });

        spinnerLanguage = (Spinner) view.findViewById(R.id.spinnerLanguage);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    localeCode = "ca";
                } else if (position == 1) {
                    localeCode = "es";
                } else {
                    localeCode = "en";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });
    }

    private void setLocale(String localeCode) {
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getContext().getResources().updateConfiguration(configuration, getContext().getResources().getDisplayMetrics());
        Toast.makeText(getContext(), getResources().getString(R.string.new_language), Toast.LENGTH_SHORT).show();
    }

}