package com.l3mdev.AppCruzada;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";
    private static final String KEY_PASSWORD = "password";

    private long startTime; // Marca de tiempo al inicio de la actividad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "onCreate: esta entrando al splash");

        startTime = System.currentTimeMillis(); // Registra la marca de tiempo al inicio

        // Verificar el estado de inicio de sesión
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.contains(KEY_NAME) && sharedPreferences.contains(KEY_PASSWORD);

        if (isLoggedIn) {
            // Usuario autenticado, ir a la actividad principal
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        } else {
            // Usuario no autenticado, ir a la actividad de inicio de sesión
            Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        long endTime = System.currentTimeMillis(); // Registra la marca de tiempo al salir
        long duration = endTime - startTime; // Calcula la duración de la actividad en milisegundos

        Log.d(TAG, "SplashActivity duración (ms): " + duration);
    }
}