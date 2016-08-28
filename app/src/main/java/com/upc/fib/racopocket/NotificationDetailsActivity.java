package com.upc.fib.racopocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationDetailsActivity extends AppCompatActivity {

    TextView notificationDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        notificationDetails = (TextView) findViewById(R.id.notification_details);
        notificationDetails.setText("FooBar");

    }
}
