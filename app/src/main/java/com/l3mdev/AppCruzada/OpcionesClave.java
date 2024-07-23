package com.l3mdev.AppCruzada;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.l3mdev.AppCruzada.DB.DatabaseHelper;
import com.l3mdev.AppCruzada.databinding.ActivityOpcionesClaveBinding;

import java.util.ArrayList;


public class OpcionesClave extends AppCompatActivity implements DatabaseHelper.DatabaseCallback, DatabaseHelper.DataUpdateListener {
    private DatabaseHelper databaseHelper;
    ActivityOpcionesClaveBinding binding;
    ListAdapterOpcionesClave listAdapterOpcionesClave;
    ArrayList<ListDataOpcionesClave> dataArrayList = new ArrayList<>();
    ListDataOpcionesClave listDataOpcionesClave;
    private AlertDialog customProgressDialog; // Cambiado de ProgressDialog a AlertDialog


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones_clave);


        binding = ActivityOpcionesClaveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);


        // Configuración de la barra de herramientas
        // Despliega boton de retroceso para la actividad
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarOpcionesClave);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Regresar a la actividad anterior
                finish();
            }
        });


        ArrayList<OpcionesClaveData> opcionesClaveList = databaseHelper.getAllOpcionesClaves();

        for (OpcionesClaveData opcionesClaveData : opcionesClaveList) {
            listDataOpcionesClave = new ListDataOpcionesClave(
                    opcionesClaveData.getNumeroDeClave(),
                    opcionesClaveData.getIdentificador()

            );
            dataArrayList.add(listDataOpcionesClave);
        }

        listAdapterOpcionesClave = new ListAdapterOpcionesClave(OpcionesClave.this, dataArrayList);

        binding.listviewOpcionesClave.setAdapter(listAdapterOpcionesClave);

        // Configuración del onItemClick Listener
        binding.listviewOpcionesClave.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // En la actividad OpcionesClave, cuando se seleccione una clave
                Intent returnIntent = new Intent();
                returnIntent.putExtra("selectedClave", opcionesClaveList.get(i).getNumeroDeClave());
                returnIntent.putExtra("selectedIdentificador", opcionesClaveList.get(i).getIdentificador());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        verificacion();

    }

    // Verificar la conexión a Internet utilizando ConnectivityManager.NetworkCallback
    public boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }

    // Implementa el método de la interfaz para manejar los mensajes de error
    @Override
    public void onDatabaseError(final String errorMessage) {
        // Manejar el mensaje de error, por ejemplo, mostrarlo en un Log o en un TextView
        Log.e(TAG, "Database Error: " + errorMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mostrarDialogoError(errorMessage);
            }
        });
    }

    private void mostrarDialogoError(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(OpcionesClave.this);
        builder.setIcon(R.drawable.baseline_error_24);
        builder.setTitle("Se encuentra un error con la base de datos");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void showCustomProgressDialog() {
        // Crear un AlertDialog personalizado con un diseño personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(OpcionesClave.this);

        // Inflar la vista cada vez que se muestra el diálogo
        View view = getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);
        // Ajustar el tamaño del AlertDialog
        customProgressDialog = builder.create();
        customProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // Mostrar el AlertDialog
        customProgressDialog.show();

        // Mostrar el AlertDialog
        customProgressDialog.show();
    }

    private void hideCustomProgressDialog() {
        if (customProgressDialog != null && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
    }

    @Override
    public void onDataUpdated(ArrayList<ListDataEntradas> updatedData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Código que actualiza la interfaz de usuario

                hideCustomProgressDialog();

                ArrayList<OpcionesClaveData> opcionesClaveList = databaseHelper.getAllOpcionesClaves();
                dataArrayList.clear(); // Limpia la lista antes de agregar nuevos datos

                for (OpcionesClaveData opcionesClaveData : opcionesClaveList) {
                    listDataOpcionesClave = new ListDataOpcionesClave(
                            opcionesClaveData.getNumeroDeClave(),
                            opcionesClaveData.getIdentificador()
                    );
                    dataArrayList.add(listDataOpcionesClave);
                }

                listAdapterOpcionesClave.notifyDataSetChanged(); // Notificar al adaptador sobre los cambios en los datos

                // Configuración del onItemClick Listener
                binding.listviewOpcionesClave.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        // En la actividad OpcionesClave, cuando se seleccione una clave
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("selectedClave", opcionesClaveList.get(i).getNumeroDeClave());
                        returnIntent.putExtra("selectedIdentificador", opcionesClaveList.get(i).getIdentificador());
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                });


            }
        });
    }

    public void verificacion() {
        // Verificar si la lista está vacía la primera vez que se inicia la actividad
        if (dataArrayList.isEmpty()) {
            showCustomProgressDialog();
            Log.d(TAG, "onCreate: la lista esta vacia");
            // La lista está vacía, recargar la base de datos
            if (isInternetConnected()) {
                databaseHelper.setDataUpdateListener(OpcionesClave.this);
                databaseHelper.fetchDataFromServer();
                databaseHelper.fetchIdClaveDescripcionFromServer();
                databaseHelper.setDatabaseCallback(OpcionesClave.this);
                Log.d(TAG, "actualización: sale");
            } else {
                hideCustomProgressDialog();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.baseline_error_24);
                builder.setTitle("Conexión a internet perdida");
                builder.setMessage("Los datos de clave no han sido cargados correctamente, comprueba que tu dispositivo esté conectado a internet y vuelve a intentarlo para descargarlos. ");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.show();
            }
        }
    }
}