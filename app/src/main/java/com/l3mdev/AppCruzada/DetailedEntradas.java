package com.l3mdev.AppCruzada;

import static android.content.ContentValues.TAG;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.l3mdev.AppCruzada.DB.DatabaseHelper;
import com.l3mdev.AppCruzada.DB.LecturaEndpoint;
import com.l3mdev.AppCruzada.DB.LecturaModel;
import com.l3mdev.AppCruzada.databinding.ActivityDetailedEntradasBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailedEntradas extends AppCompatActivity implements DatabaseHelper.DatabaseCallback, DatabaseHelper.DataUpdateListener {
    private static final int SIGNAL_STRENGTH_THRESHOLD = 5;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean isSignalWeak;


    // Variable para base de datos
    private DatabaseHelper databaseHelper;
    private Snackbar weakNetworkSnackbar;

    // Variable estática para almacenar el estado de la señal débil
    private static boolean isSignalWeakStatic;
    private Entradas entradas = new Entradas();

    private androidx.appcompat.app.AlertDialog customProgressDialog; // Cambiado de ProgressDialog a AlertDialog

    // Variables para el despliegue de los permisos de la cámara, tanto para el almacenamiento como para abrir la cámara.
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_REQUEST_CODE_PHOTO = 3;
    private static final int CAMERA_REQUEST_CODE_ADV = 2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_SETTINGS_REQUEST = 1003;
    private boolean fotoTomadaClave = false;
    private boolean fotoTomadaAdv = false;
    private boolean claveAñadida = false;
    private boolean cambios = false;
    boolean aceptaLectura;
    private String vacioClaveIncidencia;
    public String padronSeleccionado;

    private String currentPhotoPath, currentPhotoPathAdv;

    CheckBox checkBoxClave;
    TextView clave, advertenciaclave, seleccionefotografia, advertenciafotografia, añadaclave, descripcionclave;
    TextView añadafotografiaadv, advertenciafotoadv, textViewAdvertencia, editTextLecturaNueva2;

    private EditText editTextAdvertencia;

    ImageView fotografia, fotografiaadv;

    Button enviarButton;

    private String valorOriginalClave, valorOriginaldescripcionclave;
    private Drawable valorOrignalfotografia, valorOrignalfotografiaAdvertencia;
    private ConnectivityManager.NetworkCallback networkCallback;

    ActivityDetailedEntradasBinding binding;

    // Variable para la comprobación de los datos, si estos han sido cambiados del formulario.
    private boolean dataChanged = false;

    private FusedLocationProviderClient fusedLocationClient;

    String latitud;
    String longitud;

    private List<ListDataEntradas> lecturasEndpointList;
    private int currentIndex = -1;  // Añade una variable de instancia para rastrear el índice actual

    int selectedPosition;
    boolean isPositionTaken = false;
    public String mensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailedEntradasBinding.inflate(getLayoutInflater());
        // Despliega el nombre del formulario seleccionado en la actividad anterior.
        setContentView(binding.getRoot());


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // Registrar el receptor de broadcast
        IntentFilter filter = new IntentFilter("com.example.SIGNAL_STRENGTH_CHANGED");
        registerReceiver(signalStrengthReceiver, filter);

        initTelephonyManager();

        // Verifica si la posición ya ha sido tomada antes de tomarla nuevamente
        if (!isPositionTaken) {
            selectedPosition = getIntent().getIntExtra("selectedPosition", -1);
            isPositionTaken = true; // Marca que la posición ha sido tomada
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            checkLocationSettings();
        } else {
            // El permiso de ubicación no fue concedido, solicitarlo al usuario
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        //Crear una intancia del DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        lecturasEndpointList = databaseHelper.getAllLecturasEndpoint();
        Intent intent = this.getIntent();
        if (intent != null) {
            String contratoList = intent.getStringExtra("contrato");
            String padron = intent.getStringExtra("getPadronSeleccionado");

            // Verificar si hay registros en la tabla lecturas_guardado con el mismo número de contrato
            LecturaEndpoint lecturaEndpoint = databaseHelper.getEndpointByNumContrato(contratoList, padron);
            binding.editTextSector.setText(lecturaEndpoint.getVchNombreSector());
            binding.editTextRuta.setText(lecturaEndpoint.getVchNombreRuta());
            binding.editTextSecuencia.setText(String.valueOf(lecturaEndpoint.getSitNumSecuenciaRuta()));
            binding.editTextContrato.setText(lecturaEndpoint.getNumContrato());
            binding.editTextDireccion.setText(lecturaEndpoint.getVchDetalleDireccion());
            binding.editTextNumerodeMedidor.setText(lecturaEndpoint.getVchNumMedidor());
            binding.editTextLecturaAnterior.setText(String.valueOf(lecturaEndpoint.getLecturaAnterior()));
            binding.editTextAdvertencia.setText(lecturaEndpoint.getAdvertencia());
        }

        // Despliega la opción de regresar a la anterior pantalla por medio de un botón creado por la misma interfaz de código.
        Toolbar toolbar = findViewById(R.id.toolbarDetailedEntradas);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar cambios
                checkDataChanges();
                if (dataChanged) {
                    // Mostrar el diálogo de opciones (guardar, cancelar, cerrar)
                    showOptionsDialog();
                } else {
                    finish();
                }
            }
        });


        enviarButton = findViewById(R.id.enviarButton);
        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                showCustomProgressDialog();
                saveData();
            }
        });

        checkBoxClave = findViewById(R.id.checkBoxClave);
        clave = findViewById(R.id.clave);
        descripcionclave = findViewById(R.id.descripcionclave);
        advertenciaclave = findViewById(R.id.advertenciaclave);
        fotografia = findViewById(R.id.photoImageView);
        seleccionefotografia = findViewById(R.id.seleccionefotografia);
        advertenciafotografia = findViewById(R.id.advertenciafotografia);
        añadaclave = findViewById(R.id.añadaclave);
        valorOriginalClave = clave.getText().toString();
        valorOriginaldescripcionclave = descripcionclave.getText().toString();
        valorOrignalfotografia = fotografia.getDrawable();

        añadafotografiaadv = findViewById(R.id.añadafotografiaadv);
        advertenciafotoadv = findViewById(R.id.advertenciafotoadv);
        fotografiaadv = findViewById(R.id.fotografiaadv);

        editTextLecturaNueva2 = findViewById(R.id.editTextLecturaNueva);
        editTextAdvertencia = findViewById(R.id.editTextAdvertencia);
        textViewAdvertencia = findViewById(R.id.TextViewAdvertencia);

        clave.setVisibility(View.GONE);
        descripcionclave.setVisibility(View.GONE);
        advertenciaclave.setVisibility(View.GONE);
        fotografia.setVisibility(View.GONE);
        seleccionefotografia.setVisibility(View.GONE);
        advertenciafotografia.setVisibility(View.GONE);
        añadaclave.setVisibility(View.GONE);

        añadafotografiaadv.setVisibility(View.GONE);
        advertenciafotoadv.setVisibility(View.GONE);
        fotografiaadv.setVisibility(View.GONE);
        valorOrignalfotografiaAdvertencia = fotografiaadv.getDrawable();

        editTextLecturaNueva2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count,
                                          int after) {
                fotografiaadv.setImageDrawable(valorOrignalfotografiaAdvertencia);
                editTextAdvertencia.setText("");
                añadafotografiaadv.setVisibility(View.GONE);
                advertenciafotoadv.setVisibility(View.GONE);
                fotografiaadv.setVisibility(View.GONE);
                cambios = false;
                fotoTomadaAdv = false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before,
                                      int count) {
                // Verificar si el texto en editTextLecturaNueva2 está vacía
                currentPhotoPathAdv = "";
                fotografiaadv.setImageDrawable(valorOrignalfotografiaAdvertencia);
                editTextAdvertencia.setText("");
                añadafotografiaadv.setVisibility(View.GONE);
                advertenciafotoadv.setVisibility(View.GONE);
                fotografiaadv.setVisibility(View.GONE);
                cambios = false;
                fotoTomadaAdv = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

                // Verificar si el texto en editTextLecturaNueva2 está vacío
                if (editable.toString().isEmpty()) {
                    // Si está vacío, limpiar el contenido de editTextAdvertencia
                    editTextAdvertencia.setText("");
                    añadafotografiaadv.setVisibility(View.GONE);
                    advertenciafotoadv.setVisibility(View.GONE);
                    fotografiaadv.setVisibility(View.GONE);
                    fotoTomadaAdv = false;
                    cambios = false;
                } else {
                    // Obtener el valor de la lectura nueva como un número
                    double lecturaNueva = 0;
                    try {
                        lecturaNueva = Double.parseDouble(editable.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                        String numContrato = binding.editTextContrato.getText().toString();
                        String padron = intent.getStringExtra("getPadronSeleccionado");
                        double consumoPromedio = databaseHelper.getConsumoMes(numContrato, padron);
                        double lecturaAnteriors = databaseHelper.getecturaAnterior(numContrato, padron);

// Calcular los umbrales para el estado normal
                        double umbralInferiorNormal = lecturaAnteriors - 40;
                        double umbralSuperiorNormal = lecturaAnteriors + 40;

// Calcular el cambio en el consumo actual respecto al consumo anterior
                        double cambioConsumo = lecturaNueva - lecturaAnteriors;


                        if (lecturaNueva >= umbralInferiorNormal && lecturaNueva <= umbralSuperiorNormal) {
                            // No hay advertencia
                            editTextAdvertencia.setText("");
                            añadafotografiaadv.setVisibility(View.GONE);
                            advertenciafotoadv.setVisibility(View.GONE);
                            fotografiaadv.setVisibility(View.GONE);
                            cambios = false;
                            fotoTomadaAdv = false;
                        } else {
                            if (cambioConsumo > 40 && lecturaNueva > (1 + 0.30) * consumoPromedio) {
                                // Mostrar advertencia: Consumo mayor
                                editTextAdvertencia.setText("Mayor al consumo del mes");
                                añadafotografiaadv.setVisibility(View.VISIBLE);
                                advertenciafotoadv.setVisibility(View.VISIBLE);
                                fotografiaadv.setVisibility(View.VISIBLE);
                                cambios = true;
                            } else if (cambioConsumo < -40) {
                                // Mostrar advertencia: Consumo menor
                                editTextAdvertencia.setText("Menor al consumo del mes");
                                añadafotografiaadv.setVisibility(View.VISIBLE);
                                advertenciafotoadv.setVisibility(View.VISIBLE);
                                fotografiaadv.setVisibility(View.VISIBLE);
                                cambios = true;
                            } else {
                                // No hay advertencia
                                editTextAdvertencia.setText("");
                                añadafotografiaadv.setVisibility(View.GONE);
                                advertenciafotoadv.setVisibility(View.GONE);
                                fotografiaadv.setVisibility(View.GONE);
                                cambios = false;
                                fotoTomadaAdv = false;
                            }
                        }
                }
            }
        });
        fotografiaadv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar si se ha concedido el permiso de la cámara
                if (ContextCompat.checkSelfPermission(DetailedEntradas.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    // El permiso de la cámara ya ha sido concedido
                    hideKeyboard();
                    // Al hacer clic en el ImageView, restablece la URL actual
                    currentPhotoPathAdv = null;
                    // Aquí puedes abrir la cámara para tomar una nueva fotografía
                    dispatchTakePictureIntentAdv();
                } else {
                    // El permiso de la cámara no ha sido concedido, solicitarlo al usuario
                    ActivityCompat.requestPermissions(DetailedEntradas.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });

        fotografia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar si se ha concedido el permiso de la cámara
                if (ContextCompat.checkSelfPermission(DetailedEntradas.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    // El permiso de la cámara ya ha sido concedido
                    hideKeyboard();
                    // Aquí puedes abrir la cámara para tomar una nueva fotografía
                    dispatchTakePictureIntent();
                } else {
                    // El permiso de la cámara no ha sido concedido, solicitarlo al usuario
                    ActivityCompat.requestPermissions(DetailedEntradas.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });

        checkBoxClave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hideKeyboard();
                    clave.setVisibility(View.VISIBLE);
                    descripcionclave.setVisibility(View.VISIBLE);
                    advertenciaclave.setVisibility(View.VISIBLE);
                    añadaclave.setVisibility(View.VISIBLE);
                    editTextLecturaNueva2.setEnabled(false);  // Deshabilita la edición
                    editTextLecturaNueva2.setText("0");  // Borra el texto si es necesario

                    // Oculta la advertencia y la lógica relacionada con la advertencia
                    editTextAdvertencia.setText("");
                    añadafotografiaadv.setVisibility(View.GONE);
                    advertenciafotoadv.setVisibility(View.GONE);
                    fotografiaadv.setVisibility(View.GONE);
                    fotoTomadaAdv = false;
                    cambios = false;
                } else {
                    hideKeyboard();
                    clave.setText(valorOriginalClave);
                    clave.setVisibility(View.GONE);
                    descripcionclave.setVisibility(View.GONE);
                    descripcionclave.setText(valorOriginaldescripcionclave);
                    advertenciaclave.setVisibility(View.GONE);
                    fotografia.setImageDrawable(valorOrignalfotografia);
                    fotografia.setVisibility(View.GONE);
                    seleccionefotografia.setVisibility(View.GONE);
                    advertenciafotografia.setVisibility(View.GONE);
                    añadaclave.setVisibility(View.GONE);
                    editTextLecturaNueva2.setEnabled(true);  // Deshabilita la edición
                    editTextLecturaNueva2.setText("");  // Borra el texto si es necesario
                }
            }
        });

        binding.clave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la nueva actividad para seleccionar la clave
                Intent intent = new Intent(DetailedEntradas.this, OpcionesClave.class);
                startActivityForResult(intent, 1);
            }
        });

        binding.descripcionclave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abrir la nueva actividad para seleccionar la clave
                Intent intent = new Intent(DetailedEntradas.this, OpcionesClave.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void dispatchTakePictureIntentAdv() {
        // Comprueba si la aplicación tiene permiso para la cámara y almacenamiento
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Si no tienes permiso, solicítalo al usuario
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        // Cambia el código para que abra siempre la cámara, cree la imagen y la mande a createImageFile
        Intent takePictureIntentAdv = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Crea un archivo temporal para guardar la imagen capturada
        File photoFileAdv = createImageFileAdv();

        // Continúa solo si se pudo crear el archivo
        if (photoFileAdv != null) {
            try {
                Uri photoUriAdv = FileProvider.getUriForFile(DetailedEntradas.this, "com.jmaschihuahua.jmaslecturas.fileprovider", photoFileAdv);
                takePictureIntentAdv.putExtra(MediaStore.EXTRA_OUTPUT, photoUriAdv);
                startActivityForResult(takePictureIntentAdv, CAMERA_REQUEST_CODE_ADV);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // Error al obtener el URI del archivo, muestra un diálogo de error
                showDispatchTakePictureErrorDialog("Error al abrir la cámara.");
            }
        }
    }

    private void showDispatchTakePictureErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_error_24);
        builder.setTitle("Error al abrir la cámara");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // Método para la captura de los permisos y selección de los módulos de aceptación o denegación de los permisos

    // Método necesario para el despliegue de la actividad de la cámara, este método permite abrir la cámara del dispositivo.
    private void dispatchTakePictureIntent() {
        // Comprueba si la aplicación tiene permiso para la cámara y almacenamiento
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Si no tienes permiso, solicítalo al usuario
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        // Cambia el código para que abra siempre la cámara, cree la imagen y la mande a createImageFile
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Crea un archivo temporal para guardar la imagen capturada
        File photoFile = createImageFile();

        // Continúa solo si se pudo crear el archivo
        if (photoFile != null) {
            try {
                Uri photoUri = FileProvider.getUriForFile(DetailedEntradas.this, "com.jmaschihuahua.jmaslecturas.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE_PHOTO);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // Error al obtener el URI del archivo, muestra un diálogo de error
                showDispatchTakePictureErrorDialog("Error al abrir la cámara.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SETTINGS_REQUEST) {
            checkLocationSettings();
        }

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String selectedClave = data.getStringExtra("selectedClave");
                String selectedIdentificador = data.getStringExtra("selectedIdentificador");
                // Aquí puedes hacer algo con el valor de la clave seleccionada
                // por ejemplo, mostrarlo en un TextView
                binding.clave.setText(selectedClave);
                binding.descripcionclave.setText(selectedIdentificador);
                binding.advertenciaclave.setVisibility(View.GONE);
                vacioClaveIncidencia = clave.getText().toString();
                claveAñadida = true;

                aceptaLectura = databaseHelper.obtenerAceptaLectura(vacioClaveIncidencia);
                if (aceptaLectura) {
                    if (editTextLecturaNueva2.getText().toString().equals("0")) {
                        editTextLecturaNueva2.setText("");
                    }
                    editTextLecturaNueva2.setEnabled(true);
                    fotografia.setVisibility(View.VISIBLE);
                    seleccionefotografia.setVisibility(View.VISIBLE);
                    if (currentPhotoPath == null || currentPhotoPath.isEmpty()) {
                        advertenciafotografia.setVisibility(View.VISIBLE);
                    }
                } else {
                    editTextLecturaNueva2.setEnabled(false);
                    editTextLecturaNueva2.setText("0");
                    fotografia.setVisibility(View.VISIBLE);
                    seleccionefotografia.setVisibility(View.VISIBLE);
                    advertenciafotografia.setVisibility(View.VISIBLE);
                    editTextAdvertencia.setText("");

                    añadafotografiaadv.setVisibility(View.GONE);
                    advertenciafotoadv.setVisibility(View.GONE);
                    fotografiaadv.setVisibility(View.GONE);

                }
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE_PHOTO) {
                // La foto se tomó en photoImageView
                Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                binding.photoImageView.setImageBitmap(imageBitmap);
                binding.advertenciafotografia.setVisibility(View.GONE);
                binding.advertenciafotografia.setVisibility(View.GONE);
                fotoTomadaClave = true;
            }

            if (requestCode == CAMERA_REQUEST_CODE_ADV) {
                // La foto se tomó en fotografiaadv
                Bitmap advImageBitmap = BitmapFactory.decodeFile(currentPhotoPathAdv);
                fotografiaadv.setImageBitmap(advImageBitmap);
                binding.advertenciafotoadv.setVisibility(View.GONE);
                fotoTomadaAdv = true;
            }
        }
    }

    private void checkLocationSettings() {
        // Verificar si la ubicación está habilitada
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // Mostrar cuadro de diálogo para redirigir al usuario a la configuración de ubicación
            showLocationSettingsDialog();
        } else {
            getLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_opciones_clave, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        // Verificar cambios
        checkDataChanges();
        if (id == R.id.itemCerrar) {
            // Verificar cambios
            checkDataChanges();
            if (dataChanged) {
                // Mostrar el diálogo de opciones (guardar, cancelar, cerrar)
                showOptionsDialog();
            } else {
                finish();
            }
        }
        return true;
    }

    private void checkDataChanges() {
        // Obtén los valores actuales de los campos de texto
        String clave = checkBoxClave.isChecked() ? binding.clave.getText().toString() : "";
        String fotodistinta = checkBoxClave.isChecked() ? "/" + currentPhotoPath : "";
        String nueva = binding.editTextLecturaNueva.getText().toString();
        String advertencia = currentPhotoPathAdv != null && !currentPhotoPathAdv.isEmpty() ? "/" + currentPhotoPathAdv : "";

        if (!clave.equals("") || !fotodistinta.equals("") || !nueva.equals("") || !advertencia.equals("")) {
            dataChanged = true;
        } else {
            dataChanged = false;
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_error_24);
        builder.setTitle("Confirmación");
        builder.setMessage("¿Estás seguro de salir sin guardar los cambios?");

        builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Cambiar el siguiente metodo para que en caso de que exista una advertencia y no se haya tomado la foto no deje continuar para guardar los datos
    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_baseline_upload_24);
        builder.setTitle("Cambios sin guardar");
        builder.setMessage("Hay cambios sin guardar. ¿Deseas guardar los cambios antes de salir?");
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acción al hacer clic en "Guardar"
                // Acción al hacer clic en "Guardar"
                String fotodistinta = checkBoxClave.isChecked() ? "/" + currentPhotoPath : "";
                String nueva = binding.editTextLecturaNueva.getText().toString();
                String advertencia = editTextAdvertencia.getText().toString();
                String fotoadvertencia = currentPhotoPathAdv != null && !currentPhotoPathAdv.isEmpty() ? "/" + currentPhotoPathAdv : "";

                // Verificar si hay advertencia y si no se ha tomado la foto de advertencia
                if (!advertencia.equals("") && fotoadvertencia.equals("")) {
                    // Mostrar mensaje indicando que se debe tomar la foto de advertencia
                    showAdvertenciaSinFotoDialog();
                } else if (!fotodistinta.equals("") && claveAñadida || !nueva.isEmpty()) {
                    saveDataGuardarCambios();
                } else {
                    AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                    innerBuilder.setIcon(R.drawable.baseline_error_24);
                    innerBuilder.setTitle("Advertencia");
                    innerBuilder.setMessage("Por favor, completa todos los campos obligatorios antes de guardar los datos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    innerBuilder.create().show();
                }
            }
        });

        builder.setNegativeButton("Descartar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acción al hacer clic en "Descartar"
                showConfirmationDialog();
            }
        });

        builder.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acción al hacer clic en "Cancelar"
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Cambiar el color del texto
        int color = Color.parseColor("#FF0000"); // Color rojo como ejemplo
        dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        // Alinear las opciones a la derecha
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setGravity(Gravity.END);

        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setGravity(Gravity.END);

        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setGravity(Gravity.END);
    }

    private void showAdvertenciaSinFotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_error_24);
        builder.setTitle("Advertencia");
        builder.setMessage("Debes tomar la foto de advertencia antes de guardar los datos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void saveData() {
        // Obtén los valores actuales de los campos de texto
        String VchNombreSector = binding.editTextSector.getText().toString();
        String VchNombreRuta = binding.editTextRuta.getText().toString();
        String SitNumSecuenciaRuta = binding.editTextSecuencia.getText().toString();
        String NumContrato = binding.editTextContrato.getText().toString();
        String VchDetalleDireccion = binding.editTextDireccion.getText().toString();

        Intent intent = this.getIntent();
        String IntIdPadron = intent.getStringExtra("getPadronSeleccionado");
        String intidPaquete = databaseHelper.getintidPaquete(NumContrato, IntIdPadron);
        String intidSucursal = databaseHelper.getintidSucursal(NumContrato, IntIdPadron);
        String SitConsumoMes = databaseHelper.getSitConsumoMes(NumContrato, IntIdPadron);
        String SitConsumoPromedio = databaseHelper.getSitConsumoPromedio(NumContrato, IntIdPadron);

        String LecturaAnterior = binding.editTextLecturaAnterior.getText().toString();
        String vchNumMedidor = binding.editTextNumerodeMedidor.getText().toString();
        String LecturaActual = binding.editTextLecturaNueva.getText().toString();
        String ClaveLectura = checkBoxClave.isChecked() ? binding.clave.getText().toString() : "";

        String vchNombreLecturista = databaseHelper.getvchNombreLecturista(NumContrato, IntIdPadron);
        String sdtfechaRecepcion = databaseHelper.getFechaRecepcionFromEndpointTable();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String sdtfechaActualizacion = dateFormat.format(calendar.getTime());

        String vchLatitud = this.latitud;
        String vchLongitud = this.longitud;

        String rutaFotografiaClave = checkBoxClave.isChecked() ? "/" + currentPhotoPath : "";
        String advertencia = binding.editTextAdvertencia.getText().toString();

        String rutaFotografiaAdvertencia = currentPhotoPathAdv != null && !currentPhotoPathAdv.isEmpty() ? "/" + currentPhotoPathAdv : "";

        String status = "enviando";
        String statusEnvio = databaseHelper.getStatusEnvioFromEndpointTable(NumContrato, IntIdPadron);

        int COLUMN_ID_ENDPOINTS = databaseHelper.getEndpointIdByNumContrato(NumContrato, IntIdPadron);

        // Verificar si hay advertencia y si no se ha tomado la foto de advertencia
        if (!advertencia.equals("") && rutaFotografiaAdvertencia.equals("")) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Advertencia");
            innerBuilder.setMessage("Debes tomar la foto de advertencia antes de guardar los datos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        if (LecturaActual.trim().isEmpty()) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Advertencia");
            innerBuilder.setMessage("Debes agregar los datos obligatorios. Intentarlo de nuevo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        //Comprobar si existe un registro con el mismo numero de contrato en la base de datos
        if (databaseHelper.isDuplicateContract(NumContrato, IntIdPadron)) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Advertencia");
            innerBuilder.setMessage("No es posible enviar los datos. Ya existen datos idénticos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
        } else {
            if (checkBoxClave.isChecked()) {
                if (fotoTomadaClave && claveAñadida) {
                    // Crear una instancia de LecturaModel con los datos ingresados
                    LecturaModel lectura = new LecturaModel(
                            COLUMN_ID_ENDPOINTS,
                            IntIdPadron,
                            intidPaquete,
                            intidSucursal,
                            VchNombreSector,
                            VchNombreRuta,
                            SitNumSecuenciaRuta,
                            NumContrato,
                            VchDetalleDireccion,
                            SitConsumoMes,
                            SitConsumoPromedio,
                            LecturaAnterior,
                            vchNumMedidor,
                            LecturaActual,
                            ClaveLectura,
                            vchNombreLecturista,
                            sdtfechaRecepcion,
                            sdtfechaActualizacion,
                            vchLatitud,
                            vchLongitud,
                            rutaFotografiaClave,
                            advertencia,
                            status,
                            statusEnvio,
                            rutaFotografiaAdvertencia
                    );

                    // Insertar el registro en la base de datos local
                    long insertedRowId = databaseHelper.insertLectura(lectura);

                    if (insertedRowId != -1) {
                        // Eliminar el registro con el mismo numero de contrato de la tabla endpoint
                        databaseHelper.deleteRegistroEndpoint(NumContrato, IntIdPadron);

                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_check_24);
                        innerBuilder.setTitle("Exito");
                        innerBuilder.setMessage(mensajes).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String numContrato = binding.editTextContrato.getText().toString();
                                sendLecturaToServer(numContrato, IntIdPadron);
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                                enviarDatosYMostrarSiguiente();
                            }
                        });

                        AlertDialog innerDialog = innerBuilder.create();
                        innerDialog.setCanceledOnTouchOutside(false);

                        innerDialog.show();
                    } else {
                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_error_24);
                        innerBuilder.setTitle("Error");
                        innerBuilder.setMessage("Error al guardar los datos. Intentarlo de nuevo más tarde.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        innerBuilder.create().show();
                    }
                } else {
                    AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                    innerBuilder.setIcon(R.drawable.baseline_error_24);
                    innerBuilder.setTitle("Advertencia");

                    innerBuilder.setMessage("Debes agregar los datos obligatorios. Intentarlo de nuevo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hideCustomProgressDialog();
                            dialogInterface.dismiss();
                        }
                    });
                    innerBuilder.create().show();
                }
            } else {
                if (cambios) {
                    if (fotoTomadaAdv) {
                        // Crear una instancia de LecturaModel con los datos ingresados
                        LecturaModel lectura = new LecturaModel(
                                COLUMN_ID_ENDPOINTS,
                                IntIdPadron,
                                intidPaquete,
                                intidSucursal,
                                VchNombreSector,
                                VchNombreRuta,
                                SitNumSecuenciaRuta,
                                NumContrato,
                                VchDetalleDireccion,
                                SitConsumoMes,
                                SitConsumoPromedio,
                                LecturaAnterior,
                                vchNumMedidor,
                                LecturaActual,
                                ClaveLectura,
                                vchNombreLecturista,
                                sdtfechaRecepcion,
                                sdtfechaActualizacion,
                                vchLatitud,
                                vchLongitud,
                                rutaFotografiaClave,
                                advertencia,
                                status,
                                statusEnvio,
                                rutaFotografiaAdvertencia
                        );

                        // Insertar el registro en la base de datos local
                        long insertedRowId = databaseHelper.insertLectura(lectura);

                        if (insertedRowId != -1) {
                            // Eliminar el registro con el mismo numero de contrato de la tabla endpoint
                            databaseHelper.deleteRegistroEndpoint(NumContrato, IntIdPadron);

                            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                            innerBuilder.setIcon(R.drawable.baseline_check_24);
                            innerBuilder.setTitle("Exito");
                            innerBuilder.setMessage(mensajes).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String numContrato = binding.editTextContrato.getText().toString();
                                    sendLecturaToServer(numContrato, IntIdPadron);
                                    databaseHelper.setDatabaseCallback(DetailedEntradas.this);
                                    databaseHelper.setDataUpdateListener(DetailedEntradas.this);
                                    hideCustomProgressDialog();
                                    dialogInterface.dismiss();
                                    enviarDatosYMostrarSiguiente();
                                }
                            });

                            AlertDialog innerDialog = innerBuilder.create();
                            innerDialog.setCanceledOnTouchOutside(false);

                            innerDialog.show();
                        } else {
                            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                            innerBuilder.setIcon(R.drawable.baseline_error_24);
                            innerBuilder.setTitle("Error");
                            innerBuilder.setMessage("Error al guardar los datos. Intentarlo de nuevo más tarde.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            innerBuilder.create().show();
                        }
                    } else {
                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_error_24);
                        innerBuilder.setTitle("Advertencia");
                        innerBuilder.setMessage("Debes agregar los datos obligatorios. Intentarlo de nuevo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                            }
                        });
                        innerBuilder.create().show();
                    }
                } else {
                    // Crear una instancia de LecturaModel con los datos ingresados
                    LecturaModel lectura = new LecturaModel(
                            COLUMN_ID_ENDPOINTS,
                            IntIdPadron,
                            intidPaquete,
                            intidSucursal,
                            VchNombreSector,
                            VchNombreRuta,
                            SitNumSecuenciaRuta,
                            NumContrato,
                            VchDetalleDireccion,
                            SitConsumoMes,
                            SitConsumoPromedio,
                            LecturaAnterior,
                            vchNumMedidor,
                            LecturaActual,
                            ClaveLectura,
                            vchNombreLecturista,
                            sdtfechaRecepcion,
                            sdtfechaActualizacion,
                            vchLatitud,
                            vchLongitud,
                            rutaFotografiaClave,
                            advertencia,
                            status,
                            statusEnvio,
                            rutaFotografiaAdvertencia
                    );

                    // Insertar el registro en la base de datos local
                    long insertedRowId = databaseHelper.insertLectura(lectura);

                    if (insertedRowId != -1) {
//                        // Eliminar el registro con el mismo numero de contrato de la tabla endpoint
                        databaseHelper.deleteRegistroEndpoint(NumContrato, IntIdPadron);

                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_check_24);
                        innerBuilder.setTitle("Exito");
                        innerBuilder.setMessage(mensajes).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String numContrato = binding.editTextContrato.getText().toString();
                                sendLecturaToServer(numContrato, IntIdPadron);
                                databaseHelper.setDatabaseCallback(DetailedEntradas.this);
                                databaseHelper.setDataUpdateListener(DetailedEntradas.this);
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                                enviarDatosYMostrarSiguiente();
                            }
                        });

                        AlertDialog innerDialog = innerBuilder.create();
                        innerDialog.setCanceledOnTouchOutside(false);

                        innerDialog.show();
                    } else {
                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_error_24);
                        innerBuilder.setTitle("Error");
                        innerBuilder.setMessage("Error al guardar los datos. Intentarlo de nuevo más tarde.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                            }
                        });
                        innerBuilder.create().show();
                    }
                }
            }
        }
    }

    private void saveDataGuardarCambios() {
        // Obtén los valores actuales de los campos de texto
        String VchNombreSector = binding.editTextSector.getText().toString();
        String VchNombreRuta = binding.editTextRuta.getText().toString();
        String SitNumSecuenciaRuta = binding.editTextSecuencia.getText().toString();
        String NumContrato = binding.editTextContrato.getText().toString();
        String VchDetalleDireccion = binding.editTextDireccion.getText().toString();

        Intent intent = this.getIntent();
        String IntIdPadron = intent.getStringExtra("getPadronSeleccionado");
        String intidPaquete = databaseHelper.getintidPaquete(NumContrato, IntIdPadron);
        String intidSucursal = databaseHelper.getintidSucursal(NumContrato, IntIdPadron);
        String SitConsumoMes = databaseHelper.getSitConsumoMes(NumContrato, IntIdPadron);
        String SitConsumoPromedio = databaseHelper.getSitConsumoPromedio(NumContrato, IntIdPadron);

        String LecturaAnterior = binding.editTextLecturaAnterior.getText().toString();
        String vchNumMedidor = binding.editTextNumerodeMedidor.getText().toString();
        String LecturaActual = binding.editTextLecturaNueva.getText().toString();
        String ClaveLectura = checkBoxClave.isChecked() ? binding.clave.getText().toString() : "";

        String vchNombreLecturista = databaseHelper.getvchNombreLecturista(NumContrato, IntIdPadron);
        String sdtfechaRecepcion = databaseHelper.getFechaRecepcionFromEndpointTable();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String sdtfechaActualizacion = dateFormat.format(calendar.getTime());

        String vchLatitud = this.latitud;
        String vchLongitud = this.longitud;

        String rutaFotografiaClave = checkBoxClave.isChecked() ? "/" + currentPhotoPath : "";
        String advertencia = binding.editTextAdvertencia.getText().toString();

        String rutaFotografiaAdvertencia = currentPhotoPathAdv != null && !currentPhotoPathAdv.isEmpty() ? "/" + currentPhotoPathAdv : "";

        String status = "enviando";
        String statusEnvio = databaseHelper.getStatusEnvioFromEndpointTable(NumContrato, IntIdPadron);

        int COLUMN_ID_ENDPOINTS = databaseHelper.getEndpointIdByNumContrato(NumContrato, IntIdPadron);

        // Verificar si hay advertencia y si no se ha tomado la foto de advertencia
        if (!advertencia.equals("") && rutaFotografiaAdvertencia.equals("")) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Advertencia");
            innerBuilder.setMessage("Debes tomar la foto de advertencia antes de guardar los datos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        if (LecturaActual.trim().isEmpty()) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Advertencia");
            innerBuilder.setMessage("Debes agregar los datos obligatorios. Intentarlo de nuevo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        //Comprobar si existe un registro con el mismo numero de contrato en la base de datos
        if (databaseHelper.isDuplicateContract(NumContrato, IntIdPadron)) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Advertencia");
            innerBuilder.setMessage("No es posible enviar los datos. Ya existen datos idénticos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
        } else {
            if (checkBoxClave.isChecked()) {
                if (fotoTomadaClave && claveAñadida) {
                    // Crear una instancia de LecturaModel con los datos ingresados
                    LecturaModel lectura = new LecturaModel(
                            COLUMN_ID_ENDPOINTS,
                            IntIdPadron,
                            intidPaquete,
                            intidSucursal,
                            VchNombreSector,
                            VchNombreRuta,
                            SitNumSecuenciaRuta,
                            NumContrato,
                            VchDetalleDireccion,
                            SitConsumoMes,
                            SitConsumoPromedio,
                            LecturaAnterior,
                            vchNumMedidor,
                            LecturaActual,
                            ClaveLectura,
                            vchNombreLecturista,
                            sdtfechaRecepcion,
                            sdtfechaActualizacion,
                            vchLatitud,
                            vchLongitud,
                            rutaFotografiaClave,
                            advertencia,
                            status,
                            statusEnvio,
                            rutaFotografiaAdvertencia
                    );

                    // Insertar el registro en la base de datos local
                    long insertedRowId = databaseHelper.insertLectura(lectura);

                    if (insertedRowId != -1) {
                        // Eliminar el registro con el mismo numero de contrato de la tabla endpoint
                        databaseHelper.deleteRegistroEndpoint(NumContrato, IntIdPadron);

                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_check_24);
                        innerBuilder.setTitle("Exito");
                        innerBuilder.setMessage(mensajes).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String numContrato = binding.editTextContrato.getText().toString();
                                Log.d(TAG, "onClick: statusEnvio: " + statusEnvio);
                                sendLecturaToServer(numContrato, IntIdPadron);
                                databaseHelper.setDatabaseCallback(DetailedEntradas.this);
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                                finish();
                            }
                        });

                        AlertDialog innerDialog = innerBuilder.create();
                        innerDialog.setCanceledOnTouchOutside(false);

                        innerDialog.show();
                    } else {
                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_error_24);
                        innerBuilder.setTitle("Error");
                        innerBuilder.setMessage("Error al guardar los datos. Intentarlo de nuevo más tarde.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        innerBuilder.create().show();
                    }
                } else {
                    AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                    innerBuilder.setIcon(R.drawable.baseline_error_24);
                    innerBuilder.setTitle("Advertencia");
                    innerBuilder.setMessage("Debes agregar los datos obligatorios. Intentarlo de nuevo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hideCustomProgressDialog();
                            dialogInterface.dismiss();
                        }
                    });
                    innerBuilder.create().show();
                }
            } else {
                if (cambios) {
                    if (fotoTomadaAdv) {
                        // Crear una instancia de LecturaModel con los datos ingresados
                        LecturaModel lectura = new LecturaModel(
                                COLUMN_ID_ENDPOINTS,
                                IntIdPadron,
                                intidPaquete,
                                intidSucursal,
                                VchNombreSector,
                                VchNombreRuta,
                                SitNumSecuenciaRuta,
                                NumContrato,
                                VchDetalleDireccion,
                                SitConsumoMes,
                                SitConsumoPromedio,
                                LecturaAnterior,
                                vchNumMedidor,
                                LecturaActual,
                                ClaveLectura,
                                vchNombreLecturista,
                                sdtfechaRecepcion,
                                sdtfechaActualizacion,
                                vchLatitud,
                                vchLongitud,
                                rutaFotografiaClave,
                                advertencia,
                                status,
                                statusEnvio,
                                rutaFotografiaAdvertencia
                        );

                        // Insertar el registro en la base de datos local
                        long insertedRowId = databaseHelper.insertLectura(lectura);

                        if (insertedRowId != -1) {
                            // Eliminar el registro con el mismo numero de contrato de la tabla endpoint
                            databaseHelper.deleteRegistroEndpoint(NumContrato, IntIdPadron);


                            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                            innerBuilder.setIcon(R.drawable.baseline_check_24);
                            innerBuilder.setTitle("Exito");
                            innerBuilder.setMessage(mensajes).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String numContrato = binding.editTextContrato.getText().toString();
                                    sendLecturaToServer(numContrato, IntIdPadron);
                                    databaseHelper.setDatabaseCallback(DetailedEntradas.this);
                                    hideCustomProgressDialog();
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            });

                            AlertDialog innerDialog = innerBuilder.create();
                            innerDialog.setCanceledOnTouchOutside(false);

                            innerDialog.show();
                        } else {
                            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                            innerBuilder.setIcon(R.drawable.baseline_error_24);
                            innerBuilder.setTitle("Error");
                            innerBuilder.setMessage("Error al guardar los datos. Intentarlo de nuevo más tarde.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            innerBuilder.create().show();
                        }
                    } else {
                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_error_24);
                        innerBuilder.setTitle("Advertencia");
                        innerBuilder.setMessage("Debes agregar los datos obligatorios. Intentarlo de nuevo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                            }
                        });
                        innerBuilder.create().show();
                    }
                } else {
                    // Crear una instancia de LecturaModel con los datos ingresados
                    LecturaModel lectura = new LecturaModel(
                            COLUMN_ID_ENDPOINTS,
                            IntIdPadron,
                            intidPaquete,
                            intidSucursal,
                            VchNombreSector,
                            VchNombreRuta,
                            SitNumSecuenciaRuta,
                            NumContrato,
                            VchDetalleDireccion,
                            SitConsumoMes,
                            SitConsumoPromedio,
                            LecturaAnterior,
                            vchNumMedidor,
                            LecturaActual,
                            ClaveLectura,
                            vchNombreLecturista,
                            sdtfechaRecepcion,
                            sdtfechaActualizacion,
                            vchLatitud,
                            vchLongitud,
                            rutaFotografiaClave,
                            advertencia,
                            status,
                            statusEnvio,
                            rutaFotografiaAdvertencia
                    );

                    // Insertar el registro en la base de datos local
                    long insertedRowId = databaseHelper.insertLectura(lectura);

                    if (insertedRowId != -1) {
//                        // Eliminar el registro con el mismo numero de contrato de la tabla endpoint
                        databaseHelper.deleteRegistroEndpoint(NumContrato, IntIdPadron);

                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_check_24);
                        innerBuilder.setTitle("Exito");
                        innerBuilder.setMessage(mensajes).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String numContrato = binding.editTextContrato.getText().toString();
                                sendLecturaToServer(numContrato, IntIdPadron);
                                databaseHelper.setDatabaseCallback(DetailedEntradas.this);
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                                finish();
                            }
                        });

                        AlertDialog innerDialog = innerBuilder.create();
                        innerDialog.setCanceledOnTouchOutside(false);

                        innerDialog.show();
                    } else {
                        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(DetailedEntradas.this);
                        innerBuilder.setIcon(R.drawable.baseline_error_24);
                        innerBuilder.setTitle("Error");
                        innerBuilder.setMessage("Error al guardar los datos. Intentarlo de nuevo más tarde.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                hideCustomProgressDialog();
                                dialogInterface.dismiss();
                            }
                        });
                        innerBuilder.create().show();
                    }
                }
            }
        }
    }

    private File createImageFile() {
        try {
            // Crea un nombre de archivo único basado en la fecha y hora actual
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";

            // Obtiene el directorio de almacenamiento externo
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null) {
                // Crea el archivo temporal
                File imageFile = File.createTempFile(
                        imageFileName,  // Nombre del archivo
                        ".jpg",        // Extensión del archivo
                        storageDir     // Directorio de almacenamiento
                );

                // Guarda la ruta del archivo para usarla posteriormente
                currentPhotoPath = imageFile.getAbsolutePath();

                return imageFile;
            } else {
                // Error: No se pudo obtener el directorio de almacenamiento externo
                showCreateImageFileErrorDialog("No se pudo acceder al almacenamiento externo.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Error al crear el archivo
            showCreateImageFileErrorDialog("Error al crear el archivo de imagen.");
            return null;
        }
    }

    private File createImageFileAdv() {
        try {
            Log.d(TAG, "createImageFileAdv: " + currentPhotoPathAdv);
            // Crea un nombre de archivo único basado en la fecha y hora actual
            String timeStampAdv = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileNameAdv = "JPEG_" + timeStampAdv + "_";

            // Obtiene el directorio de almacenamiento externo
            File storageDirAdv = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDirAdv != null) {
                // Crea el archivo temporal
                File imageFileAdv = File.createTempFile(
                        imageFileNameAdv,  // Nombre del archivo
                        ".jpg",            // Extensión del archivo
                        storageDirAdv      // Directorio de almacenamiento
                );

                // Verifica si el archivo se creó correctamente
                if (imageFileAdv != null) {
                    // Guarda la ruta del archivo solo si se creó correctamente
                    currentPhotoPathAdv = imageFileAdv.getAbsolutePath();
                    return imageFileAdv;
                } else {
                    // Error: No se pudo crear el archivo
                    showCreateImageFileErrorDialog("Error al crear el archivo de imagen.");
                    return null;
                }
            } else {
                // Error: No se pudo obtener el directorio de almacenamiento externo
                showCreateImageFileErrorDialog("No se pudo acceder al almacenamiento externo.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Error al crear el archivo
            showCreateImageFileErrorDialog("Error al crear el archivo de imagen.");
            return null;
        }
    }


    private void showCreateImageFileErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_error_24);
        builder.setTitle("Error al crear el archivo de imagen. Intentarlo de nuevo más tarde.");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void showSaveImageErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_error_24);
        builder.setTitle("Error al guardar la imagen");
        builder.setMessage("No se pudo guardar la imagen en la galería. Intentarlo más tarde.");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        // Verificar cambios
        checkDataChanges();
        if (dataChanged) {
            showOptionsDialog();
        } else {
            super.onBackPressed();
        }
    }

    protected void onStart() {
        super.onStart();
        registerConnectivityCallback();
    }

    protected void onStop() {
        super.onStop();
        unregisterConnectivityCallback();

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

    //Registrar ConnectivityManager.NetworkCallback para recibir cambios de conectividad
    private void registerConnectivityCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                // La conectividad a internet esta disponible
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
            }
        };
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    // Desregistrar ConnectivityManager.NetworkCallback para recibir cambios de conectividad
    private void unregisterConnectivityCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }


    private void getLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        latitud = String.valueOf(latitude);
                        longitud = String.valueOf(longitude);

                    } else {
                        Toast.makeText(DetailedEntradas.this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                        showLocationSettingsDialog();
                    }
                })
                .addOnFailureListener(this, e -> {
                    // Manejar cualquier error que ocurra al obtener la ubicación
                    Toast.makeText(DetailedEntradas.this, "Obtención de localización via GPS", Toast.LENGTH_SHORT).show();
                    String errorMessage = e.getMessage();
                    obtainLocationViaGPS();
                });
    }

    private void obtainLocationViaGPS() {
        // Comprobar si se concedió el permiso de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Verificar si el GPS está habilitado
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Asignar los valores de latitud y longitud a las variables de clase
                        latitud = String.valueOf(latitude);
                        longitud = String.valueOf(longitude);


                        // A partir de aquí, puedes realizar cualquier acción que requiera la ubicación actual,
                        // como almacenarla en la base de datos.
                        locationManager.removeUpdates(this); // Detener actualizaciones de ubicación
                    }

                    public void onProviderEnabled(String provider) {
                        caseonProviderEnabled();
                    }
                });
            } else {
                showLocationSettingsDialog();
                Toast.makeText(this, "El GPS no está habilitado", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void caseonProviderEnabled() {
        latitud = "-1";
        longitud = "-1";
        obtainLocationViaGPS();
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_error_24)
        ;
        builder.setTitle("Ubicación desactivada");
        builder.setMessage("Por favor, active la ubicación para permitir que la aplicación funcione correctamente.");
        builder.setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Redirigir al usuario a la configuración de ubicación
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, LOCATION_SETTINGS_REQUEST);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }


    public void sendLecturaToServer(String numContrato, String padron) {
        LecturaModel lectura = databaseHelper.getLecturaByNumContrato(numContrato, padron);
        if (lectura != null) {
            String statusEnvio = lectura.getStatusEnvio();
            Log.d(TAG, "sendLecturaToServer: este: " + statusEnvio);
            if (isInternetConnected()) {
//                // Verificar si es una operación de grupo o individual
//                if ("grupo".equals(statusEnvio)) {
//                    Log.d(TAG, "entro a la opción de grupo");
//                    int intIdPaquete = Integer.parseInt(lectura.getIntidPaquete());
//                    if (!databaseHelper.hasOtherGroupRecordsWithSameIntIdPaquete(intIdPaquete)) {
//                        Log.d(TAG, "sendLecturaToServer: Se envió porque ya no existen otros registros con el mismo intidpaquete y status_envio 'grupo'");
//                        sendGroupDataToServer(intIdPaquete);
//                    } else {
//                        Log.d(TAG, "sendLecturaToServer: No se envió la lectura debido a otros registros con el mismo intidpaquete y status_envio 'grupo'");
//                    }
//                } else if ("individual".equals(statusEnvio)) {
//                    // Crear una lista con un solo objeto LecturaModel y enviarlo
                ArrayList<LecturaModel> lecturaList = new ArrayList<>();
                lecturaList.add(lectura);
                databaseHelper.sendDataToServer(lecturaList);
                databaseHelper.setDataUpdateListener(DetailedEntradas.this);
                databaseHelper.setDatabaseCallback(DetailedEntradas.this);


//                } else {
//                    Toast.makeText(DetailedEntradas.this, "No se pudo obtener la lectura de la base de datos.", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(DetailedEntradas.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendGroupDataToServer(int intIdPaquete) {
        ArrayList<LecturaModel> datosPendientes = databaseHelper.getPendingDataFromEndpoint1(intIdPaquete);
        // Enviar datos pendientes al servidor
        for (LecturaModel lectura : datosPendientes) {
            // Crear una lista con un solo objeto LecturaModel y enviarlo
            ArrayList<LecturaModel> lecturaList = new ArrayList<>();
            lecturaList.add(lectura);
            databaseHelper.sendDataToServer(lecturaList);
            databaseHelper.setDatabaseCallback(DetailedEntradas.this);
            lectura.getIntIdPadron();
        }
        // Cierra la instancia de DatabaseHelper
        databaseHelper.close();
    }

    private void showCustomProgressDialog() {
        // Crear un AlertDialog personalizado con un diseño personalizado
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(DetailedEntradas.this);
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View view = getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDataUpdated(ArrayList<ListDataEntradas> updatedData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: funciona");;
            }
        });
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
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(DetailedEntradas.this);
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

    public void launchDetailedEntradasActivity(int index) {
        // Crear una instancia del DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        List<ListDataEntradas> lecturasEndpointList = databaseHelper.getAllLecturasEndpoint();

        if (index >= 0 && index < lecturasEndpointList.size()) {
            currentIndex = index;  // Usar el índice seleccionado en lugar del índice actual

            ListDataEntradas lectura = lecturasEndpointList.get(currentIndex);

            //LecturaEndpoint lecturaEndpoint = databaseHelper.getEndpointByNumContrato(lectura.getContrato(), lectura.getPadron());

            // Crear un Intent para iniciar la actividad DetailedEntradas
            Intent intent = new Intent(this, DetailedEntradas.class);

            // Agregar datos a través de putExtra
//            intent.putExtra("sector", lecturaEndpoint.getVchNombreSector());
//            intent.putExtra("ruta", lecturaEndpoint.getVchNombreRuta());
//            intent.putExtra("secuencia", lecturaEndpoint.getSitNumSecuenciaRuta());
//            intent.putExtra("contrato", lecturaEndpoint.getNumContrato());
//            intent.putExtra("domicilio", lecturaEndpoint.getVchDetalleDireccion());
//            intent.putExtra("numerodemedidor", lecturaEndpoint.getVchNumMedidor());
//            intent.putExtra("anterior", lecturaEndpoint.getLecturaAnterior());
//            intent.putExtra("padron", lecturaEndpoint.getIntIdPadron());
//            intent.putExtra("fechaActualizacion", lecturaEndpoint.getSdtfechaActualizacion());


            // Obtener el valor de "padron" basándote en el ítem seleccionado
            //padronSeleccionado = obtenerPadronSegunItemSeleccionado(lectura);
            getPadronSeleccionado();

            intent.putExtra("getPadronSeleccionado", getPadronSeleccionado());
            //intent.putExtra("advertencia", lecturaEndpoint.getAdvertencia());

            selectedPosition = currentIndex;
            intent.putExtra("selectedPosition", selectedPosition);
            // Iniciar la actividad con el Intent
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    // Método para enviar datos y pasar al siguiente elemento
    private void enviarDatosYMostrarSiguiente() {
        currentIndex = selectedPosition;
        launchDetailedEntradasActivity(currentIndex);
    }

//    public String obtenerPadronSegunItemSeleccionado(ListDataEntradas lectura) {
//        // Lógica para obtener el valor de "padron"
//        //return lectura.getPadron();
//    }

    // Método público para obtener el valor de "padron" desde otras clases
    public String getPadronSeleccionado() {
        return padronSeleccionado;
    }


    private boolean isVerificationDone = false; // Bandera para controlar si la verificación ya se ha realizado

    public void initTelephonyManager() {
        Log.d(ContentValues.TAG, "initTelephonyManager: enviados uno");
        if (!isVerificationDone) {
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                listenSignalStrength();
            }
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
                isVerificationDone = true;
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
        Log.d(TAG, "handleNetworkQuality: siete");
        if (signalStrengthValue <= SIGNAL_STRENGTH_THRESHOLD) {
            Log.d(TAG, "handleNetworkQuality: ocho uno");
            isSignalWeak = true; // Señal débil detectada
            mensajes = "Los datos han sido guardados con éxito. Señal de red móvil débil, por favor, consideré cambiar a una red estable.";
        } else {
            Log.d(TAG, "handleNetworkQuality: ocho dos");
            isSignalWeak = false; // Señal normal
            mensajes = "Los datos han sido guardados con exito. Se procederá a enviarlos.";
        }
    }

    private BroadcastReceiver signalStrengthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.SIGNAL_STRENGTH_CHANGED".equals(intent.getAction())) {
                boolean isSignalWeak = intent.getBooleanExtra("isSignalWeak", false);
                isSignalWeakStatic = isSignalWeak;
                if (isSignalWeak){
                    mensajes = "Los datos han sido guardados con éxito. Señal de red móvil débil, por favor, consideré cambiar a una red estable.";
                } else {
                    mensajes = "Los datos se han guardado con exito. Se procederá a enviarlos.";
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        // Deregistrar el receptor de broadcast
        unregisterReceiver(signalStrengthReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Registrar el receptor de broadcast
        IntentFilter filter = new IntentFilter("com.example.SIGNAL_STRENGTH_CHANGED");
        registerReceiver(signalStrengthReceiver, filter);

//        initTelephonyManager();
    }
}