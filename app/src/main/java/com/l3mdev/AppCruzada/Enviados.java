package com.l3mdev.AppCruzada;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.material.snackbar.Snackbar;
import com.l3mdev.AppCruzada.DB.DatabaseHelper;
import com.l3mdev.AppCruzada.databinding.ActivityEnviadosBinding;
import com.l3mdev.AppCruzada.servicios.ConnectivityService;

import java.util.ArrayList;

public class Enviados extends AppCompatActivity {
    private boolean previousSignalWeakState = false; // Variable para almacenar el estado de la señal débil anterior
    private static final int SIGNAL_STRENGTH_THRESHOLD = 5;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;


    private String currentSearchQuery = "";
    ActivityEnviadosBinding binding;
    ListAdapterEnviados listAdapterEnviados;
    ArrayList<ListDataEnviados> dataArrayList = new ArrayList<>();

    private ArrayList<ListDataEnviados> originalArrayList = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private Snackbar weakNetworkSnackbar;
    private boolean isSignalWeakStatic;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(ContentValues.TAG, "onResume: tres");

        IntentFilter filter = new IntentFilter("com.example.SIGNAL_STRENGTH_CHANGED");
        registerReceiver(signalStrengthReceiver, filter);

        showSignalWeakToast();

        // Obtener los datos de la tabla "lecturas_endpoint" desde la base de datos
        ArrayList<ListDataEnviados> lecturasEndpointList = databaseHelper.getAllLecturasEndpointStatusEnviados();

        // Creación del adaptador de lista
        listAdapterEnviados = new ListAdapterEnviados(Enviados.this, lecturasEndpointList);

        // Asignación del adaptador a la vista de lista
        binding.listviewEnviados.setAdapter(listAdapterEnviados);

        listAdapterEnviados.notifyDataSetChanged(); // Notificar al adaptador para que actualice el ListView

        // Aplicar el filtro nuevamente si hay un estado de búsqueda almacenado
        if (!currentSearchQuery.isEmpty()) {
            listAdapterEnviados.getFilter().filter(currentSearchQuery);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnviadosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        // Obtener los datos de la tabla "lecturas_endpoint" desde la base de datos
        ArrayList<ListDataEnviados> lecturasEndpointList = databaseHelper.getAllLecturasEndpointStatusEnviados();

        // Creación del adaptador de lista
        listAdapterEnviados = new ListAdapterEnviados(Enviados.this, lecturasEndpointList);

        // Asignación del adaptador a la vista de lista
        binding.listviewEnviados.setAdapter(listAdapterEnviados);
        listAdapterEnviados.notifyDataSetChanged(); // Notificar al adaptador para que actualice el ListView

        // Configuración del listener de clics en elementos de la lista
        binding.listviewEnviados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Creación del intento para iniciar la actividad detallada
                Intent intent = new Intent(Enviados.this, DetailedEnviados.class);
                ListDataEnviados lectura = lecturasEndpointList.get(i);
                intent.putExtra("sector", lectura.getSector());
                intent.putExtra("ruta", lectura.getRuta());
                intent.putExtra("secuencia", lectura.getSecuencia());
                intent.putExtra("contrato", lectura.getContrato());
                intent.putExtra("domicilio", lectura.getDomicilio());
                intent.putExtra("numerodemedidor", lectura.getNumerodemedidor());
                intent.putExtra("anterior", lectura.getAnterior());
                intent.putExtra("advertenciaList", lectura.getAdvertencia());
                intent.putExtra("fechaActualizacion", lectura.getFecha());
                startActivity(intent);
            }
        });

        // Configuración de la barra de herramientas
        // Despliega boton de retroceso para la actividad
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarEnviados);
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
        originalArrayList.addAll(dataArrayList);

        IntentFilter filter = new IntentFilter("com.example.SIGNAL_STRENGTH_CHANGED");
        registerReceiver(signalStrengthReceiver, filter);

        initTelephonyManager();

        // Mostrar el Toast correspondiente al estado de la señal débil
        showSignalWeakToast();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_enviados, menu);

        MenuItem menuItem = menu.findItem(R.id.itemBusquedaToolbarMenuEnviados);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Escribe aquí tu búsqueda");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(ContentValues.TAG, "onQueryTextChange: uno");
                currentSearchQuery = newText; // Almacena el estado de la búsqueda

                listAdapterEnviados.updateData(dataArrayList);
                listAdapterEnviados.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
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
        // Deregistrar el receptor de broadcast
        unregisterReceiver(signalStrengthReceiver);
    }

    // Método para mostrar el Toast correspondiente al estado de la señal débil
    private void showSignalWeakToast() {
        // Verificar si la conexión a la red es débil
        if (isSignalWeakStatic) {
            // La conexión a la red es débil, mostrar el Snackbar
            if (weakNetworkSnackbar == null || !weakNetworkSnackbar.isShownOrQueued()) {
                weakNetworkSnackbar = Snackbar.make(findViewById(android.R.id.content), "Señal de red móvil débil, por favor, considere cambiar a una red estable.", Snackbar.LENGTH_INDEFINITE);
                weakNetworkSnackbar.show();
            }
        } else {
            // La conexión a la red es normal, ocultar el Snackbar
            if (weakNetworkSnackbar != null && weakNetworkSnackbar.isShownOrQueued()) {
                weakNetworkSnackbar.dismiss();
                weakNetworkSnackbar = null;
            }
        }
    }

    private boolean isVerificationDone = false; // Bandera para controlar si la verificación ya se ha realizado

    // Método para inicializar el TelephonyManager y registrar el PhoneStateListener
    public void initTelephonyManager() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            listenSignalStrength();
        }
    }

    private void listenSignalStrength() {
        Log.d(ContentValues.TAG, "listenSignalStrength: enviados dos");
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                Log.d(ContentValues.TAG, "onSignalStrengthsChanged: enviados tres");
                super.onSignalStrengthsChanged(signalStrength);
                int signalStrengthValue = getSignalStrengthValue(signalStrength);
                Log.d(ContentValues.TAG, "onSignalStrengthsChanged: enviados seis");
                handleNetworkQuality(signalStrengthValue);
                Log.d(ContentValues.TAG, "onSignalStrengthsChanged: enviados sale ");
                // Establece la bandera como verdadera para indicar que la verificación ya se ha realizado
                //isVerificationDone = true;
                // Detener la escucha después de la primera verificación
                stopListeningSignalStrength();
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    // Método para detener la escucha después de la primera verificación
    private void stopListeningSignalStrength() {
        Log.d(ContentValues.TAG, "stopListeningSignalStrength: se ha detenido uno");
        if (telephonyManager != null && phoneStateListener != null) {
            Log.d(ContentValues.TAG, "stopListeningSignalStrength: se ha detenido dos");
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private int getSignalStrengthValue(SignalStrength signalStrength) {
        Log.d(ContentValues.TAG, "getSignalStrengthValue: enviados cuatro");
        if (signalStrength.isGsm()) {
            Log.d(ContentValues.TAG, "getSignalStrengthValue: enviados cinco uno");
            return signalStrength.getGsmSignalStrength();
        } else {
            Log.d(ContentValues.TAG, "getSignalStrengthValue: enviados cinco dos");
            return signalStrength.getCdmaDbm();
        }
    }

    // Método para manejar la calidad de la red
    public void handleNetworkQuality(int signalStrengthValue) {
        Log.d(ContentValues.TAG, "handleNetworkQuality: enviados siete");
        boolean signal;
        if (signalStrengthValue <= SIGNAL_STRENGTH_THRESHOLD) {
            Log.d(ContentValues.TAG, "handleNetworkQuality: enviados ocho uno");
            signal = true;
            isSignalWeakStatic = signal; // Señal débil detectada
        } else {
            Log.d(ContentValues.TAG, "handleNetworkQuality: enviados ocho dos");
            signal = false; // Señal normal
            isSignalWeakStatic = signal;
        }
        showSignalWeakToast();
    }
}