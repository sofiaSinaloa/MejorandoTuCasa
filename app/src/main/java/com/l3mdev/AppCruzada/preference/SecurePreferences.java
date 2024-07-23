package com.l3mdev.AppCruzada.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class SecurePreferences {
    private static final String PREFERENCES_FILE = "secure_prefs";
    private static final String DEVELOPMENT_TOKEN_KEY = "development_token";
    private static final String PRODUCTION_TOKEN_KEY = "production_token";

    private SharedPreferences sharedPreferences;

    public SecurePreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public void saveTokens(String developmentToken, String productionToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVELOPMENT_TOKEN_KEY, developmentToken);
        editor.putString(PRODUCTION_TOKEN_KEY, productionToken);
        editor.apply();
    }

    public String getDevelopmentToken() {
        return sharedPreferences.getString(DEVELOPMENT_TOKEN_KEY, "flhdfSDFSA453a56afASFS");
    }

    public String getProductionToken() {
        return sharedPreferences.getString(PRODUCTION_TOKEN_KEY, "E3422FB24E3224EBB9FDFCD4AAC1F");
    }
}
