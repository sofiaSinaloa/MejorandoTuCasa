package com.l3mdev.AppCruzada;

public class OpcionesClaveData {
    private int id;
    private String numeroDeClave;
    private String identificador;
    private int AceptaLectura;

    public OpcionesClaveData(int id, String numeroDeClave, String identificador, int AceptaLectura) {
        this.id = id;
        this.numeroDeClave = numeroDeClave;
        this.identificador = identificador;
        this.AceptaLectura = AceptaLectura;
    }

    public int getId() {
        return id;
    }

    public String getNumeroDeClave() {
        return numeroDeClave;
    }

    public String getIdentificador() {
        return identificador;
    }

    public int getAceptaLectura() {
        return AceptaLectura;
    }
}