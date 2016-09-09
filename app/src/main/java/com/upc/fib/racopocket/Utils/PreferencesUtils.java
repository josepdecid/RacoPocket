package com.upc.fib.racopocket.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtils {

    /**
     * Checks if the preference exists in the given context.
     * @param context Desired context.
     * @param preferenceKey Preference name to search for.
     * @return Boolean value that represents if preference exists or not.
     */
    public static boolean preferenceExists(Context context, String preferenceKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.contains(preferenceKey);
    }

    /**
     * Stores a preference given a key and a value.
     * @param context Desired context.
     * @param preferenceKey Preference key.
     * @param preferenceValue Preference value.
     */
    public static void storeStringPreference(Context context, String preferenceKey, String preferenceValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(preferenceKey, preferenceValue).apply();
    }

    /**
     * Reads a text preference given a key.
     * @param context Desired context.
     * @param preferenceKey Preference key.
     * @return String object with the preference value.
     */
    public static String recoverStringPreference(Context context, String preferenceKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(preferenceKey, "");
    }

    /**
     * Reads a boolean preference given a key.
     * @param context Desired context.
     * @param preferenceKey Peference key.
     * @return Boolean object with the preference value.
     */
    public static boolean recoverBooleanPreference(Context context, String preferenceKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(preferenceKey, false);
    }

    /**
     * Store OAuth flow tokens.
     * @param context Desired context.
     * @param token Token used in OAuth flow.
     * @param token_secret Secret token used in OAuth flow.
     */
    public static void storeTokens(Context context, String token, String token_secret) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("OAUTH_TOKEN", token);
        editor.putString("OAUTH_TOKEN_SECRET", token_secret);
        editor.apply();
    }

    /**
     * Remove OAuth flow tokens and login check.
     * @param context Desired context.
     */
    public static void removeTokens(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove("OAUTH_TOKEN").apply();
        sharedPreferences.edit().remove("OAUTH_TOKEN_SECRET").apply();
        sharedPreferences.edit().remove("LOGIN_SUCCESSFUL").apply();
    }
}
