package com.l3mdev.AppCruzada;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.l3mdev.AppCruzada.DB.DatabaseHelper;
import com.l3mdev.AppCruzada.R;

public class HomeFragment extends Fragment {
    CardView formulariosCard, entradasCardHome, enviadosCardHome;
    SharedPreferences sharedPreference;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLES = "roles";


    private DatabaseHelper databaseHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        databaseHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Inflar el diseño para este fragmento
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // Obtener referencias a los elementos de la interfaz de usuario
        formulariosCard = v.findViewById(R.id.formulariosCardHome);
        entradasCardHome = v.findViewById(R.id.entradasCardHome);
        sharedPreference = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        // Obtener el nombre y contraseña almacenados en las preferencias compartidas
        String name = sharedPreference.getString(KEY_NAME, "");
        String password = sharedPreference.getString(KEY_PASSWORD, null);
        TextView textViewDescUsuHome = v.findViewById(R.id.textViewDescUsuHome);
        textViewDescUsuHome.setText("Bienvenido " + name);



        // Obtener el rol almacenado en SharedPreferences
        String roles = sharedPreference.getString(KEY_ROLES, "");

        // Validar el rol y configurar la visibilidad de los CardView
        if ("pintor".equalsIgnoreCase(roles)) {
            // Si el rol es "pintor", mostrar solo formulariosCard y ocultar entradasCardHome
            entradasCardHome.setVisibility(View.GONE);
            formulariosCard.setVisibility(View.VISIBLE);
        } else if ("encuestador".equalsIgnoreCase(roles)) {
            // Si el rol es "encuestador", mostrar solo entradasCardHome y ocultar formulariosCard
            formulariosCard.setVisibility(View.GONE);
            entradasCardHome.setVisibility(View.VISIBLE);
        } else {
            // Si el rol no está definido correctamente, ocultar ambos CardView por seguridad
            formulariosCard.setVisibility(View.GONE);
            entradasCardHome.setVisibility(View.GONE);
        }


        // Configurar el OnClickListener para el CardView de formularios
        formulariosCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abrir la actividad de formularios al hacer clic en el CardView
                Intent intent = new Intent(getActivity(), Enviados.class);
                startActivity(intent);
            }
        });

        entradasCardHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Entradas.class);
                startActivity(intent);
            }
        });


        // Devolver la vista inflada
        return v;
    }
}