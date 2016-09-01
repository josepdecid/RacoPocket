package com.upc.fib.racopocket;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UniversityMapMainMenu extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_uni_map));
        return inflater.inflate(R.layout.university_map_main_menu, container, false);
    }
}
