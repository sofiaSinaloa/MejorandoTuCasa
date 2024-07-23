package com.l3mdev.AppCruzada.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class ErrorManager {

    private static final String PREF_NAME = "error_pref";
    private static final String KEY_ERROR_MESSAGE = "error_message";

    public static void saveErrorMessage(Context context, String errorMessage) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ERROR_MESSAGE, errorMessage);
        editor.apply();
    }

    public static String getAndClearErrorMessage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String errorMessage = prefs.getString(KEY_ERROR_MESSAGE, null);
        clearErrorMessage(context);
        return errorMessage;
    }

    private static void clearErrorMessage(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(KEY_ERROR_MESSAGE);
        editor.apply();
    }
}
