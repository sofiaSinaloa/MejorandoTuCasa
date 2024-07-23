package com.l3mdev.AppCruzada;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.l3mdev.AppCruzada.DB.DatabaseHelper;
import com.l3mdev.AppCruzada.servicios.DataUpdateService;
import com.l3mdev.AppCruzada.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
        private static final String TAG = "MainActivity";
        private static final int REQUEST_CAMERA_PERMISSION = 100;
        private static final int REQUEST_STORAGE_PERMISSION = 101;
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 103;

        private boolean previousSignalWeakState = false;
        private boolean isVerificationDone = false;
        private boolean isSnackbarShown = false;
        private static boolean isSignalWeakStatic;
        private Snackbar weakNetworkSnackbar;


        private ActivityMainBinding binding;
        private DatabaseHelper databaseHelper;
        private static final String SHARED_PREF_NAME = "mypref";
        private static final String KEY_NAME = "name";
        private static final String KEY_PASSWORD = "password";
        private static final String KEY_TOKEN = "token";
        private static final String KEY_ROLES = "roles";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());

                previousSignalWeakState = false;

                verificarYActualizarToken();

                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.contains(KEY_NAME) && sharedPreferences.contains(KEY_PASSWORD);

                databaseHelper = new DatabaseHelper(this);

                if (!isLoggedIn) {
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkCameraPermission() && checkStoragePermission() && checkLocationPermission()) {
                                replaceFragment(new HomeFragment());
                        } else {
                                requestPermissions();
                        }
                } else {
                        replaceFragment(new HomeFragment());
                }

                Toolbar toolbar = findViewById(R.id.toolbarMain);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                binding.bottomNavigationViewMain.setBackground(null);
                binding.bottomNavigationViewMain.setOnItemSelectedListener(item -> {
                        if (item.getItemId() == R.id.IconoInicioBottomMenu) {
                                replaceFragment(new HomeFragment());
                        }
                        return true;
                });

        }


        private boolean checkCameraPermission() {
                return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }

        private boolean checkStoragePermission() {
                return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        private boolean checkLocationPermission() {
                return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        private void requestPermissions() {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CAMERA_PERMISSION);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode == REQUEST_CAMERA_PERMISSION) {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                if (checkStoragePermission()) {
                                        replaceFragment(new HomeFragment());
                                        if (checkLocationPermission()) {
                                                //getLocation();
                                        } else {
                                                ActivityCompat.requestPermissions(this, new String[]{
                                                                Manifest.permission.ACCESS_FINE_LOCATION},
                                                        LOCATION_PERMISSION_REQUEST_CODE);
                                        }
                                } else {
                                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_STORAGE_PERMISSION);
                                }
                        } else {
                                showPermissionDeniedDialog();
                        }
                } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                replaceFragment(new HomeFragment());
                        } else {
                                showPermissionDeniedDialog();
                        }
                }
        }

        private void showPermissionDeniedDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permisos requeridos").setMessage("Los permisos de cámara y almacenamiento son necesarios para utilizar esta aplicación").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                finish();
                        }
                }).setCancelable(false).show();
        }

        @Override
        protected void onResume() {
                super.onResume();
                // Calcular y mostrar el tiempo transcurrido desde el almacenamiento del token
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

                verificarYActualizarToken();

                databaseHelper = new DatabaseHelper(this);
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                if (!previousSignalWeakState){
                        iniciarServicioActualizacionDatos();
                }
                showSignalWeakToast();
        }

        private void iniciarServicioActualizacionDatos(){
                previousSignalWeakState = true;
                Intent intent = new Intent(this, DataUpdateService.class);
                DataUpdateService.enqueueWork(this, intent);
        }

        private BroadcastReceiver signalStrengthReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                        if ("com.example.SIGNAL_STRENGTH_CHANGED".equals(intent.getAction())) {
                                boolean isSignalWeak = intent.getBooleanExtra("isSignalWeak", false);
                                isSignalWeakStatic = isSignalWeak;
                                isVerificationDone = true;
                                updateSignalWeakState(isSignalWeak);
                        }
                }
        };

        private void updateSignalWeakState(boolean isSignalWeak) {
                if (isSignalWeak != isSnackbarShown) {
                        isSnackbarShown = isSignalWeak;
                        showSignalWeakToast();
                }
        }


        private void showSignalWeakToast() {
                if (isSignalWeakStatic) {
                        if (weakNetworkSnackbar == null || !weakNetworkSnackbar.isShownOrQueued()) {
                                weakNetworkSnackbar = Snackbar.make(findViewById(android.R.id.content), "Señal de red móvil débil, por favor, considere cambiar a una red estable.", Snackbar.LENGTH_INDEFINITE);
                                weakNetworkSnackbar.show();
                        }
                } else {
                        if (weakNetworkSnackbar != null && weakNetworkSnackbar.isShownOrQueued()) {
                                weakNetworkSnackbar.dismiss();
                                weakNetworkSnackbar = null;
                        }
                }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu){
                getMenuInflater().inflate(R.menu.toolbar_menu, menu);
                return true;
        }
        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                        case R.id.itemConfiguracionToolbarMenu:
                                Toast.makeText(this, "Esta opción no esta disponible por el momento", Toast.LENGTH_SHORT).show();
                                break;
                        case R.id.itemCerrarSesionToolbarMenu:
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setIcon(R.drawable.baseline_error_24);
                                builder.setTitle("Confirmación");
                                builder.setMessage("¿Estás seguro de cerrar sesión?");
                                builder.setPositiveButton("Cerrar sesión", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                                if (databaseHelper.hasPendingBeneficiarios()) {
                                                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(MainActivity.this);
                                                        innerBuilder.setIcon(R.drawable.baseline_error_24);
                                                        innerBuilder.setTitle("Envios pendientes");
                                                        innerBuilder.setMessage("Tienes envíos beneficiarios todavia por enviar. ¿Estás seguro de cerrar sesión?");
                                                        innerBuilder.setPositiveButton("Cerrar sesión", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                        editor.remove(KEY_NAME);
                                                                        editor.remove(KEY_PASSWORD);
                                                                        editor.remove(KEY_TOKEN);
                                                                        editor.remove(KEY_ROLES);
                                                                        editor.apply();
                                                                        databaseHelper.deleteDatabase(MainActivity.this);
                                                                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                                                                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        startActivity(loginIntent);
                                                                        dialog.dismiss();
                                                                        finish();
                                                                }
                                                        });
                                                        innerBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                }
                                                        });
                                                        innerBuilder.show();
                                                } else {
                                                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.remove(KEY_NAME);
                                                        editor.remove(KEY_PASSWORD);
                                                        editor.remove(KEY_TOKEN);
                                                        editor.remove(KEY_ROLES);
                                                        editor.apply();
                                                        databaseHelper.deleteDatabase(MainActivity.this);
                                                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                                                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(loginIntent);
                                                        dialog.dismiss();
                                                        finish();
                                                }
                                        }
                                });
                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                        }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                break;
                }
                return true;
        }

        private void replaceFragment(Fragment fragment) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layoutMain, fragment);
                fragmentTransaction.commit();
        }

//        public long obtenerFechaDeAlmacenamientoDelToken(SharedPreferences sharedPreferences) {
//                return sharedPreferences.getLong(KEY_TOKEN + "_timestamp", -1);
//        }
//
//        public String calcularTiempoTranscurridoDelToken(SharedPreferences sharedPreferences) {
//                long timestamp = obtenerFechaDeAlmacenamientoDelToken(sharedPreferences);
//                if (timestamp == -1) {
//                        return "Fecha de almacenamiento del token no encontrada";
//                }
//
//                long tiempoActual = System.currentTimeMillis();
//                long diferencia = tiempoActual - timestamp;
//
//                // Convertir la diferencia a un formato legible (por ejemplo, en horas, minutos, días, etc.)
//                // Aquí se muestra un ejemplo de cómo convertirlo a días y horas
//                long segundos = diferencia / 1000;
//                long minutos = segundos / 60;
//                long horas = minutos / 60;
//                long dias = horas / 24;
//
//                return dias + " días y " + (horas % 24) + " horas " + (minutos % 60) + " minutos " + (segundos % 60) + " segundos";
//
//        }

        private static final long INTERVALO_ACTUALIZACION_TIMESTAMP = 5 * 60 * 1000; // 5 minutos en milisegundos
        private static final String KEY_TOKEN_TIMESTAMP = KEY_TOKEN + "_timestamp";
        private long tiempoUltimaActualizacionTimestamp = 0;

        public void verificarYActualizarToken() {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                String Tusername = sharedPreferences.getString(KEY_NAME, "");
                String Tpassword = sharedPreferences.getString(KEY_PASSWORD, "");

                long tiempoActual = System.currentTimeMillis();
                long tiempoTranscurrido = tiempoActual - tiempoUltimaActualizacionTimestamp;

                if (tiempoTranscurrido >= INTERVALO_ACTUALIZACION_TIMESTAMP) {
                        obtenerTokenDesdeServidor(Tusername, Tpassword);
                } else {
                        Log.d(TAG, "verificarYActualizarToken: El tiempo todavia no es excedido");
                }

                // No actualizamos tiempoUltimaActualizacion aquí

                // Verificar y actualizar timestamp
                actualizarTimestampIfNeeded();
        }

        private void actualizarTimestampIfNeeded() {
                long tiempoActual = System.currentTimeMillis();
                long tiempoTranscurrido = tiempoActual - tiempoUltimaActualizacionTimestamp;

                if (tiempoTranscurrido >= INTERVALO_ACTUALIZACION_TIMESTAMP) {
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        // Guardar la fecha actual en milisegundos
                        long currentTimeMillis = System.currentTimeMillis();
                        editor.putLong(KEY_TOKEN_TIMESTAMP, currentTimeMillis);
                        editor.apply();

                        // Actualizar el tiempo de referencia
                        tiempoUltimaActualizacionTimestamp = currentTimeMillis;

                        // Registrar el valor que se está guardando
                        Log.d(TAG, "Actualizando timestamp: " + currentTimeMillis);
                }
        }

        private void obtenerTokenDesdeServidor(String username, String password) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS) // Timeout de conexión
                        .readTimeout(30, TimeUnit.SECONDS)    // Timeout de lectura
                        .build();

                JSONObject jsonObject = new JSONObject();
                try {
                        jsonObject.put("userName", username);
                        jsonObject.put("password", password);
                } catch (JSONException e) {
                        e.printStackTrace();
                }

                RequestBody requestBody = RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        jsonObject.toString()
                );
                Request request = new Request.Builder()
                        .url("https://api.cruzada.changarro.online/api/v1/Account/Login/")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                                Log.d(TAG, "Hubo un error al obtener el token. Por favor, vuelva a iniciar sesión. ");
                                        }
                                });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                if (response.isSuccessful()) {
                                        String responseData = response.body().string();

                                        Log.d(TAG, "onResponse: Respuesta del servidor: " + responseData);
                                        try {
                                                JSONObject jsonResponse = new JSONObject(responseData);

                                                boolean isSuccess = jsonResponse.optBoolean("isSuccess", false);
                                                JSONObject data = jsonResponse.optJSONObject("data");

                                                if (isSuccess && data != null) {
                                                        boolean isAuthenticated = data.optBoolean("isAuthenticated", false);
                                                        if (isAuthenticated) {
                                                                String authToken = data.optString("token", "");

                                                                // Guardar datos de sesión en SharedPreferences
                                                                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                editor.putString(KEY_TOKEN, authToken);
                                                                editor.apply();

                                                                // Guardar la fecha actual en milisegundos
                                                                long currentTimeMillis = System.currentTimeMillis();
                                                                editor.putLong(KEY_TOKEN + "_timestamp", currentTimeMillis);

                                                                // Registrar el valor que se está guardando
                                                                Log.d(TAG, "Guardando token: " + authToken + " con timestamp: " + currentTimeMillis);

                                                        } else {
                                                                Log.d(TAG, "onResponse: Las credenciales son incorrectas.");
                                                        }
                                                } else {
                                                        Log.d(TAG, "onResponse: Error en la respuesta del servidor");
                                                }
                                        } catch (JSONException e) {
                                                Log.d(TAG, "onResponse: Error en la respuesta del servidor. problema del json");
                                        }
                                }
                        }
                });
        }



//        private static final long INTERVALO_ACTUALIZACION_TOKEN = 15 * 60 * 1000;
//        private long tiempoUltimaActualizacion = 0;
//
//        public void verificarYActualizarToken() {
//                long tiempoActual = System.currentTimeMillis();
//                long tiempoTranscurrido = tiempoActual - tiempoUltimaActualizacion;
//
//                if (tiempoTranscurrido >= INTERVALO_ACTUALIZACION_TOKEN) {
//
//                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
//                        String Tusername = sharedPreferences.getString(KEY_NAME, "");
//                        String Tpassword = sharedPreferences.getString(KEY_PASSWORD, "");
//
//                        obtenerTokenDesdeServidor(Tusername, Tpassword);
//
//                        // Actualizar el tiempo de referencia
//                        tiempoUltimaActualizacion = tiempoActual;
//                }
//        }
//
//        private void obtenerTokenDesdeServidor(String username, String password) {
//                OkHttpClient client = new OkHttpClient.Builder()
//                        .connectTimeout(10, TimeUnit.SECONDS) // Timeout de conexión
//                        .readTimeout(30, TimeUnit.SECONDS)    // Timeout de lectura
//                        .build();
//
//                JSONObject jsonObject = new JSONObject();
//                try {
//                        jsonObject.put("userName", username);
//                        jsonObject.put("password", password);
//                } catch (JSONException e) {
//                        e.printStackTrace();
//                }
//
//                RequestBody requestBody = RequestBody.create(
//                        MediaType.get("application/json; charset=utf-8"),
//                        jsonObject.toString()
//                );
//                Request request = new Request.Builder()
//                        .url("https://api.cruzada.changarro.online/api/v1/Account/Login/")
//                        .post(requestBody)
//                        .build();
//
//                client.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                                e.printStackTrace();
//                                runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                                Log.d(TAG, "Hubo un error al obtener el token. Por favor, vuelva a iniciar sesión. ");
//                                        }
//                                });
//                        }
//
//                        @Override
//                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                                if (response.isSuccessful()) {
//                                        String responseData = response.body().string();
//
//                                        Log.d(TAG, "onResponse: Respuesta del servidor: " + responseData);
//                                        try {
//                                                JSONObject jsonResponse = new JSONObject(responseData);
//
//                                                boolean isSuccess = jsonResponse.optBoolean("isSuccess", false);
//                                                JSONObject data = jsonResponse.optJSONObject("data");
//
//                                                if (isSuccess && data != null) {
//                                                        boolean isAuthenticated = data.optBoolean("isAuthenticated", false);
//                                                        if (isAuthenticated) {
//                                                                String authToken = data.optString("token", "");
//
//                                                                // Guardar datos de sesión en SharedPreferences
//                                                                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
//                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
//                                                                editor.putString(KEY_TOKEN, authToken);
//                                                                editor.apply();
//
//                                                                // Guardar la fecha actual en milisegundos
//                                                                long currentTimeMillis = System.currentTimeMillis();
//                                                                editor.putLong(KEY_TOKEN + "_timestamp", currentTimeMillis);
//
//                                                                // Registrar el valor que se está guardando
//                                                                Log.d(TAG, "Guardando token: " + authToken + " con timestamp: " + currentTimeMillis);
//
//                                                        } else {
//                                                                Log.d(TAG, "onResponse: Las credenciales son incorrectas.");
//                                                        }
//                                                } else {
//                                                        Log.d(TAG, "onResponse: Error en la respuesta del servidor");
//                                                }
//                                        } catch (JSONException e) {
//                                                Log.d(TAG, "onResponse: Error en la respuesta del servidor. problema del json");
//                                        }
//                                }
//                        }
//                });
//        }
}
