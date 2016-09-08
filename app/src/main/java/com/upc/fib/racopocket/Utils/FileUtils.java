package com.upc.fib.racopocket.Utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import biweekly.io.text.ICalReader;
import oauth.signpost.OAuthConsumer;

public class FileUtils {

    // Checks if 'filename' file, exists in the given context.
    public static boolean fileExists(Context context, String fileName)
    {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }

    // Tries delete 'filename' file in the given context, and returns if file has been deleted.
    public static boolean fileDelete(Context context, String fileName)
    {
        File file = new File(context.getFilesDir(), fileName);
        return file.delete();
    }

    // Fetch and store url data into 'outputFile' if statusCode is OK, and returns statusCode.
    public static int fetchAndStoreFile(Context context, OAuthConsumer consumer, String u, String outputFile)
    {
        try {
            URL url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if (consumer != null)
                consumer.sign(urlConnection);

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder buffer = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null)
                    buffer.append(line).append('\n');

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    String data = buffer.toString();
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(outputFile, Context.MODE_PRIVATE));
                        outputStreamWriter.write(data);
                        outputStreamWriter.close();
                    } catch (IOException e) {
                        Log.e("File", "File write failed: " + e.toString());
                    }
                }
                return statusCode;

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.i("FILE", "" + e.getMessage());
        }

        return -1;

    }

    // Reads a file from 'inputFile' and returns the string if read successfully, otherwise, null.
    public static String readFileToString(Context context, String inputFile)
    {
        try {
            InputStream inputStream = context.openFileInput(inputFile);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null)
                    stringBuilder.append(receiveString);

                inputStream.close();
                return stringBuilder.toString();
            }
        } catch (IOException e) {
            Log.e("FILE", "Can not read file: " + e.toString());
        }

        return null;
    }


    public static ICalReader readFileToICal(Context context, String inputFile)
    {
        File file = new File(context.getFilesDir(), inputFile);
        try {
            return new ICalReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

}
