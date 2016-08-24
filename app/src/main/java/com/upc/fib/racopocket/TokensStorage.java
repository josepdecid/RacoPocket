package com.upc.fib.racopocket;

import android.content.Context;
import android.content.SharedPreferences;

public class TokensStorage {

    public static void storeTokens(Context context, String token, String token_secret) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("OAUTH_TOKEN", token);
        editor.putString("OAUTH_TOKEN_SECRET", token_secret);
        editor.apply();
    }

    public static String recoverTokens(Context context, String preference_name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString(preference_name, "");
    }
}
