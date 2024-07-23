
package com.l3mdev.AppCruzada;

public class ListDataEntradas {

    // Variables de instancia para almacenar los datos de cada elemento de la lista
    String Nid; // Nombre del elemento
    String Ncurp;
    String Nrfc;
    String Nnombre;
    String Npaterno;
    String Nmaterno;
    String Ncalle;
    String NnumExt;
    String NnumInt;
    String Ncolonia;


    // Constructor de la clase ListData para inicializar los datos de cada elemento
    public ListDataEntradas(String id, String curp, String rfc, String nombre,
                            String paterno, String materno, String calle, String numExt, String numInt, String colonia) {
        this.Nid = id; // Asignar el nombre recibido al nombre del elemento
        this.Ncurp = curp;
        this.Nrfc = rfc;
        this.Nnombre = nombre;
        this.Npaterno = paterno;
        this.Nmaterno = materno;
        this.Ncalle = calle;
        this.NnumExt = numExt;
        this.NnumInt = numInt;
        this.Ncolonia = colonia;

    }

    public String getNcolonia() {
        return Ncolonia;
    }

    public void setNcolonia(String ncolonia) {
        Ncolonia = ncolonia;
    }

    public String getNid() {
        return Nid;
    }

    public void setNid(String nid) {
        Nid = nid;
    }

    public String getNcurp() {
        return Ncurp;
    }

    public void setNcurp(String ncurp) {
        Ncurp = ncurp;
    }

    public String getNrfc() {
        return Nrfc;
    }

    public void setNrfc(String nrfc) {
        Nrfc = nrfc;
    }

    public String getNnombre() {
        return Nnombre;
    }

    public void setNnombre(String nnombre) {
        Nnombre = nnombre;
    }

    public String getNpaterno() {
        return Npaterno;
    }

    public void setNpaterno(String npaterno) {
        Npaterno = npaterno;
    }

    public String getNmaterno() {
        return Nmaterno;
    }

    public void setNmaterno(String nmaterno) {
        Nmaterno = nmaterno;
    }

    public String getNcalle() {
        return Ncalle;
    }

    public void setNcalle(String ncalle) {
        Ncalle = ncalle;
    }

    public String getNnumExt() {
        return NnumExt;
    }

    public void setNnumExt(String nnumExt) {
        NnumExt = nnumExt;
    }

    public String getNnumInt() {
        return NnumInt;
    }

    public void setNnumInt(String nnumInt) {
        NnumInt = nnumInt;
    }
}