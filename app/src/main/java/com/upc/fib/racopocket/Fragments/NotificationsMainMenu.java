package com.upc.fib.racopocket.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.upc.fib.racopocket.Activities.MainMenuActivity;
import com.upc.fib.racopocket.Models.NotificationModel;
import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class NotificationsMainMenu extends Fragment {

    ImageButton update;
    ExpandableListView expListViewNotifications;
    ProgressBar progressBar;

    String mySubjects;
    List<String> listDataHeader;

    ExpandableListAdapter expandableListAdapter;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_notifications));
        View rootView = inflater.inflate(R.layout.notifications_main_menu, container, false);

        update = (ImageButton) rootView.findViewById(R.id.updateNotifications);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarNotifications);
        expListViewNotifications = (ExpandableListView) rootView.findViewById(R.id.expListViewNotifications);

        String token = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expListViewNotifications.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                NotificationModel notification = (NotificationModel) expandableListAdapter.getChild(groupPosition, childPosition);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(notification.getLink()));
                startActivity(intent);
                return true;
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetNotifications().execute(true);
            }
        });

        new GetNotifications().execute(false);
    }

    private class GetNotifications extends AsyncTask<Boolean, Void, List<Pair<Integer, String>>> {

        FileUtils fileUtils = new FileUtils(getContext().getApplicationContext(), consumer);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Pair<Integer, String>> doInBackground(Boolean... params) {
            Boolean forceUpdate = params[0];

            List<Pair<Integer, String>> subjectsRss = new ArrayList<>();
            String mySubjects = fileUtils.readFileToString("assignatures.json");
            try {
                JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
                for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                    JSONObject mySubjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                    String subjectID = mySubjectJSONObject.getString("codi_upc");
                    int statusCode = 200;
                    if (forceUpdate || !fileUtils.checkFileExists("notifications_" + subjectID + ".rss")
                            || (PreferencesUtils.preferenceExists(getContext().getApplicationContext(), "enableAutomaticUpdates")
                            && PreferencesUtils.recoverBooleanPreference(getContext().getApplicationContext(), "enableAutomaticUpdates"))) {
                        statusCode = fileUtils.fetchAndStoreFile("https://raco.fib.upc.edu/api-v1/avisos-assignatura.rss?espai=" + subjectID, "notifications_" + subjectID + ".rss");
                    }
                    String subjectData = fileUtils.readFileToString("notifications_" + subjectID + ".rss");
                    subjectsRss.add(new Pair<>(statusCode, subjectData));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return subjectsRss;
        }

        @Override
        protected void onPostExecute(List<Pair<Integer, String>> response) {
            for (Pair<Integer, String> subjectNotifications: response) {
                if (subjectNotifications.first != 200) {
                    Toast.makeText(getContext().getApplicationContext(), getResources().getString(R.string.connection_problems), Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            mySubjects = fileUtils.readFileToString("assignatures.json");
            listDataHeader = new ArrayList<>();
            try {
                JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
                for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                    JSONObject subjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                    listDataHeader.add(subjectJSONObject.getString("idAssig"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HashMap<String, List<NotificationModel>> listDataChild = new HashMap<>();
            for (int i = 0; i < response.size(); i++) {
                List<NotificationModel> dataChild = fileUtils.readFileToRSS(response.get(i).second);
                listDataChild.put(listDataHeader.get(i), dataChild);
            }

            // Set and display the Subjects Array
            expandableListAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);
            expListViewNotifications.setAdapter(expandableListAdapter);

            expListViewNotifications.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<String> listDataHeader;
        private HashMap<String, List<NotificationModel>> listDataChild;

        public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<NotificationModel>> listChildData) {
            this.context = context;
            this.listDataHeader = listDataHeader;
            this.listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.notifications_item_list, parent, false);
            }

            TextView titleNotifications = (TextView) convertView.findViewById(R.id.titleNotifications);
            TextView dateNotifications = (TextView) convertView.findViewById(R.id.dateNotifications);

            NotificationModel element = (NotificationModel) getChild(groupPosition, childPosition);
            titleNotifications.setText(element.getTitle());

            final String oldFormat = "EEE, dd MMM yyyy HH:mm:ss Z";
            final String newFormat = "dd/MM/yyyy HH':'mm':'ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(oldFormat, Locale.ENGLISH);
            try {
                Date date = simpleDateFormat.parse(element.getPubDate());
                simpleDateFormat.applyPattern(newFormat);
                String dateNewFormat = simpleDateFormat.format(date);
                dateNotifications.setText(dateNewFormat);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.notifications_group_list, parent, false);
            }

            TextView lblListHeader = (TextView) convertView.findViewById(R.id.notificationListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

}