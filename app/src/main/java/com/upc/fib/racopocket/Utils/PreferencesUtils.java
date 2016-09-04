package com.upc.fib.racopocket.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtils
{
    public static boolean preferenceExists(Context context, String preferenceKey)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.contains(preferenceKey);
    }

    public static void storeStringPreference(Context context, String preferenceKey, String preferenceValue)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(preferenceKey, preferenceValue).apply();
    }

    public static String recoverStringPreference(Context context, String preference_name)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(preference_name, "");
    }

    public static boolean recoverBooleanPreference(Context context, String preference_name)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(preference_name, false);
    }

    public static void storeTokens(Context context, String token, String token_secret)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("OAUTH_TOKEN", token);
        editor.putString("OAUTH_TOKEN_SECRET", token_secret);
        editor.apply();
    }

    public static void removeTokens(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove("OAUTH_TOKEN").apply();
        sharedPreferences.edit().remove("OAUTH_TOKEN_SECRET").apply();
    }
}
