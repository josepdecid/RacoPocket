package com.upc.fib.racopocket.Utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ColorScheme {

    private Context context;
    private int[] colors = {
            Color.parseColor("#DFE9C6"),
            Color.parseColor("#FFF3BA"),
            Color.parseColor("#FFD2A7"),
            Color.parseColor("#BDDCE9"),
            Color.parseColor("#DDBFE4"),
            Color.parseColor("#F4828C"),
            Color.parseColor("#BD8B5A"),
            Color.parseColor("#EEABCA"),
            Color.parseColor("#C2BB63"),
            Color.parseColor("#297DB5"),
    };

    /**
     * ColorScheme constructor
     * @param context Desired context.
     */
    public ColorScheme(Context context) {
        this.context = context;
    }

    /**
     * Returns a Hash map with subject as key and color as value for each assigned subject.
     * @return HashMap containing all subject-color necessary entries.
     */
    public HashMap<String, Integer> setColorsToSubjects() {
        HashMap<String, Integer> colorScheme = new HashMap<>();
        FileUtils fileUtils = new FileUtils(this.context, null);
        String mySubjects = fileUtils.readFileToString("assignatures.json");
        try {
            JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
            for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                JSONObject mySubjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                colorScheme.put(mySubjectJSONObject.getString("idAssig"), colors[i % colors.length]);
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG_JSON, "JSON parse failed: " + e.toString());
        }

        return colorScheme;
    }
}
