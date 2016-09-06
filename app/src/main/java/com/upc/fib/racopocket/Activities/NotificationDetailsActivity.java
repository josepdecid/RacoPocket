package com.upc.fib.racopocket.Activities;

import android.content.ClipboardManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.upc.fib.racopocket.R;

public class NotificationDetailsActivity extends AppCompatActivity {

    TextView notificationDescription, notificationTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        notificationTitle = (TextView) findViewById(R.id.notification_title);
        notificationDescription = (TextView) findViewById(R.id.notification_description);
        if (notificationDescription != null) {
            notificationDescription.setClickable(true);
            notificationDescription.setMovementMethod(LinkMovementMethod.getInstance());
        }

        String subjectName = getIntent().getExtras().getString("subjectName");
        String title = getIntent().getExtras().getString("title");
        String description = getIntent().getExtras().getString("description");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subjectName);
        }

        notificationTitle.setText(title);
        notificationDescription.setText(Html.fromHtml(description));

    }
}
