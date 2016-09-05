package com.upc.fib.racopocket.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.upc.fib.racopocket.Fragments.ClassAvailabilityMainMenu;
import com.upc.fib.racopocket.Fragments.NotificationsMainMenu;
import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Fragments.ScheduleMainMenu;
import com.upc.fib.racopocket.Fragments.SubjectInfoMainMenu;
import com.upc.fib.racopocket.Fragments.TimetableMainMenu;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    Fragment newFragment = null;
    TextView welcomeName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        welcomeName = (TextView) findViewById(R.id.welcome_name);

        setWelcomeText();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setActionBarDesign(getResources().getString(R.string.app_name));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null)
            drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null)
            navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (id == R.id.nav_timetable)
            newFragment = new TimetableMainMenu();
        else if (id == R.id.nav_notifications)
            newFragment = new NotificationsMainMenu();
        else if (id == R.id.nav_schedule)
            newFragment = new ScheduleMainMenu();
        else if (id == R.id.nav_class_availability)
            newFragment = new ClassAvailabilityMainMenu();
        else if (id == R.id.nav_subject_info)
            newFragment = new SubjectInfoMainMenu();
         else if (id == R.id.nav_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "RacoPocket");
            String sAux = "\nLet me recommend you this application\n\n";
            sAux = sAux + "https://play.google.com/store/\n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "choose one"));
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainMenuActivity.this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            PreferencesUtils.removeTokens(getApplicationContext());

            FileUtils.fileDelete(getApplicationContext(), "info-personal.json");
            FileUtils.fileDelete(getApplicationContext(), "assignatures.json");
            FileUtils.fileDelete(getApplicationContext(), "horari-setmanal.json");
            FileUtils.fileDelete(getApplicationContext(), "avisos.json");
            FileUtils.fileDelete(getApplicationContext(), "calendari-portada.ics");
            FileUtils.fileDelete(getApplicationContext(), "places-lliures.json");

            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

        if (newFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_menu_fragment_container, newFragment).commit();
            transaction.replace(R.id.main_menu_fragment_container, newFragment).addToBackStack(null).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
            drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private Boolean exit = false;
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            if (newFragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().remove(newFragment).commit();
                newFragment = null;
                setActionBarDesign(getResources().getString(R.string.app_name));
            } else {
                if (exit)
                    finish();
                else {
                    Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
                    exit = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, 3000);
                }
            }
        }
    }

    public void setActionBarDesign(String title)
    {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    private void setWelcomeText()
    {
        String studentData = FileUtils.readFileToString(getApplicationContext(), "info-personal.json");

        try {
            JSONObject object = new JSONObject(studentData);
            String data = getResources().getString(R.string.welcome).toUpperCase() + "\n " + object.getString("nom") + " " + object.getString("cognoms");
            welcomeName.setText(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
