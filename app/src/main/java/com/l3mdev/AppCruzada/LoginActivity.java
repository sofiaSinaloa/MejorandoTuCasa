package com.l3mdev.AppCruzada;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

// Clases necesarias a importar para la conexion con la base de datos
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


import androidx.appcompat.app.AppCompatActivity;

import com.l3mdev.AppCruzada.interfaces.AppConfig;
import com.l3mdev.AppCruzada.preference.SecurePreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button loginButton, registrarButton;
    TextView TextViewProblemasLogin;

    SharedPreferences sharedPreference;

    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";
    private static final String KEY_PASSWORD = "password";
    private static final String TOKEN_KEY = "token";
    private static final String KEY_ROLES = "roles";
    private String authToken;

    String name;
    private androidx.appcompat.app.AlertDialog customProgressDialog; // Cambiado de ProgressDialog a AlertDialog


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView errorTextView = findViewById(R.id.TextViewErrorLogin);
        errorTextView.setVisibility(View.GONE);

        username = findViewById(R.id.EditTextUsuarioLogin);
        password = findViewById(R.id.EditTextContraseñaLogin);

        sharedPreference = getSharedPreferences("mypref", MODE_PRIVATE);

        name = sharedPreference.getString(KEY_NAME, null);

        loginButton = findViewById(R.id.ButtonInicioSesionLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();

                String Tusername = username.getText().toString();
                String Tpassword = password.getText().toString();

                if (isInternetConnected()) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS) // Timeout de conexión
                            .readTimeout(30, TimeUnit.SECONDS)    // Timeout de lectura
                            .build();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("userName", Tusername);
                        jsonObject.put("password", Tpassword);
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

                    showCustomProgressDialog();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Manejo de error en el hilo principal
                                    hideCustomProgressDialog();
                                    hideKeyboard();
                                    if (e instanceof UnknownHostException) {
                                        errorTextView.setText("No se pudo iniciar sesión. Verifica tu conexión a Internet.");
                                    } else if (e instanceof SocketTimeoutException) {
                                        errorTextView.setText("Tiempo de espera agotado. Inténtalo de nuevo más tarde.");
                                    } else {
                                        errorTextView.setText("Comuniquese con el equipo desarrollador. Error de red: " + e.getMessage());
                                    }
                                    errorTextView.setVisibility(View.VISIBLE);
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
                                            authToken = data.optString("token", "");
                                            Log.d(TAG, "onResponse: authtoken: " + authToken);
                                            // Login correcto, continuar con la lógica de inicio de sesión exitoso
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    errorTextView.setVisibility(View.GONE);
                                                    String Tusername = username.getText().toString();
                                                    String Tpassword = password.getText().toString();

                                                    // Guardar datos de sesión en SharedPreferences
                                                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedPreference.edit();
                                                    editor.putString(TOKEN_KEY, authToken);
                                                    editor.putString(KEY_NAME, Tusername);
                                                    editor.putString(KEY_PASSWORD, Tpassword);

                                                    // Guardar la fecha actual en milisegundos
                                                    long currentTimeMillis = System.currentTimeMillis();
                                                    editor.putLong(TOKEN_KEY + "_timestamp", currentTimeMillis);
                                                    Log.d(TAG, "run: ");


                                                    // Registrar el valor que se está guardando
                                                    Log.d(TAG, "Guardando token: " + authToken + " con timestamp: " + currentTimeMillis);

                                                    JSONArray rolesArray = data.optJSONArray("roles");
                                                    if (rolesArray != null && rolesArray.length() > 0) {
                                                        String rolesString = rolesArray.optString(0, ""); // Obtener el primer rol
                                                        editor.putString(KEY_ROLES, rolesString);
                                                        Log.d(TAG, "run: roles: " + rolesString);
                                                    } else {
                                                        editor.remove(KEY_ROLES);
                                                    }

                                                    editor.apply();

                                                    hideCustomProgressDialog();
                                                    startMainActivity();
                                                }
                                            });
                                        } else {
                                            // Credenciales incorrectas
                                            String message = data.optString("message", "");
                                            if (message.contains("Incorrect Credentials")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideCustomProgressDialog();
                                                        errorTextView.setText("Usuario o contraseña incorrectos. Vuelva a intentarlo.");
                                                        errorTextView.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            } else {
                                                // Otro tipo de error de autenticación
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideCustomProgressDialog();
                                                        errorTextView.setText("Error en la autenticación: " + message);
                                                        errorTextView.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        // Respuesta no esperada del servidor
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                hideCustomProgressDialog();
                                                errorTextView.setText("Error en la respuesta del servidor.");
                                                errorTextView.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideCustomProgressDialog();
                                            errorTextView.setText("Error en la respuesta del servidor. JSON inválido.");
                                            errorTextView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Manejo de error en el hilo principal
                                        hideCustomProgressDialog();
                                        Log.d(TAG, "onResponse: Error en la respuesta del servidor. Código: " + response.code());
                                        errorTextView.setText("Usuario o contraseña incorrectos. Vuelva a intentarlo.");
                                        errorTextView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    hideCustomProgressDialog();
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this);
                    builder.setIcon(R.drawable.baseline_error_24);
                    builder.setTitle("Error");
                    builder.setMessage("La conexión a internet es débil o se ha perdido. Inténtelo más tarde.");
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    androidx.appcompat.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        TextViewProblemasLogin = findViewById(R.id.TextViewProblemasLogin);
        TextViewProblemasLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Aviso");
                builder.setMessage("Por problemas de 'inicio de sesión', comunícate con el " +
                        "departamento de sistemas. EXT. 2407");

                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Verificar si el campo de usuario está vacío
                if (charSequence.length() == 0) {
                    errorTextView.setVisibility(View.GONE); // Oculta el mensaje de error
                }
             }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Verificar si el campo de contraseña está vacío
                if (charSequence.length() == 0) {
                    errorTextView.setVisibility(View.GONE); // Oculta el mensaje de error
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    private void opcionesdeConexion() {
        android.app.AlertDialog.Builder innerBuilder = new android.app.AlertDialog.Builder(LoginActivity.this);
        innerBuilder.setIcon(R.drawable.baseline_error_24);
        innerBuilder.setTitle("Conexión a internet perdida");
        innerBuilder.setMessage("Comprueba que tu dispositivo esté conectado a internet y vuelve a intentarlo. ")
                .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        opcionesDeRestablecer();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        innerBuilder.create().show();
    }

    private void opcionesDeRestablecer() {
        if (!restablecerConexion()) {
            opcionesdeConexion();
        }
    }

    private boolean restablecerConexion() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void startMainActivity() {
        // Crear un Intent para iniciar la actividad MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showCustomProgressDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this);

        View view = getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);

        builder.setView(view);
        builder.setCancelable(false);

        // Ajustar el tamaño del AlertDialog
        customProgressDialog = builder.create();
        customProgressDialog.setCanceledOnTouchOutside(false);
        customProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Mostrar el AlertDialog
        customProgressDialog.show();
    }

    private void hideCustomProgressDialog() {
        if (customProgressDialog != null && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View view = getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }

    public void guardarTokenConFecha(String token, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);

        // Guardar la fecha actual en milisegundos
        long currentTimeMillis = System.currentTimeMillis();
        editor.putLong(TOKEN_KEY + "_timestamp", currentTimeMillis);

        editor.apply();
    }
}