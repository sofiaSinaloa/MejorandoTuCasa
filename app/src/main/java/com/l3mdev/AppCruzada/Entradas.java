package com.l3mdev.AppCruzada;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.l3mdev.AppCruzada.DB.DatabaseHelper;
import com.l3mdev.AppCruzada.DB.LecturaBeneficiarios;
import com.l3mdev.AppCruzada.databinding.ActivityEntradasBinding;
import com.l3mdev.AppCruzada.servicios.DataUpdateService;

import java.util.ArrayList;

public class Entradas extends AppCompatActivity implements DatabaseHelper.DataUpdateListener, DatabaseHelper.DatabaseCallback{

    private static boolean isSignalWeakStatic;
    private Snackbar weakNetworkSnackbar;
    private static final int SIGNAL_STRENGTH_THRESHOLD = 5;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    private ActivityEntradasBinding binding;
    private String currentSearchQuery = "";

    private AlertDialog customProgressDialog;

    ListAdapterEntradas listAdapterEntradas;
    ArrayList<ListDataEntradas> dataArrayList = new ArrayList<>();
    private int originasize;
    private ArrayList<ListDataEntradas> originalArrayList = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    // Estado del Snackbar

    private void showCustomProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Entradas.this);
        View view = getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);
        customProgressDialog = builder.create();
        customProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        customProgressDialog.show();
    }

    private void hideCustomProgressDialog() {
        if (customProgressDialog != null && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
    }

    @Override
    public void onDataUpdated(final ArrayList<ListDataEntradas> updatedData) {
        runOnUiThread(() -> {
            Log.d(TAG, "onDataUpdated: uno");
            setupListView();
        });
    }

    private BroadcastReceiver updateListViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DataUpdateService.ACTION_UPDATE_LISTVIEW)) {
                // Actualizar los datos desde databaseHelper
                ArrayList<ListDataEntradas> updatedData = databaseHelper.getAllBeneficiariosEndpoint();

                // Limpiar y actualizar el ArrayList del adaptador
                dataArrayList.clear();
                dataArrayList.addAll(updatedData);

                // Notificar al adaptador que los datos han cambiado
                listAdapterEntradas.updateData(updatedData);
                listAdapterEntradas.notifyDataSetChanged();

                setupListView();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: tres");
        IntentFilter filter = new IntentFilter("com.example.SIGNAL_STRENGTH_CHANGED");
        registerReceiver(signalStrengthReceiver, filter);

        // Cargar datos al inicio de la actividad
        ArrayList<ListDataEntradas> updateData = databaseHelper.getAllBeneficiariosEndpoint();
        dataArrayList.clear();
        dataArrayList.addAll(updateData);

        if (!currentSearchQuery.isEmpty()){
            listAdapterEntradas.getFilter().filter(currentSearchQuery);
        }
        listAdapterEntradas.updateData(updateData);
        listAdapterEntradas.notifyDataSetChanged();

        setupListView();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: cuatro");
        super.onCreate(savedInstanceState);
        binding = ActivityEntradasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarEntradas);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupListView();
        originalArrayList.addAll(dataArrayList);

        if (dataArrayList.isEmpty()) {
            if (isInternetConnected()) {
                databaseHelper.fetchBeneficiarioFromServer();
                databaseHelper.setDatabaseCallback(Entradas.this);
                databaseHelper.setDataUpdateListener(Entradas.this);
                actualización();
            } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setIcon(R.drawable.baseline_error_24);
                    builder.setTitle("Error");
                    builder.setMessage("La conexión a internet es débil o se ha perdido, no se ha logrado actualizar los datos. Inténtelo más tarde.");
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
            }

        }

        IntentFilter filter = new IntentFilter("com.example.SIGNAL_STRENGTH_CHANGED");
        registerReceiver(signalStrengthReceiver, filter);

        initTelephonyManager();
        showSignalWeakToast();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_entradas, menu);
        MenuItem menuItemReiniciar = menu.findItem(R.id.itemReiniciarToolbarMenu);
        menuItemReiniciar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showPopupAndResetData();
                return true;
            }
        });

        MenuItem menuItem = menu.findItem(R.id.itemBusquedaToolbarMenu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Escribe aquí tu búsqueda");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                listAdapterEntradas.updateData(dataArrayList);
                listAdapterEntradas.getFilter().filter(newText);
                return false;
            }
        });

        MenuItem menuItemAgregar = menu.findItem(R.id.itemAgregarToolbarMenu);
        menuItemAgregar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Entradas.this, Detailed.class);
                startActivityForResult(intent, 1); // Usamos requestCode 1 aquí
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showCustomProgressDialog();
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetConnected()){
                            databaseHelper.fetchBeneficiarioFromServer();
                            databaseHelper.setDatabaseCallback(Entradas.this);
                            databaseHelper.setDataUpdateListener(Entradas.this);
                            actualización();
                            setupListView();
                        }
                    }
                }, 8000);
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetConnected()){
                            databaseHelper.fetchBeneficiarioFromServer();
                            databaseHelper.setDatabaseCallback(Entradas.this);
                            databaseHelper.setDataUpdateListener(Entradas.this);
                            actualización();
                            setupListView();
                        }
                    }
                }, 8000);
            }
        }
        hideCustomProgressDialog();
    }

    public void showPopupAndResetData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_refresh_24);
        builder.setTitle("Actualizar datos");
        builder.setMessage("¿Deseas reiniciar los datos ahora?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isInternetConnected()) {
                    showCustomProgressDialog();
                    actualización();
                    databaseHelper.fetchBeneficiarioFromServer();
                    databaseHelper.setDatabaseCallback(Entradas.this);
                    databaseHelper.setDataUpdateListener(Entradas.this);
                //databaseHelper.deleteOldRecords();
                } else {
                    opcionesdeConexion();
                }
            }
        });
        builder.setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.show();
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

    public void actualización() {
        DatabaseHelper databaseHelper = new DatabaseHelper(Entradas.this);
        listAdapterEntradas.notifyDataSetChanged();

        if (dataArrayList.isEmpty()){
            Toast.makeText(this, "No se han encontrado datos. Vuelva a intentarlo más tarde", Toast.LENGTH_SHORT).show();
        }

        ArrayList<LecturaBeneficiarios> datosPendientesNuevo = databaseHelper.getPendingBeneficiarios();
        ArrayList<LecturaBeneficiarios> datosPendientesObtenidos = databaseHelper.getPendingBeneficiariosEnObtenido();
        if (isInternetConnected()) {
            if (!datosPendientesNuevo.isEmpty()) {
               for (LecturaBeneficiarios lectura : datosPendientesNuevo) {
                    sendBeneficiariosNuevo(lectura);
               }
            }
            if (!datosPendientesObtenidos.isEmpty()) {
                for (LecturaBeneficiarios lectura : datosPendientesObtenidos) {
                    sendBeneficiariosObtenido(lectura);
                }
            }
        } else {
            opcionesdeConexion();
        }
        hideCustomProgressDialog();
    }

    public void sendBeneficiariosNuevo(LecturaBeneficiarios lectura) {
        if (lectura != null) {
            if (isInternetConnected()) {
                ArrayList<LecturaBeneficiarios> lecturaList = new ArrayList<>();
                lecturaList.add(lectura);
                databaseHelper.sendBeneficiariosToServer(lecturaList);
                databaseHelper.setDatabaseCallback(Entradas.this);
                databaseHelper.setDataUpdateListener(Entradas.this);
            }
        }
    }

    public void sendBeneficiariosObtenido(LecturaBeneficiarios lectura) {
        if (lectura != null) {
            if (isInternetConnected()) {
                ArrayList<LecturaBeneficiarios> lecturaList = new ArrayList<>();
                lecturaList.add(lectura);
                databaseHelper.updateBeneficiariosToServer(lecturaList);
                databaseHelper.setDatabaseCallback(Entradas.this);
                databaseHelper.setDataUpdateListener(Entradas.this);
            }
        }
    }

    private void opcionesdeConexion() {
        android.app.AlertDialog.Builder innerBuilder = new android.app.AlertDialog.Builder(Entradas.this);
        innerBuilder.setIcon(R.drawable.baseline_error_24);
        innerBuilder.setTitle("Conexión a internet perdida");
        innerBuilder.setMessage("Comprueba que tu dispositivo esté conectado a internet y vuelve a intentarlo.")
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
        if (restablecerConexion()) {
            showPopupAndResetData();
        } else {
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

    public void setupListView() {
        IntentFilter filter = new IntentFilter(DataUpdateService.ACTION_UPDATE_LISTVIEW);
        registerReceiver(updateListViewReceiver, filter);

        eliminarpreferencias();

        Log.d(TAG, "setupListView: se ve");

        ArrayList<ListDataEntradas> lecturasEndpointList = databaseHelper.getAllBeneficiariosEndpoint();

        dataArrayList.clear();
        dataArrayList.addAll(lecturasEndpointList);
        originasize = lecturasEndpointList.size();

        listAdapterEntradas = new ListAdapterEntradas(Entradas.this, dataArrayList);
        binding.listviewEntradas.setAdapter(listAdapterEntradas);

        listAdapterEntradas.notifyDataSetChanged(); // Notificar al adaptador para que actualice el ListView

        binding.listviewEntradas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(Entradas.this, DetailedEnviados.class);

                ListDataEntradas lectura = lecturasEndpointList.get(i);
                intent.putExtra("id", lectura.getNid());
                intent.putExtra("curp", lectura.getNcurp());
                intent.putExtra("rfc", lectura.getNrfc());
                intent.putExtra("nombre", lectura.getNnombre());
                intent.putExtra("paterno", lectura.getNpaterno());
                intent.putExtra("materno", lectura.getNmaterno());
                intent.putExtra("calle", lectura.getNcalle());
                intent.putExtra("numExt", lectura.getNnumExt());
                intent.putExtra("numInt", lectura.getNnumInt());

                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    public void onDatabaseError(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mostrarDialogoError(errorMessage);
            }
        });
    }

    private void mostrarDialogoError(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Entradas.this);
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

    private BroadcastReceiver signalStrengthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.SIGNAL_STRENGTH_CHANGED".equals(intent.getAction())) {
                boolean isSignalWeak = intent.getBooleanExtra("isSignalWeak", false);
                isSignalWeakStatic = isSignalWeak;
                isVerificationDone = true;
                showSignalWeakToast();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(signalStrengthReceiver);
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

    private boolean isVerificationDone = false; // Bandera para controlar si la verificación ya se ha realizado

    public void initTelephonyManager() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            listenSignalStrength();
        }
    }

    private void listenSignalStrength() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                int signalStrengthValue = getSignalStrengthValue(signalStrength);
                handleNetworkQuality(signalStrengthValue);
                stopListeningSignalStrength();
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    // Método para detener la escucha después de la primera verificación
    private void stopListeningSignalStrength() {
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private int getSignalStrengthValue(SignalStrength signalStrength) {
        if (signalStrength.isGsm()) {
            return signalStrength.getGsmSignalStrength();
        } else {
            return signalStrength.getCdmaDbm();
        }
    }

    // Método para manejar la calidad de la red
    public void handleNetworkQuality(int signalStrengthValue) {
        boolean signal;
        if (signalStrengthValue <= SIGNAL_STRENGTH_THRESHOLD) {
            signal = true;
            isSignalWeakStatic = signal; // Señal débil detectada
        } else {
            signal = false; // Señal normal
            isSignalWeakStatic = signal;
        }
        showSignalWeakToast();
    }

    private void eliminarpreferencias(){
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor.remove("currentPhotoPathReverso");
        editor.apply();
    }
}
