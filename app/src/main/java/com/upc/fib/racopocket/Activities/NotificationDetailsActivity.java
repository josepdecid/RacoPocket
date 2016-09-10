package com.upc.fib.racopocket.Activities;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class NotificationDetailsActivity extends AppCompatActivity {

    TextView notificationDescription, notificationTitle;
    LinearLayout downloadLayout;
    ProgressBar progressBar;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        downloadLayout = (LinearLayout) findViewById(R.id.downloadLayoutNotificationDetails);
        notificationTitle = (TextView) findViewById(R.id.notification_title);
        notificationDescription = (TextView) findViewById(R.id.notification_description);
        progressBar = (ProgressBar) findViewById(R.id.progressBarNotificationDetails);
        //Allow link-clicking
        if (notificationDescription != null) {
            notificationDescription.setClickable(true);
            notificationDescription.setMovementMethod(LinkMovementMethod.getInstance());
        }

        Bundle extras = getIntent().getExtras();
        String title = extras.getString("title");
        String description = extras.getString("description");
        final String subjectId = extras.getString("subjectId");
        final String notificationId = extras.getString("notificationId");
        List<Pair<String, String>> attachments = new ArrayList<>();
        for (int i = 0;;i++) {
            if (extras.containsKey("attachment_" + i + "_id")) {
                attachments.add(new Pair<>(extras.getString("attachment_" + i + "_id"), extras.getString("attachment_" + i + "_title")));
            } else break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subjectId);
        }

        notificationTitle.setText(title);
        if (description != null && !description.equals("null")) {
            notificationDescription.setText(Html.fromHtml(description));
        }

        String token = PreferencesUtils.recoverStringPreference(getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverStringPreference(getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        for (final Pair<String, String> attachment : attachments) {
            Button downloadButton = new Button(this);
            downloadButton.setText(attachment.second);
            downloadButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_attachment_black_24dp), null, null, null);
            downloadButton.setGravity(Gravity.START);
            downloadButton.setSingleLine(true);
            downloadButton.setEllipsize(TextUtils.TruncateAt.END);
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DownloadAttachmentsAsyncTask().execute(subjectId, notificationId, attachment.first, attachment.second);
                }
            });
            downloadLayout.addView(downloadButton);
        }
    }

    private class DownloadAttachmentsAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            String subjectId = params[0];
            String notificationId = params[1];
            String attachmentId = params[2];
            String filename = params[3];
            String url = "https://raco.fib.upc.edu/api-v1/attachment?espai=" + subjectId + "&idAvis=" + notificationId + "&idAdjunt=" + attachmentId;
            try {
                consumer.sign(url);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);
            } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            progressBar.setVisibility(View.GONE);
        }

    }

}
