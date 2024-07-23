package com.l3mdev.AppCruzada;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.l3mdev.AppCruzada.DB.DatabaseHelper;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class Configuracion extends AppCompatActivity implements CambioContraseña.ExampleDialogListener{

    //Para el cambio de contraseña
    private TextView textViewcambiarcontraseña, textViewCerrarSesion;
    private DatabaseHelper databaseHelper;


    //Para que el nombre del usuario aparezca dentro del activity
    SharedPreferences sharedPreference;
    private static final String KEY_TOKEN = "token";
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";
    private static final String KEY_PASSWORD = "password";
    TextView textView, cerrarSesion;
    LinearLayout cerrarsesion;

    // Mediante el uso del switch se permite o niegan las notificaciones
    Switch switchPermitirNotificaciones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Programación para el uso del switch
        switchPermitirNotificaciones = findViewById(R.id.switchPermitirNotificaciones);
        switchPermitirNotificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Programación para el uso del switch
                switchPermitirNotificaciones = findViewById(R.id.switchPermitirNotificaciones);

                // Desactivar el cambio del estado del Switch
                switchPermitirNotificaciones.setChecked(false);

//                switchPermitirNotificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                        // Desactivar el cambio del estado del Switch
//                        switchPermitirNotificaciones.setChecked(false);
//
//                        // Mostrar un Toast para informar al usuario
//                        Toast.makeText(Configuracion.this, "Esta opción no está disponible", Toast.LENGTH_SHORT).show();
//                    }
//                });

                switchPermitirNotificaciones.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Mostrar el Toast cuando se toca el Switch
                        Toast.makeText(Configuracion.this, "Esta opción no está disponible", Toast.LENGTH_SHORT).show();
                        // Desactivar el Switch para mantenerlo en su estado original
                        switchPermitirNotificaciones.setChecked(false);
                    }
                });
            }
        });

        // Despliega la opción de regresar a la anterior pantalla por medio de un boton creado por la misma interfaz de codigo.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarConfiguracion);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Regresar...
                finish();
            }
        });

        cerrarsesion = findViewById(R.id.linearLayoutConfiguracionDeSesionConfiguracion);
        cerrarsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Configuracion.this);
                builder.setIcon(R.drawable.baseline_error_24);
                builder.setTitle("Confirmación");
                builder.setMessage("¿Estás seguro de cerrar sesión?");

                builder.setPositiveButton("cerrar sesión", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar sesión
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(KEY_NAME);
                        editor.remove(KEY_PASSWORD);
                        editor.remove(KEY_TOKEN);
                        editor.apply();

                        databaseHelper.deleteDatabase(Configuracion.this);
                        Intent loginIntent = new Intent(Configuracion.this, LoginActivity.class);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        dialog.dismiss();
                        finish();
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Para que el nombre del usuario aparezca dentro del activity
        textView = findViewById(R.id.textViewNomUsuConfiguracion);
        sharedPreference = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        String name = sharedPreference.getString(KEY_NAME, null);
        if (name != null){
            textView.setText("Usuario: " + name);
        }
    }

    //Permite que el customDialog se despliegue dentro de la actividad sin necesidad de una nueva actividad
    public void openDialog(){
        CambioContraseña cambioContraseñaDialog = new CambioContraseña();
        cambioContraseñaDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(String nuevaContraseña) {
        // Obtener la contraseña actual del SharedPreferences
        String contraseñaActual = sharedPreference.getString(KEY_PASSWORD, "");

        // Verificar si la contraseña actual coincide con la introducida por el usuario
        if (contraseñaActual.equals(nuevaContraseña)) {
            Toast.makeText(this, "La nueva contraseña no puede ser igual a la actual.", Toast.LENGTH_SHORT).show();
        } else {
            // Actualizar la contraseña en SharedPreferences
            SharedPreferences.Editor editor = sharedPreference.edit();
            editor.putString(KEY_PASSWORD, nuevaContraseña);
            editor.apply();
            Toast.makeText(this, "Contraseña actualizada correctamente.", Toast.LENGTH_SHORT).show();
        }
    }
}