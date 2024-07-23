package com.l3mdev.AppCruzada;

public class ListDataEnviados {
    // Variables de instancia para almacenar los datos de cada elemento de la lista
    String sector; // Nombre del elemento
    String ruta;
    String secuencia;
    String contrato;
    String domicilio;
    String numerodemedidor;
    String anterior;
    String advertencia;
    String fecha;

    // Constructor de la clase ListData para inicializar los datos de cada elemento
    public ListDataEnviados(String name, String ruta, String secuencia, String contrato, String domicilio, String numerodemedidor, String anterior, String advertencia, String fecha) {
        this.sector = name; // Asignar el nombre recibido al nombre del elemento
        this.ruta = ruta;
        this.secuencia = secuencia;
        this.contrato = contrato;
        this.domicilio = domicilio;
        this.numerodemedidor = numerodemedidor;
        this.anterior = anterior;
        this.advertencia = advertencia;
        this.fecha = fecha;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(String secuencia) {
        this.secuencia = secuencia;
    }

    public String getContrato() {
        return contrato;
    }

    public void setContrato(String contrato) {
        this.contrato = contrato;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getNumerodemedidor() {
        return numerodemedidor;
    }

    public void setNumerodemedidor(String numerodemedidor) {
        this.numerodemedidor = numerodemedidor;
    }

    public String getAnterior() {
        return anterior;
    }

    public void setAnterior(String anterior) {
        this.anterior = anterior;
    }

    public String getAdvertencia() {
        return advertencia;
    }

    public void setAdvertencia(String advertencia) {
        this.advertencia = advertencia;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
