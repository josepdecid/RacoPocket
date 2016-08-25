package com.upc.fib.racopocket;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import oauth.signpost.OAuthConsumer;

public class FileHelpers {

    public static void fetchAndStoreJSONFile(Context context, OAuthConsumer consumer, String u, String outputFile) {

        try {
            URL url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            consumer.sign(urlConnection);
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line).append('\n');
                }

                String data = buffer.toString();
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(outputFile, Context.MODE_PRIVATE));
                    outputStreamWriter.write(data);
                    outputStreamWriter.close();
                } catch (IOException e) {
                    Log.e("File", "File write failed: " + e.toString());
                }

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.i("OAuth", "" + e.getMessage());
        }

    }

    public static String readFileToString(Context context, String inputFile) {

        try {
            InputStream inputStream = context.openFileInput(inputFile);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                return stringBuilder.toString();
            }
        } catch (IOException e) {
            Log.e("FILE", "Can not read file: " + e.toString());
        }

        return "";

    }

}
