package com.upc.fib.racopocket.Activities;

import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class NotificationDetailsActivity extends AppCompatActivity
{
    TextView notificationDescription, notificationTitle;
    LinearLayout downloadLayout;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        downloadLayout = (LinearLayout) findViewById(R.id.downloadLayoutNotificationDetails);
        notificationTitle = (TextView) findViewById(R.id.notification_title);
        notificationDescription = (TextView) findViewById(R.id.notification_description);
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
        notificationDescription.setText(Html.fromHtml(description));

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
                    //TODO: Fix 403 ERROR ON FILE
                    new DownloadAttachmentsAsyncTask().execute(subjectId, notificationId, attachment.first);
                }
            });
            downloadLayout.addView(downloadButton);
        }
    }

    private class DownloadAttachmentsAsyncTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            
        }

        @Override
        protected String doInBackground(String... params)
        {
            String subjectId = params[0];
            String notificationId = params[1];
            String attachmentId = params[2];
            String url = "https://raco.fib.upc.edu/api-v1/attachment?espai=" + subjectId + "&idAvis=" + notificationId + "&idAdjunt=" + attachmentId;
            FileUtils.fetchAndStoreFile(getApplicationContext(), consumer, url, "attachment.pdf");
            return FileUtils.readFileToString(getApplicationContext(), "attachment.pdf");
        }

        @Override
        protected void onPostExecute(String response)
        {
            Toast.makeText(NotificationDetailsActivity.this, response, Toast.LENGTH_LONG).show();
        }
    }
}
