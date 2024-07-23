package com.l3mdev.AppCruzada;

import static android.widget.Toast.LENGTH_SHORT;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.l3mdev.AppCruzada.DB.LecturaBeneficiarios;
import com.l3mdev.AppCruzada.DB.LecturaModel;
import com.l3mdev.AppCruzada.databinding.ActivityDetailedBinding;
import com.l3mdev.AppCruzada.preference.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Detailed extends AppCompatActivity implements DatabaseHelper.DatabaseCallback , DatabaseHelper.DataUpdateListener{
    private static final int SIGNATURE_REQUEST_CODE = 1004;

    private DatabaseHelper databaseHelper;

    private androidx.appcompat.app.AlertDialog customProgressDialog; // Cambiado de ProgressDialog a AlertDialog

    // Variables para el despliegue de los permisos de la cámara, tanto para el almacenamiento como para abrir la cámara.
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_SETTINGS_REQUEST = 1003;
    private static final int CAMERA_REQUEST_CODE_PHOTO = 3;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private String currentPhotoPathReverso, currentPhotoPathFrente,
            currentPhotoPathDomicilio, currentPhotoPathTrabajo, currentPhotoPathFachadaAntes, currentPhotoPathDespues, currentPhotoPathConfirmidad;

    private String currentSignaturePath;

    EditText editTextCurp, editTextRfc, editTextNombre, editTextPaterno, editTextMaterno,
            editTextCalle, editTextNumExt, editTextNumInt, editTextColonia, editTextEdad,
            editTextTelefono, editTextMetros, editTextColor;

    ImageView advFotoReverso, advFotoFrente, advFotoDomicilio, advFotoTrabajo, advFotoFachadaAntes, advFotoFachadaDespues, advFotoConfirmidad,
            imageViewToUpdate,  advFotoFirma;
    Spinner spinnerSexo;
    String sexo = "";
    String dudeTip;
    Button enviarButton;

    private boolean dataChanged = false;

    private FusedLocationProviderClient fusedLocationClient;

    String latitud;
    String longitud;
    public String mensajes;

    ActivityDetailedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        binding = ActivityDetailedBinding.inflate(getLayoutInflater());

        databaseHelper = new DatabaseHelper(this);

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


        editTextCurp = findViewById(R.id.editTextCurp);
        editTextRfc = findViewById(R.id.editTextRfc);
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextPaterno = findViewById(R.id.editTextPaterno);
        editTextMaterno = findViewById(R.id.editTextMaterno);
        editTextCalle = findViewById(R.id.editTextCalle);
        editTextNumExt = findViewById(R.id.editTextNumExt);
        editTextNumInt = findViewById(R.id.editTextNumInt);
        editTextColonia = findViewById(R.id.editTextColonia);
        editTextEdad = findViewById(R.id.editTextEdad);
        editTextTelefono = findViewById(R.id.editTextTelefono);
        editTextMetros = findViewById(R.id.editTextMetros);
        float minRange = 0.0f;  // Cambia estos valores según sea necesario
        float maxRange = 100.0f;
        editTextMetros.setFilters(new InputFilter[] { new RangeInputFilter(minRange, maxRange) });

        editTextColor = findViewById(R.id.editTextColor);
        spinnerSexo = findViewById(R.id.spinnerSexo);

        setContentView(binding.getRoot());
        Intent intent = this.getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarDetailedEntradas);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar cambios
                checkDataChanges();
                if (dataChanged) {
                    showOptionsDialog();
                } else {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        setupSpinnerBasic();

        enviarButton = findViewById(R.id.enviarBeneficiarioButton);
        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                showCustomProgressDialog();
                getLocation();
                saveData();
            }
        });

        advFotoReverso = findViewById(R.id.advFotoIdentificacionReverso);
        advFotoReverso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Detailed.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    hideKeyboard();

                    dudeTip = "1";
                    dispatchTakePictureIntent();
                } else {
                    ActivityCompat.requestPermissions(Detailed.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });

        advFotoFrente = findViewById(R.id.advFotoIdentificacionFrente);
        advFotoFrente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Detailed.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    hideKeyboard();

                    dudeTip = "2";
                    dispatchTakePictureIntent();
                } else {
                    ActivityCompat.requestPermissions(Detailed.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });

        advFotoDomicilio = findViewById(R.id.advFotoComprobanteDomicilio);
        advFotoDomicilio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Detailed.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    hideKeyboard();

                    dudeTip = "3";
                    dispatchTakePictureIntent();
                } else {
                    ActivityCompat.requestPermissions(Detailed.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });

        advFotoTrabajo = findViewById(R.id.advFotoAnuenciaTrabajo);
        advFotoTrabajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Detailed.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    hideKeyboard();
                    currentPhotoPathTrabajo = null;
                    dudeTip = "4";
                    //dispatchTakePictureIntent();
                    startSignatureActivity();
                } else {
                    ActivityCompat.requestPermissions(Detailed.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });
        advFotoFachadaAntes = findViewById(R.id.advFotoFachadaAntes);
        advFotoFachadaAntes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Detailed.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    hideKeyboard();

                    dudeTip = "5";
                    dispatchTakePictureIntent();
                } else {
                    ActivityCompat.requestPermissions(Detailed.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });

    }

    private void startSignatureActivity() {
        Intent intent = new Intent(Detailed.this, SignatureActivity.class);
        startActivityForResult(intent, SIGNATURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SETTINGS_REQUEST) {
            checkLocationSettings();
        }

        if (requestCode == SIGNATURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                SignatureActivity signatureActivity = new SignatureActivity();
                if (signatureActivity != null) {
                    currentPhotoPathTrabajo = data.getStringExtra("signaturePath");
                    if (currentPhotoPathTrabajo != null) {
                        Bitmap imageBitmapAnuencia = BitmapFactory.decodeFile(currentPhotoPathTrabajo);
                        binding.advFotoUpdate4.setImageResource(R.drawable.baseline_check_24);

                        binding.advFotoUpdate4.setImageBitmap(imageBitmapAnuencia);
                        int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_width);
                        int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_height);
                        ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate4.getLayoutParams();
                        layoutParams.width = widthInPixels;
                        layoutParams.height = heightInPixels;
                        binding.advFotoUpdate4.setLayoutParams(layoutParams);

                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show();

                currentPhotoPathTrabajo = "";
                Bitmap imageBitmapTrabajo = BitmapFactory.decodeFile(currentPhotoPathTrabajo);
                advFotoTrabajo.setImageResource(R.drawable.baseline_border_color_24);

                binding.advFotoUpdate4.setImageBitmap(imageBitmapTrabajo);
                int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate4.getLayoutParams();
                layoutParams.width = widthInPixels;
                layoutParams.height = heightInPixels;
            } else {
                Toast.makeText(this, "Error al capturar la imagen", Toast.LENGTH_SHORT).show();
                currentPhotoPathTrabajo = "";
                Bitmap imageBitmapTrabajo = BitmapFactory.decodeFile(currentPhotoPathTrabajo);
                advFotoTrabajo.setImageResource(R.drawable.baseline_camera_alt_24);

                binding.advFotoUpdate4.setImageBitmap(imageBitmapTrabajo);
                int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate4.getLayoutParams();
                layoutParams.width = widthInPixels;
                layoutParams.height = heightInPixels;
                binding.advFotoUpdate4.setLayoutParams(layoutParams);

            }
        }

        if (requestCode == CAMERA_REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (dudeTip.equals("1")) {
                    Bitmap imageBitmapReverso = BitmapFactory.decodeFile(currentPhotoPathReverso);
                    advFotoReverso.setImageResource(R.drawable.baseline_check_24);

                    binding.advFotoUpdate1.setImageBitmap(imageBitmapReverso);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_width);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_height);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate1.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate1.setLayoutParams(layoutParams);


                } else if (dudeTip.equals("2")) {
                    Bitmap imageBitmapFrente = BitmapFactory.decodeFile(currentPhotoPathFrente);
                    advFotoFrente.setImageResource(R.drawable.baseline_check_24);

                    binding.advFotoUpdate2.setImageBitmap(imageBitmapFrente);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_width);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_height);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate2.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate2.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("3")) {
                    Bitmap imageBitmapDomicilio = BitmapFactory.decodeFile(currentPhotoPathDomicilio);
                    advFotoDomicilio.setImageResource(R.drawable.baseline_check_24);

                    binding.advFotoUpdate3.setImageBitmap(imageBitmapDomicilio);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_width);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_height);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate3.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate3.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("4")) {
//                    Bitmap imageBitmapTrabajo = BitmapFactory.decodeFile(currentPhotoPathTrabajo);
//                    advFotoTrabajo.setImageResource(R.drawable.baseline_check_24);
//
//
//                    binding.advFotoUpdate4.setImageBitmap(imageBitmapTrabajo);
//                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_width);
//                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_height);
//                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate4.getLayoutParams();
//                    layoutParams.width = widthInPixels;
//                    layoutParams.height = heightInPixels;
//                    binding.advFotoUpdate4.setLayoutParams(layoutParams);
                } else if (dudeTip.equals("5")) {
                    Bitmap imageBitmapFachadaAntes = BitmapFactory.decodeFile(currentPhotoPathFachadaAntes);
                    advFotoFachadaAntes.setImageResource(R.drawable.baseline_check_24);

                    binding.advFotoUpdate5.setImageBitmap(imageBitmapFachadaAntes);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_width);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_height);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate5.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate5.setLayoutParams(layoutParams);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Captura de imagen cancelada", Toast.LENGTH_SHORT).show();
                if (dudeTip.equals("1")) {
                    currentPhotoPathReverso = "";
                    Bitmap imageBitmapReverso = BitmapFactory.decodeFile(currentPhotoPathReverso);
                    advFotoReverso.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate1.setImageBitmap(imageBitmapReverso);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate1.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate1.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("2")) {
                    currentPhotoPathFrente = "";
                    Bitmap imageBitmapFrente = BitmapFactory.decodeFile(currentPhotoPathFrente);
                    advFotoFrente.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate2.setImageBitmap(imageBitmapFrente);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate2.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate2.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("3")) {
                    currentPhotoPathDomicilio = "";
                    Bitmap imageBitmapDomicilio = BitmapFactory.decodeFile(currentPhotoPathDomicilio);
                    advFotoDomicilio.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate3.setImageBitmap(imageBitmapDomicilio);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate3.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate3.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("4")) {
                    currentPhotoPathTrabajo = "";
                    Bitmap imageBitmapTrabajo = BitmapFactory.decodeFile(currentPhotoPathTrabajo);
                    advFotoTrabajo.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate4.setImageBitmap(imageBitmapTrabajo);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate4.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate4.setLayoutParams(layoutParams);
                } else if (dudeTip.equals("5")) {
                    currentPhotoPathFachadaAntes = "";
                    Bitmap imageBitmapFachadaAntes = BitmapFactory.decodeFile(currentPhotoPathFachadaAntes);
                    advFotoFachadaAntes.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate5.setImageBitmap(imageBitmapFachadaAntes);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate5.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate5.setLayoutParams(layoutParams);
                }
            } else {
                Toast.makeText(this, "Error al capturar la imagen", Toast.LENGTH_SHORT).show();
                if (dudeTip.equals("1")) {
                    currentPhotoPathReverso = "";
                    Bitmap imageBitmapReverso = BitmapFactory.decodeFile(currentPhotoPathReverso);
                    advFotoReverso.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate1.setImageBitmap(imageBitmapReverso);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate1.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate1.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("2")) {
                    currentPhotoPathFrente = "";
                    Bitmap imageBitmapFrente = BitmapFactory.decodeFile(currentPhotoPathFrente);
                    advFotoFrente.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate2.setImageBitmap(imageBitmapFrente);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate2.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate2.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("3")) {
                    currentPhotoPathDomicilio = "";
                    Bitmap imageBitmapDomicilio = BitmapFactory.decodeFile(currentPhotoPathDomicilio);
                    advFotoDomicilio.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate3.setImageBitmap(imageBitmapDomicilio);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate3.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate3.setLayoutParams(layoutParams);

                } else if (dudeTip.equals("4")) {
//                    currentPhotoPathTrabajo = "";
//                    Bitmap imageBitmapTrabajo = BitmapFactory.decodeFile(currentPhotoPathTrabajo);
//                    advFotoTrabajo.setImageResource(R.drawable.baseline_camera_alt_24);
//
//                    binding.advFotoUpdate4.setImageBitmap(imageBitmapTrabajo);
//                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
//                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
//                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate4.getLayoutParams();
//                    layoutParams.width = widthInPixels;
//                    layoutParams.height = heightInPixels;
//                    binding.advFotoUpdate4.setLayoutParams(layoutParams);
                } else if (dudeTip.equals("5")) {
                    currentPhotoPathFachadaAntes = "";
                    Bitmap imageBitmapFachadaAntes = BitmapFactory.decodeFile(currentPhotoPathFachadaAntes);
                    advFotoFachadaAntes.setImageResource(R.drawable.baseline_camera_alt_24);

                    binding.advFotoUpdate5.setImageBitmap(imageBitmapFachadaAntes);
                    int widthInPixels = getResources().getDimensionPixelSize(R.dimen.image_widthPRE);
                    int heightInPixels = getResources().getDimensionPixelSize(R.dimen.image_heightPRE);
                    ViewGroup.LayoutParams layoutParams = binding.advFotoUpdate5.getLayoutParams();
                    layoutParams.width = widthInPixels;
                    layoutParams.height = heightInPixels;
                    binding.advFotoUpdate5.setLayoutParams(layoutParams);
                }
            }
        }
    }

    private void setupSpinnerBasic() {
        Spinner spinner = findViewById(R.id.spinnerSexo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String s = adapterView.getItemAtPosition(position).toString();
                if (s.equals("Femenino (F)")) {
                    sexo = "F";
                } else if (s.equals("Masculino (M)")) {
                    sexo = "M";
                } else if (s.equals("No Seleccionado")) {
                    sexo = "N";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    // FOTOGRAFIAS
    private void dispatchTakePictureIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();

        if (photoFile != null) {
            try {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.l3mdev.AppCruzada.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE_PHOTO);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                showDispatchTakePictureErrorDialog("Error al abrir la cámara.");
            }
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null) {
                File imageFile = File.createTempFile(
                        imageFileName,  // Nombre del archivo
                        ".jpg",        // Extensión del archivo
                        storageDir     // Directorio de almacenamiento
                );

                if (dudeTip.equals("1")) {
                    currentPhotoPathReverso = imageFile.getAbsolutePath();
                } else if (dudeTip.equals("2")) {
                    currentPhotoPathFrente = imageFile.getAbsolutePath();
                } else if (dudeTip.equals("3")) {
                    currentPhotoPathDomicilio = imageFile.getAbsolutePath();
                } else if (dudeTip.equals("4")) {
                    currentPhotoPathTrabajo = imageFile.getAbsolutePath();
                } else if (dudeTip.equals("5")) {
                    currentPhotoPathFachadaAntes = imageFile.getAbsolutePath();
                }

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

    private void saveData() {
        String vchCURP = binding.editTextCurp.getText().toString();

        String vchRFC = binding.editTextRfc.getText().toString();
        String vchNombre = binding.editTextNombre.getText().toString();
        String vchPaterno = binding.editTextPaterno.getText().toString();

        String vchMaterno = binding.editTextMaterno.getText().toString();
        String vchCalle = binding.editTextCalle.getText().toString();
        String vchNumExt = binding.editTextNumExt.getText().toString();
        String vchNumInt = binding.editTextNumInt.getText().toString();
        String vchColonia = binding.editTextColonia.getText().toString();

        String intEdad = binding.editTextEdad.getText().toString();
        String vchSexo = sexo;

        String vchTelefono = binding.editTextTelefono.getText().toString();
        String fltMetrosCasa = binding.editTextMetros.getText().toString();

        String vchColor = binding.editTextColor.getText().toString();
        String vchLatitud = this.latitud;
        String vchLongitud = this.longitud;

        String vchFotoIdentificacionFrente = currentPhotoPathFrente != null && !currentPhotoPathFrente.isEmpty() ? "/" + currentPhotoPathFrente : "";
        String vchFotoIdentificacionReverso = currentPhotoPathReverso != null && !currentPhotoPathReverso.isEmpty() ? "/" + currentPhotoPathReverso : "";

        String vchFotoComprobante = currentPhotoPathDomicilio != null && !currentPhotoPathDomicilio.isEmpty() ? "/" + currentPhotoPathDomicilio : "";
        String vchFotoAnuenciaTrabajo = currentPhotoPathTrabajo != null && !currentPhotoPathTrabajo.isEmpty() ? "/" + currentPhotoPathTrabajo : "";
        String vchFotoFachadaAntes = currentPhotoPathFachadaAntes != null && !currentPhotoPathFachadaAntes.isEmpty() ? "/" + currentPhotoPathFachadaAntes : "";
        String vchFotoFachadaDespues = currentPhotoPathDespues != null && !currentPhotoPathDespues.isEmpty() ? "/" + currentPhotoPathDespues : "";
        String vchFotoConfirmidad = currentPhotoPathConfirmidad != null && !currentPhotoPathConfirmidad.isEmpty() ? "/" + currentPhotoPathConfirmidad : "";

        String vchStatus = "enviando";

        LecturaBeneficiarios lectura = new LecturaBeneficiarios(
                0,
                vchCURP,
                vchRFC,
                vchNombre,
                vchPaterno,
                vchMaterno,
                vchCalle,
                vchNumExt,
                vchNumInt,
                vchColonia,
                intEdad,
                vchSexo,
                vchTelefono,
                fltMetrosCasa,
                vchColor,
                vchLatitud,
                vchLongitud,
                vchFotoIdentificacionFrente,
                vchFotoIdentificacionReverso,
                vchFotoComprobante,
                vchFotoAnuenciaTrabajo,
                vchFotoFachadaAntes,
                vchFotoFachadaDespues,
                vchFotoConfirmidad,
                vchStatus,
                ""
        );

        hideKeyboard();
        String curp = binding.editTextCurp.getText().toString().trim();
        if (curp.length() < 18) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Error");
            innerBuilder.setMessage("La CURP deben ser 18 caracteres. Vuelva a intentarlo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        if (!curpValida(vchCURP)) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Error");
            innerBuilder.setMessage("La CURP ingresada no es valida. Vuelve a intentarlo").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        String rfc = binding.editTextRfc.getText().toString().trim();
        if (!(rfc.length() == 0) && !(rfc.isEmpty())) {
            if (rfc.length() < 12) {
                AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
                innerBuilder.setIcon(R.drawable.baseline_error_24);
                innerBuilder.setTitle("Error");
                innerBuilder.setMessage("El dato RFC deben ser 12 caracteres. Vuelva a introducir el valor.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideCustomProgressDialog();
                        dialogInterface.dismiss();
                    }
                });
                innerBuilder.create().show();
                return;
            }
        }

        String inEdad = binding.editTextEdad.getText().toString().trim();
        if (inEdad.isEmpty()) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Error");
            innerBuilder.setMessage("Por favor, ingrese la edad.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        try {
            int edad = Integer.parseInt(inEdad);
            if (edad <= 18) {
                AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
                innerBuilder.setIcon(R.drawable.baseline_error_24);
                innerBuilder.setTitle("Error");
                innerBuilder.setMessage("La edad debe ser mayor que 18.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideCustomProgressDialog();
                        dialogInterface.dismiss();
                    }
                });
                innerBuilder.create().show();
                return;
            }
        } catch (NumberFormatException e) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Error");
            innerBuilder.setMessage("Ingrese una edad válida.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        if (databaseHelper.isDuplicateBeneficiario(vchCURP)) {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Error");
            innerBuilder.setMessage("El CURP ya esta registrado. Vuelva a intentarlo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
            return;
        }

        if (!binding.editTextCurp.getText().toString().isEmpty() &&
                !binding.editTextNombre.getText().toString().isEmpty() &&
                !binding.editTextPaterno.getText().toString().isEmpty() &&
                !binding.editTextEdad.getText().toString().isEmpty() &&
                !binding.editTextCalle.getText().toString().isEmpty() &&
                !binding.editTextColonia.getText().toString().isEmpty() &&
                !binding.editTextNumExt.getText().toString().isEmpty() &&
                !binding.editTextMetros.getText().toString().isEmpty()) {

            long insertedRowId = databaseHelper.insertBeneficiario(lectura);

            if (!isInternetConnected()) {
                AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
                innerBuilder.setIcon(R.drawable.baseline_error_24);
                innerBuilder.setTitle("Error");
                innerBuilder.setMessage("La conexión a internet es débil o se ha perdido, se ha logrado guardar los datos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideCustomProgressDialog();
                        dialogInterface.dismiss();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
                innerBuilder.create().show();
                return;
            }

            if (insertedRowId != -1) {
                if (!isInternetConnected()) {
                    AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
                    innerBuilder.setIcon(R.drawable.baseline_error_24);
                    innerBuilder.setTitle("Error");
                    innerBuilder.setMessage("La conexión a internet es débil o se ha perdido, se ha logrado guardar los datos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hideCustomProgressDialog();
                            dialogInterface.dismiss();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    innerBuilder.create().show();
                    return;
                }

                AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
                innerBuilder.setIcon(R.drawable.baseline_check_24);
                innerBuilder.setTitle("Exito");
                innerBuilder.setMessage("El registro fue guardado con exito. Se procederá a enviarlo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendLecturaToServer(lectura);
                        hideCustomProgressDialog();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

                AlertDialog innerDialog = innerBuilder.create();
                innerDialog.setCanceledOnTouchOutside(false);

                innerDialog.show();
            } else {
                String errorMessage = ErrorManager.getAndClearErrorMessage(Detailed.this);
                showErrorDialog(errorMessage);
            }
        } else {
            AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
            innerBuilder.setIcon(R.drawable.baseline_error_24);
            innerBuilder.setTitle("Error");
            innerBuilder.setMessage("Es obligatorio ingresar todos los datos con asterico '*', vuelve a intentarlo.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideCustomProgressDialog();
                    dialogInterface.dismiss();
                }
            });
            innerBuilder.create().show();
        }
    }

    private void showErrorDialog(String errorMessage) {
        AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
        innerBuilder.setIcon(R.drawable.baseline_error_24);
        innerBuilder.setTitle("Error");
        innerBuilder.setMessage("Hubo un error al guardar los datos: " + errorMessage).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideCustomProgressDialog();
                dialogInterface.dismiss();
            }
        });
        innerBuilder.create().show();
    }

    public void sendLecturaToServer(LecturaBeneficiarios lectura) {
        ArrayList<LecturaBeneficiarios> benefList = new ArrayList<>();
        benefList.add(lectura);
        databaseHelper.sendBeneficiariosToServer(benefList);
        databaseHelper.setDataUpdateListener(Detailed.this);
        databaseHelper.setDatabaseCallback(Detailed.this);
    }

    private void showCustomProgressDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Detailed.this);
        View view = getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);

        customProgressDialog = builder.create();
        customProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        customProgressDialog.show();
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

    private void checkDataChanges() {
        if (!binding.editTextCurp.getText().toString().isEmpty() ||
                !binding.editTextRfc.getText().toString().isEmpty() ||
                !binding.editTextNombre.getText().toString().isEmpty() ||
                !binding.editTextPaterno.getText().toString().isEmpty() ||
                !binding.editTextMaterno.getText().toString().isEmpty() ||
                !binding.editTextEdad.getText().toString().isEmpty() ||
                !binding.editTextTelefono.getText().toString().isEmpty() ||
                !binding.editTextCalle.getText().toString().isEmpty() ||
                !binding.editTextColonia.getText().toString().isEmpty() ||
                !binding.editTextNumExt.getText().toString().isEmpty() ||
                !binding.editTextNumInt.getText().toString().isEmpty() ||
                !binding.editTextMetros.getText().toString().isEmpty() ||
                !binding.editTextColor.getText().toString().isEmpty()) {
            dataChanged = true;
        } else {
            dataChanged = false;
        }
    }

    private void showOptionsDialog() {
        hideKeyboard();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_baseline_upload_24);
        builder.setTitle("Cambios sin guardar");
        builder.setMessage("Hay cambios sin guardar. ¿Deseas guardar los cambios antes de salir?");
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!binding.editTextCurp.getText().toString().isEmpty() ||
                        !binding.editTextNombre.getText().toString().isEmpty() ||
                        !binding.editTextPaterno.getText().toString().isEmpty() ||
                        !binding.editTextEdad.getText().toString().isEmpty() ||
                        !binding.editTextCalle.getText().toString().isEmpty() ||
                        !binding.editTextColonia.getText().toString().isEmpty() ||
                        !binding.editTextNumExt.getText().toString().isEmpty() ||
                        !binding.editTextMetros.getText().toString().isEmpty() ||
                        binding.editTextColor.getText().toString().isEmpty()) {

                    AlertDialog.Builder innerBuilder = new AlertDialog.Builder(Detailed.this);
                    innerBuilder.setIcon(R.drawable.baseline_error_24);
                    innerBuilder.setTitle("Advertencia");
                    innerBuilder.setMessage("Por favor, completa todos los campos obligatorios antes de guardar los datos.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    innerBuilder.create().show();
                } else {
                    saveData();
                }
            }
        });

        builder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        builder.create().show();
    }

    // GESTION ERRORES
    @Override
    public void onDataUpdated(ArrayList<ListDataEntradas> updatedData) {

    }

    public void onDatabaseError(String errorMessage) {
        Log.e(TAG, "Database Error: " + errorMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mostrarDialogoError(errorMessage);
            }
        });
    }

    private void mostrarDialogoError(String errorMessage) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Detailed.this);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permiso de ubicación no concedido
                Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // CONEXION DE INTERNET
    public boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }

    public static boolean curpValida(String curp) {
        String regex = "^[A-Z][AEIOUX][A-Z]{2}\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])[HM](?:AS|B[CS]|C[CLMSH]|D[FG]|G[TR]|HG|JC|M[CNS]|N[ETL]|OC|PL|Q[TR]|S[PLR]|T[CSL]|VZ|YN|ZS)[B-DF-HJ-NP-TV-Z]{3}[A-Z\\d](\\d)$";
        boolean validado = curp.matches(regex);

        if (!validado) {
            Log.d(TAG, "Formato general incorrecto");
            return false;
        }

        // Validar que coincida el dígito verificador
        if (curp.charAt(17) != digitoVerificador(curp.substring(0, 17))) {
            Log.d(TAG, "Dígito verificador incorrecto");
            return false;
        }

        return true; // Validado
    }

    private static char digitoVerificador(String curp17) {
        final String diccionario = "0123456789ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
        int suma = 0;

        for (int i = 0; i < 17; i++) {
            char c = curp17.charAt(i);
            int pos = diccionario.indexOf(c);
            suma += (18 - i) * pos;
        }

        int residuo = suma % 10;
        int digito = 10 - residuo;
        if (digito == 10) {
            return '0';
        } else {
            return diccionario.charAt(digito);
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
                        Toast.makeText(Detailed.this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                        showLocationSettingsDialog();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(Detailed.this, "Obtención de localización via GPS", Toast.LENGTH_SHORT).show();
                    String errorMessage = e.getMessage();
                    obtainLocationViaGPS();
                });
    }

    private void obtainLocationViaGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        latitud = String.valueOf(latitude);
                        longitud = String.valueOf(longitude);

                        locationManager.removeUpdates(this);
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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Aquí puedes realizar las acciones necesarias según el cambio de configuración
        // Por ejemplo, ajustar el diseño de la UI si es necesario
    }

}