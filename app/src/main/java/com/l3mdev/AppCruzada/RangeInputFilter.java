package com.l3mdev.AppCruzada;

import android.text.InputFilter;
import android.text.Spanned;

public class RangeInputFilter implements InputFilter {
    private float min, max;

    public RangeInputFilter(float min, float max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            // Concatenamos el texto actual con el nuevo texto
            String newVal = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend);
            float input = Float.parseFloat(newVal);
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException ignored) {
        }
        // Si no está en el rango, devolvemos una cadena vacía para evitar que se añada el nuevo texto al EditText
        return "";
    }

    private boolean isInRange(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}

