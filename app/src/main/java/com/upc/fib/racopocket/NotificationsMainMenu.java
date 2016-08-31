package com.upc.fib.racopocket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class NotificationsMainMenu extends Fragment
{
    ImageButton update;
    ImageView connectionProblem;
    ProgressBar progressBar;

    String mySubjects, myNotifications;

    ExpandableListView expListView;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_notifications));
        return inflater.inflate(R.layout.notifications_main_menu, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        update = (ImageButton) view.findViewById(R.id.updateNotifications);
        connectionProblem = (ImageView) view.findViewById(R.id.connectionNotifications);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarNotifications);
        expListView = (ExpandableListView) view.findViewById(R.id.expListViewNotifications);

        String token = TokensStorageHelpers.recoverTokens(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = TokensStorageHelpers.recoverTokens(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String subjectName = null, title = null, description = null;
                try {
                    JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
                    JSONObject subjectJSONObject = mySubjectsJSONArray.getJSONObject(groupPosition);
                    subjectName = subjectJSONObject.getString("idAssig");

                    JSONObject subjectsNotificationsJSONObject = new JSONObject(myNotifications);
                    JSONArray subjectNotificationsJSONArray = subjectsNotificationsJSONObject.getJSONArray(subjectName);
                    JSONObject subjectNotificationJSONObject = subjectNotificationsJSONArray.getJSONObject(childPosition);
                    title = subjectNotificationJSONObject.getString("title");
                    description = subjectNotificationJSONObject.getString("description");


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getContext(), NotificationDetailsActivity.class);
                intent.putExtra("subjectName", subjectName);
                intent.putExtra("title", title);
                intent.putExtra("description", description);
                startActivity(intent);
                return true;
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileHelpers.fileDelete(getContext().getApplicationContext(), "avisos.json");
                new GetNotifications().execute();
            }
        });

        new GetNotifications().execute();
    }

    private class GetNotifications extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!FileHelpers.fileExists(getContext().getApplicationContext(), "avisos.json")) {
                FileHelpers.fetchAndStoreJSONFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/avisos.json", "avisos.json");
            }
            return FileHelpers.readFileToString(getContext().getApplicationContext(), "avisos.json");
        }

        @Override
        protected void onPostExecute(String response) {
            myNotifications = response;
            mySubjects = FileHelpers.readFileToString(getContext().getApplicationContext(), "assignatures.json");
            List<String> listDataHeader = new ArrayList<>();
            try {
                JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
                for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                    JSONObject subjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                    listDataHeader.add(subjectJSONObject.getString("idAssig"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HashMap<String, List<String>> listDataChild = new HashMap<>();
            try {
                JSONObject subjectsNotificationsJSONObject = new JSONObject(response);
                for (int i = 0; i < listDataHeader.size(); i++) {
                    // Each subject
                    List<String> dataChild = new ArrayList<>();
                    // If subject has notifications we display the subject's title
                    // Otherwise we don't display the subject's title
                    if (subjectsNotificationsJSONObject.has(listDataHeader.get(i))) {
                        JSONArray iSubjectNotificationsJSONArray = subjectsNotificationsJSONObject.getJSONArray(listDataHeader.get(i));
                        for (int j = 0; j < iSubjectNotificationsJSONArray.length(); j++) {
                            // Each subject notifications
                            JSONObject subjectJSONObject = iSubjectNotificationsJSONArray.getJSONObject(j);
                            String title = subjectJSONObject.getString("title");
                            dataChild.add(title);
                        }
                        listDataChild.put(listDataHeader.get(i), dataChild);
                    } else {
                        listDataHeader.remove(i--);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Set and display the Subjects Array
            ExpandableListAdapter expandableListAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);
            expListView.setAdapter(expandableListAdapter);

            progressBar.setVisibility(View.GONE);
        }

    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private List<String> listDataHeader;
        private HashMap<String, List<String>> listDataChild;

        public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listChildData) {
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

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            TextView txtListChild = (TextView) convertView.findViewById(android.R.id.text1);
            txtListChild.setText(childText);

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
                LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.notifications_list_group, parent, false);
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