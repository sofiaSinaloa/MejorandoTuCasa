package com.l3mdev.AppCruzada;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

public class CambioContraseña extends AppCompatDialogFragment {

    // Esta clase no contiene actividad en ella, externa.

    //EditText para el cambio de contraseña
    private EditText editTextcambiarcontraseña, editTextcontraseñactual, editTextconfirmarcontraseñactual;

    //DialogListener para que el metodo sea llamado una vez se presione el boton en la actividad donde se implementó
    private ExampleDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        // Configuración del diálogo
        builder.setView(view) // Establece la vista inflada como la vista del diálogo
                .setTitle("Actualizar Contraseña") // Establece el título del diálogo como "Actualizar Contraseña"
                .setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Lógica cuando se hace clic en el botón "cancelar"
                    }
                })
                .setPositiveButton("confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Lógica cuando se hace clic en el botón "confirmar"
                        String contraseñactual = editTextcontraseñactual.getText().toString();
                        String cambiarcontraseña = editTextcambiarcontraseña.getText().toString();
                        String confirmarcontraseñaactual = editTextconfirmarcontraseñactual.getText().toString();
                        listener.applyTexts(cambiarcontraseña);
                    }
                });

        // Asignación de vistas
        editTextcambiarcontraseña = view.findViewById(R.id.edit_cambiarcontraseña);
        editTextcontraseñactual = view.findViewById(R.id.edit_contraseñaactual);
        editTextconfirmarcontraseñactual = view.findViewById(R.id.edit_confirmarcambiarcontraseña);

        return builder.create(); // Devuelve el diálogo creado
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ExampleDialogListener) context; // Intenta asignar la actividad al objeto listener
        } catch (ClassCastException e) {
            throw new ClassCastException(context +
                    "must implement ExampleDialogListener"); // Lanza una excepción si la actividad no implementa la interfaz ExampleDialogListener
        }
    }

    public interface ExampleDialogListener {
        void applyTexts(String cambiarcontraseña); // Interfaz que define el método applyTexts()
    }
}