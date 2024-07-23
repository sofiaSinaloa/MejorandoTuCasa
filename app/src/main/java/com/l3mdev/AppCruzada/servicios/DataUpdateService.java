package com.l3mdev.AppCruzada.servicios;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.JobIntentService;

import com.l3mdev.AppCruzada.DB.DatabaseHelper;
import com.l3mdev.AppCruzada.DB.LecturaBeneficiarios;
import com.l3mdev.AppCruzada.DB.LecturaModel;
import com.l3mdev.AppCruzada.Entradas;
import com.l3mdev.AppCruzada.ListDataEntradas;

import java.util.ArrayList;

public class DataUpdateService extends JobIntentService implements DatabaseHelper.DataUpdateListener {

    Handler handler;
    private ConnectivityManager.NetworkCallback networkCallback;

    private static final String TAG = "DataUpdateJobService";
    private ArrayList<ListDataEntradas> dataArrayList = new ArrayList<>();
    private DatabaseHelper databaseHelper = new DatabaseHelper(this);
    private DatabaseHelper.DataUpdateListener dataUpdateListener;

    private final int INTERVALO_ACTUALIZACION = 5 * 60 * 1000; // 5 MINUTOS EN MILISEGUNDOS

    public static final String ACTION_UPDATE_LISTVIEW = "com.jmaschihuahua.jmaslecturas.UPDATE_LISTVIEW";

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DataUpdateService.class, 1000, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // Inicia la verificación de conexión a internet cada cinco minutos
        Log.d(TAG, "onHandleWork: inicia 1");
        startIntentCheckTask();
    }

    private void updateData() {
        // Reiniciar los datos de la lista y notificar al adaptador
        dataArrayList.clear();
        ArrayList<LecturaBeneficiarios> datosPendientesNuevo = databaseHelper.getPendingBeneficiarios();
        ArrayList<LecturaBeneficiarios> datosPendientesObtenidos = databaseHelper.getPendingBeneficiariosEnObtenido();
            if (!datosPendientesNuevo.isEmpty()) {
                for (LecturaBeneficiarios lectura : datosPendientesNuevo) {
                    Log.d(TAG, "actualización: status: " + lectura.getStatus());
                    sendBeneficiariosNuevo(lectura);
                }
            }
            if (!datosPendientesObtenidos.isEmpty()) {
                for (LecturaBeneficiarios lectura : datosPendientesObtenidos) {
                    sendBeneficiariosObtenido(lectura);
                }
            }

        databaseHelper.setDataUpdateListener(this);
        // Cierra la instancia de DatabaseHelper
        databaseHelper.close();
    }

    private void startDataUpdateService() {
        Log.d(TAG, "startDataUpdateService: 4");
        updateData();
    }

    private void startIntentCheckTask() {
        Log.d(TAG, "onHandleWork: empieza el tiempo 2");
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startIntentCheckTaskTwo();
            }
        }, INTERVALO_ACTUALIZACION);
    }

    private void startIntentCheckTaskTwo() {
        Log.d(TAG, "startIntentCheckTaskTwo: entra 3");
        // Desregistrar el callback existente si hay uno
        unregisterNetworkCallback();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.d(TAG, "onAvailable: entra 3");
                startDataUpdateService();
            }

            @Override
            public void onLost(Network network) {
                Log.d(TAG, "onLost: se perdió la conexión a internet 3");
                // Programa la verificación de conexión nuevamente después de un pequeño intervalo

            }
        };

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
            startIntentCheckTask();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                handleException(e);
            }
            networkCallback = null;
        }
    }

    private void handleException(Exception e) {
        // Manejar la excepción según tus necesidades
        Log.e(TAG, "Error al registrar/desregistrar NetworkCallback", e);
    }

    // Verificar la conexión a Internet utilizando ConnectivityManager.NetworkCallback
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public void sendBeneficiariosNuevo(LecturaBeneficiarios lectura) {
        if (lectura != null) {
            if (isInternetConnected()) {
                ArrayList<LecturaBeneficiarios> lecturaList = new ArrayList<>();
                lecturaList.add(lectura);
                databaseHelper.sendBeneficiariosToServer(lecturaList);
            }
        }
    }

    public void sendBeneficiariosObtenido(LecturaBeneficiarios lectura) {
        if (lectura != null) {
            if (isInternetConnected()) {
                ArrayList<LecturaBeneficiarios> lecturaList = new ArrayList<>();
                lecturaList.add(lectura);
                databaseHelper.updateBeneficiariosToServer(lecturaList);
            }
        }
    }

    private void sendLecturaToServer(LecturaModel lectura) {
        if (lectura != null) {
            String statusEnvio = lectura.getStatusEnvio();

            if (isInternetConnected()) {
//                // Verificar si es una operación de grupo o individual
//                if ("grupo".equals(statusEnvio)) {
//                    int intIdPaquete = Integer.parseInt(lectura.getIntIdPaquete());
//                    if (!databaseHelper.hasOtherGroupRecordsWithSameIntIdPaquete(intIdPaquete)) {
//                        // Crear una lista con un solo objeto LecturaModel y enviarlo
//                        ArrayList<LecturaModel> lecturaList = new ArrayList<>();
//                        lecturaList.add(lectura);
//                        databaseHelper.sendDataToServer(lecturaList);
//                    }
//                } else if ("individual".equals(statusEnvio)) {
                    // Crear una lista con un solo objeto LecturaModel y enviarlo
                    ArrayList<LecturaModel> lecturaList = new ArrayList<>();
                    lecturaList.add(lectura);
                    databaseHelper.sendDataToServer(lecturaList);
//                }
            }
        }
    }

    @Override
    public void onDataUpdated(ArrayList<ListDataEntradas> updatedData) {
        // Agregar nuevos datos a la tabla estática
        Intent intent = new Intent(ACTION_UPDATE_LISTVIEW);
        sendBroadcast(intent);
    }

    @Override
    public void onDatabaseError(String errorMessage) {
        // Notificar el error a través de la interfaz
        if (dataUpdateListener != null) {
            dataUpdateListener.onDatabaseError(errorMessage);
        }
    }
    // Método para establecer el oyente de actualización de datos
    public void setDataUpdateListener(DatabaseHelper.DataUpdateListener listener) {
        this.dataUpdateListener = listener;
    }
}
