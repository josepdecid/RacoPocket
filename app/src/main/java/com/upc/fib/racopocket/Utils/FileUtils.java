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

    private Context context;
    private OAuthConsumer consumer;

    /**
     * FileUtils constructor;
     * @param context Desired context.
     * @param consumer OAuth consumer with secret keys already set. Pass null to avoid signing.
     */
    public FileUtils(Context context, OAuthConsumer consumer) {
        this.context = context;
        this.consumer = consumer;
    }

    /**
     * Checks if the file exists.
     * @param fileName Name of the file to be checked.
     * @return Boolean value representing if file exists.
     */
    public boolean checkFileExists(String fileName) {
        File file = new File(this.context.getFilesDir(), fileName);
        return file.exists();
    }

    /**
     * Tries to delete the file and returns if file has been deleted.
     * @param fileName Name of the file to be deleted.
     * @return Boolean value representing if file has been removed.
     */
    public boolean deleteFile(String fileName) {
        File file = new File(this.context.getFilesDir(), fileName);
        return file.delete();
    }

    /**
     * Fetch and store url data into a file if statusCode is OK, and returns statusCode.
     * @param u URL from which extract the data.
     * @param outputFile Name of file where the data will be stored.
     * @return Integer value representing the server response status code, -1 if any error ocurred.
     */
    public int fetchAndStoreFile(String u, String outputFile) {
        try {
            URL url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if (this.consumer != null) {
                this.consumer.sign(urlConnection);
            }

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder buffer = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line).append('\n');
                }

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
     * @param inputFile Name of file where the data will be read from.
     * @return String object containing the data, null if any error ocurred.
     */
    @Nullable
    public String readFileToString(String inputFile) {
        try {
            InputStream inputStream = this.context.openFileInput(inputFile);
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
            Log.e(Constants.TAG_FILE, "File read to string failed: " + e.toString());
        }

        return null;
    }

    /**
     * Reads a file and returns the ICalReader if read successfully, otherwise, null.
     * @param inputFile Name of file where the data will be read from.
     * @return ICalReader object containing the data, null if any error ocurred.
     */
    @Nullable
    public ICalReader readFileToICalReader(String inputFile) {
        File file = new File(this.context.getFilesDir(), inputFile);
        try {
            return new ICalReader(file);
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG_FILE, "File read to ICalReader failed: " + e.toString());
        }

        return null;
    }

}
