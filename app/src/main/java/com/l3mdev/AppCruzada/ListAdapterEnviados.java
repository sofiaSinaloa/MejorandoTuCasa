package com.l3mdev.AppCruzada;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;


public class ListAdapterEnviados extends ArrayAdapter<ListDataEnviados> {
    private ArrayList<ListDataEnviados> dataArrayList;
    private ArrayList<ListDataEnviados> filteredArrayList;
    private ArrayList<ListDataEnviados> originalArrayList;

    public ListAdapterEnviados(@NonNull Context context, ArrayList<ListDataEnviados> dataArrayList) {
        super(context, R.layout.list_item_enviados, dataArrayList);
        this.dataArrayList = dataArrayList;
        this.filteredArrayList = new ArrayList<>(dataArrayList);
        this.originalArrayList = new ArrayList<>(dataArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        // Método getView para obtener la vista de cada elemento en la lista.
        // Recibe la posición del elemento, una vista previa opcional y el padre de las vistas como parámetros.

        ListDataEnviados listDataEnviados = getItem(position); // Obtener los datos del elemento en la posición actual.

        if (view == null) {
            // Si la vista previa es nula, inflar el diseño del elemento de la lista.
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_enviados, parent, false);
        }

        TextView sector = view.findViewById(R.id.itemSectorEnviados); // Referencia al nombre en la vista.
        TextView ruta = view.findViewById(R.id.itemRutaEnviados);
        TextView secuencia = view.findViewById(R.id.itemSecuenciaEnviados);
        TextView contrato = view.findViewById(R.id.itemContratoEnviados);
        TextView domicilio = view.findViewById(R.id.itemDomicilioEnviados);
        TextView fecha = view.findViewById(R.id.itemFechaEnviados);


        sector.setText(listDataEnviados.sector); // Establecer el nombre del elemento actual.
        ruta.setText(listDataEnviados.ruta);
        secuencia.setText(listDataEnviados.secuencia);
        contrato.setText(listDataEnviados.contrato);
        domicilio.setText(listDataEnviados.domicilio);
        fecha.setText(listDataEnviados.fecha);

        return view; // Devolver la vista del elemento actualizada.
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            private boolean isCleared = false;
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<ListDataEnviados> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    // Si no hay texto de búsqueda, mostrar la lista original en lugar de filteredList
                    filteredList.addAll(originalArrayList);
                    isCleared = true;
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    String[] filterArray = filterPattern.split(" ");

                    for (ListDataEnviados item : originalArrayList) {
                        boolean match = true;
                        for (String filterWord : filterArray) {
                            if (!(item.sector.toLowerCase().contains(filterWord)
                                    || item.ruta.toLowerCase().contains(filterWord)
                                    || item.secuencia.toLowerCase().contains(filterWord)
                                    || item.contrato.toLowerCase().contains(filterWord)
                                    || item.domicilio.toLowerCase().contains(filterWord)
                                    || item.fecha.toLowerCase().contains(filterWord))) {
                                match = false;
                                break;
                            }
                        }

                        if (match) {
                            filteredList.add(item);
                        }
                    }

                    isCleared = false;
                }

                filterResults.values = filteredList;
                filterResults.count = filteredList.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (!isCleared) {
                    dataArrayList.clear();
                    dataArrayList.addAll((ArrayList<ListDataEnviados>) results.values);
                    notifyDataSetChanged();
                } else {
                    clear();
                    addAll((ArrayList<ListDataEnviados>) results.values);
                    notifyDataSetChanged();
                }

                if (results.count == 0) {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    public void updateData(ArrayList<ListDataEnviados> newData) {
        dataArrayList.clear();
        dataArrayList.addAll(newData);
        notifyDataSetChanged();
    }
}
