package com.l3mdev.AppCruzada;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ListAdapterOpcionesClave extends ArrayAdapter<ListDataOpcionesClave> {
    public ListAdapterOpcionesClave(@NonNull Context context, ArrayList<ListDataOpcionesClave> dataArrayList) {
        // Constructor de la clase ListAdapter que hereda de ArrayAdapter.
        // Recibe el contexto y la lista de datos como parámetros.
        super(context, R.layout.list_item_opciones_clave, dataArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        // Método getView para obtener la vista de cada elemento en la lista.
        // Recibe la posición del elemento, una vista previa opcional y el padre de las vistas como parámetros.

        ListDataOpcionesClave listDataOpcionesClave = getItem(position); // Obtener los datos del elemento en la posición actual.

        if (view == null) {
            // Si la vista previa es nula, inflar el diseño del elemento de la lista.
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_opciones_clave, parent, false);
        }

        // ImageView listImage = view.findViewById(R.id.listImageListItem); // Referencia a la imagen en la vista.
        TextView listNameNumerodeClave = view.findViewById(R.id.listNameNumerodeClave); // Referencia al nombre en la vista.
        TextView listNameidentificador = view.findViewById(R.id.listNameIdentificador);
        //TextView listTime = view.findViewById(R.id.listTime); // Referencia al tiempo en la vista.

        //listImage.setImageResource(listData.image); // Establecer la imagen del elemento actual.
        listNameNumerodeClave.setText(listDataOpcionesClave.numerodeclave); // Establecer el nombre del elemento actual.
        listNameidentificador.setText(listDataOpcionesClave.identificador);
        //listTime.setText(listData.time); // Establecer el tiempo del elemento actual.

        return view; // Devolver la vista del elemento actualizada.
    }
}
