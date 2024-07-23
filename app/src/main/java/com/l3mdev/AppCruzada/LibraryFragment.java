package com.l3mdev.AppCruzada;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.l3mdev.AppCruzada.R;


public class LibraryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate la actividad para este fragment
        return inflater.inflate(R.layout.fragment_library, container, false);
    }
}