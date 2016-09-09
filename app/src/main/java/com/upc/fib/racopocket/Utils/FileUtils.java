package com.upc.fib.racopocket.Utils;

import android.content.Context;
import android.support.annotation.Nullable;
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

    /**
     * Checks if the file exists in the given context.
     * @param context Desired context.
     * @param fileName Name of the file to be checked.
     * @return Boolean value representing if file exists.
     */
    public static boolean checkFileExists(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }

    /**
     * Tries to delete the file in the given context, and returns if file has been deleted.
     * @param context Desired context.
     * @param fileName Name of the file to be deleted.
     * @return Boolean value representing if file has been removed.
     */
    public static boolean deleteFile(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.delete();
    }

    /**
     * Fetch and store url data into a file if statusCode is OK, and returns statusCode.
     * @param context Desired context.
     * @param consumer OAuth consumer with secret keys already set. Pass null to avoid signing.
     * @param u URL from which extract the data.
     * @param outputFile Name of file where the data will be stored.
     * @return Integer value representing the server response status code, -1 if any error ocurred.
     */
    public static int fetchAndStoreFile(Context context, OAuthConsumer consumer, String u, String outputFile) {
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
                        Log.e(Constants.TAG_FILE, "File write failed: " + e.toString());
                    }
                }

                return statusCode;

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(Constants.TAG_FILE, "Connection error: " + e.getMessage());
        }

        return -1;

    }

    /**
     * Reads a file and returns the String if read successfully, otherwise, null.
     * @param context Desired context.
     * @param inputFile Name of file where the data will be read from.
     * @return String object containing the data, null if any error ocurred.
     */
    @Nullable
    public static String readFileToString(Context context, String inputFile) {
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
            Log.e(Constants.TAG_FILE, "File read to string failed: " + e.toString());
        }

        return null;
    }

    /**
     * Reads a file and returns the ICalReader if read successfully, otherwise, null.
     * @param context Desired context.
     * @param inputFile Name of file where the data will be read from.
     * @return ICalReader object containing the data, null if any error ocurred.
     */
    @Nullable
    public static ICalReader readFileToICalReader(Context context, String inputFile) {
        File file = new File(context.getFilesDir(), inputFile);
        try {
            return new ICalReader(file);
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG_FILE, "File read to ICalReader failed: " + e.toString());
        }

        return null;
    }

}
