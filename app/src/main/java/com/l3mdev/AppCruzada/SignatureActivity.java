package com.l3mdev.AppCruzada;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignatureActivity extends AppCompatActivity {

    private SignatureView captureBitmapView;
    private Button btnClear, btnSave;

    private String currentSignaturePath; // Variable para almacenar la ruta del archivo de la firma

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        captureBitmapView = findViewById(R.id.signatureView);
        btnClear = findViewById(R.id.btnClear);
        btnSave = findViewById(R.id.btnSave);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureBitmapView.clearCanvas();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSignature();
            }
        });
    }

    private void saveSignature() {
        Bitmap signatureBitmap = captureBitmapView.getBitmap();
        if (signatureBitmap != null) {
            // Guardar la firma como archivo de imagen
            File signatureFile = createImageFile();
            if (signatureFile != null) {
                saveBitmapToFile(signatureBitmap, signatureFile);
                currentSignaturePath = signatureFile.getAbsolutePath();
                Toast.makeText(this, "Firma guardada", Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("signaturePath", currentSignaturePath);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Error al guardar la firma", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Primero dibuja una firma", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "firmaGuardada_JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null) {
                File imageFile = File.createTempFile(
                        imageFileName,  // Nombre del archivo
                        ".jpg",        // Extensión del archivo
                        storageDir     // Directorio de almacenamiento
                );

                return imageFile;
            } else {
                // Error: No se pudo obtener el directorio de almacenamiento externo
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Error al crear el archivo
            return null;
        }
    }

    private void saveBitmapToFile(Bitmap bitmap, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentSignaturePath() {
        return currentSignaturePath;
    }
}
