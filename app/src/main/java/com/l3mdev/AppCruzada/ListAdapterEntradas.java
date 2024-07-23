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

public class ListAdapterEntradas extends ArrayAdapter<ListDataEntradas> {
    private ArrayList<ListDataEntradas> dataArrayList;
    private ArrayList<ListDataEntradas> filteredArrayList;
    private ArrayList<ListDataEntradas> originalArrayList;

    public ListAdapterEntradas(@NonNull Context context, ArrayList<ListDataEntradas> dataArrayList) {
        super(context, R.layout.list_item_entradas, dataArrayList);
        this.dataArrayList = dataArrayList;
        this.filteredArrayList = new ArrayList<>(dataArrayList);
        this.originalArrayList = new ArrayList<>(dataArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        // Método getView para obtener la vista de cada elemento en la lista.
        // Recibe la posición del elemento, una vista previa opcional y el padre de las vistas como parámetros.

        ListDataEntradas listDataEntradas = getItem(position); // Obtener los datos del elemento en la posición actual.

        if (view == null) {
            // Si la vista previa es nula, inflar el diseño del elemento de la lista.
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_entradas, parent, false);
        }

        TextView nombre = view.findViewById(R.id.itemNombre); // Referencia al nombre en la vista.
        TextView paterno = view.findViewById(R.id.itemPaterno);
        TextView materno = view.findViewById(R.id.itemMaterno);
        TextView calle = view.findViewById(R.id.itemCalle);
        TextView numero = view.findViewById(R.id.itemNumero);
        TextView colonia = view.findViewById(R.id.itemColonia);


        nombre.setText(listDataEntradas.Nnombre);
        paterno.setText(listDataEntradas.Npaterno);
        materno.setText(listDataEntradas.Nmaterno);
        calle.setText(listDataEntradas.Ncalle);
        numero.setText(listDataEntradas.NnumExt);
        colonia.setText(listDataEntradas.Ncolonia);

        return view; // Devolver la vista del elemento actualizada.
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            private boolean isCleared = false;
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<ListDataEntradas> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    // Si no hay texto de búsqueda, mostrar la lista original en lugar de filteredList
                    filteredList.addAll(originalArrayList);
                    isCleared = true;
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    String[] filterArray = filterPattern.split(" ");

                    for (ListDataEntradas item : originalArrayList) {
                        boolean match = true;
                        for (String filterWord : filterArray) {
                            if (!(item.Nnombre.toLowerCase().contains(filterWord)
                                    || item.Npaterno.toLowerCase().contains(filterWord)
                                    || item.Ncalle.toLowerCase().contains(filterWord)
                                    || item.NnumExt.toLowerCase().contains(filterWord))) {
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
                    dataArrayList.addAll((ArrayList<ListDataEntradas>) results.values);
                    notifyDataSetChanged();
                } else {
                    clear();
                    addAll((ArrayList<ListDataEntradas>) results.values);
                    notifyDataSetChanged();
                }

                if (results.count == 0) {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    public void updateData(ArrayList<ListDataEntradas> newData) {
        originalArrayList.clear();
        originalArrayList.addAll(newData);
        dataArrayList.clear();
        dataArrayList.addAll(newData);
        notifyDataSetChanged();
    }
}