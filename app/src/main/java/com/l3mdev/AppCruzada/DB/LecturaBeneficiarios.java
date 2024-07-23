package com.l3mdev.AppCruzada.DB;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LecturaBeneficiarios {

    private int intIdAct;
    private String vchCURP;
    private String vchRFC;
    private String vchNombre;
    private String vchPaterno;
    private String vchMaterno;
    private String vchCalle;
    private String vchNumExt;
    private String vchNumInt;
    private String vchColonia;
    private String intEdad;
    private String vchSexo;
    private String vchTelefono;
    private String fltMetrosCasa;
    private String vchColor;
    private String vchLatitud;
    private String vchLongitud;
    private String vchFotoIdentificacionFrente;
    private String vchFotoIdentificacionReverso;
    private String vchFotoComprobante;
    private String vchFotoAnuenciaTrabajo;
    private String vchFotoFachadaAntes;
    private String vchFotoFachadaDespues;
    private String vchFotoConfirmidad;
    private String vchfactura;
    private String status;
    public LecturaBeneficiarios( int intIdBeneficiario, String vchCURP, String vchRFC, String vchNombre, String vchPaterno, String vchMaterno, String vchCalle, String vchNumExt, String vchNumInt, String vchColonia, String intEdad, String vchSexo, String vchTelefono, String fltMetrosCasa, String vchColor, String vchLatitud, String vchLongitud, String vchFotoIdentificacionFrente, String vchFotoIdentificacionReverso, String vchFotoComprobante, String vchFotoAnuenciaTrabajo, String vchFotoFachadaAntes, String vchFotoFachadaDespues, String vchFotoConfirmidad, String status, String vchfactura) {
        this.intIdAct = intIdBeneficiario;
        this.vchCURP = vchCURP;
        this.vchRFC = vchRFC;
        this.vchNombre = vchNombre;
        this.vchPaterno = vchPaterno;
        this.vchMaterno = vchMaterno;
        this.vchCalle = vchCalle;
        this.vchNumExt = vchNumExt;
        this.vchNumInt = vchNumInt;
        this.vchColonia = vchColonia;
        this.intEdad = intEdad;
        this.vchSexo = vchSexo;
        this.vchTelefono = vchTelefono;
        this.fltMetrosCasa = fltMetrosCasa;
        this.vchColor = vchColor;
        this.vchLatitud = vchLatitud;
        this.vchLongitud = vchLongitud;
        this.vchFotoIdentificacionFrente = vchFotoIdentificacionFrente;
        this.vchFotoIdentificacionReverso = vchFotoIdentificacionReverso;
        this.vchFotoComprobante = vchFotoComprobante;
        this.vchFotoAnuenciaTrabajo = vchFotoAnuenciaTrabajo;
        this.vchFotoFachadaAntes = vchFotoFachadaAntes;
        this.vchFotoFachadaDespues = vchFotoFachadaDespues;
        this.vchFotoConfirmidad = vchFotoConfirmidad;
        this.vchfactura = vchfactura;
        this.status = status;
    }

    public LecturaBeneficiarios() {

    }

    public int getIntIdBeneficiario() {
        return intIdAct;
    }

    public void setIntIdBeneficiario(int intIdBeneficiario) {
        this.intIdAct = intIdBeneficiario;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVchCURP() {
        return vchCURP;
    }

    public void setVchCURP(String vchCURP) {
        this.vchCURP = vchCURP;
    }

    public String getVchRFC() {
        return vchRFC;
    }

    public void setVchRFC(String vchRFC) {
        this.vchRFC = vchRFC;
    }

    public String getVchNombre() {
        return vchNombre;
    }

    public void setVchNombre(String vchNombre) {
        this.vchNombre = vchNombre;
    }

    public String getVchPaterno() {
        return vchPaterno;
    }

    public void setVchPaterno(String vchPaterno) {
        this.vchPaterno = vchPaterno;
    }

    public String getVchMaterno() {
        return vchMaterno;
    }

    public void setVchMaterno(String vchMaterno) {
        this.vchMaterno = vchMaterno;
    }

    public String getVchCalle() {
        return vchCalle;
    }

    public void setVchCalle(String vchCalle) {
        this.vchCalle = vchCalle;
    }

    public String getVchNumExt() {
        return vchNumExt;
    }

    public void setVchNumExt(String vchNumExt) {
        this.vchNumExt = vchNumExt;
    }

    public String getVchNumInt() {
        return vchNumInt;
    }

    public void setVchNumInt(String vchNumInt) {
        this.vchNumInt = vchNumInt;
    }

    public String getVchColonia() {
        return vchColonia;
    }

    public void setVchColonia(String vchColonia) {
        this.vchColonia = vchColonia;
    }

    public String getIntEdad() {
        return intEdad;
    }

    public void setIntEdad(String intEdad) {
        this.intEdad = intEdad;
    }

    public String getVchSexo() {
        return vchSexo;
    }

    public void setVchSexo(String vchSexo) {
        this.vchSexo = vchSexo;
    }

    public String getVchTelefono() {
        return vchTelefono;
    }

    public void setVchTelefono(String vchTelefono) {
        this.vchTelefono = vchTelefono;
    }

    public String getFltMetrosCasa() {
        return fltMetrosCasa;
    }

    public void setFltMetrosCasa(String fltMetrosCasa) {
        this.fltMetrosCasa = fltMetrosCasa;
    }

    public String getVchColor() {
        return vchColor;
    }

    public void setVchColor(String vchColor) {
        this.vchColor = vchColor;
    }

    public String getVchLatitud() {
        return vchLatitud;
    }

    public void setVchLatitud(String vchLatitud) {
        this.vchLatitud = vchLatitud;
    }

    public String getVchLongitud() {
        return vchLongitud;
    }

    public void setVchLongitud(String vchLongitud) {
        this.vchLongitud = vchLongitud;
    }

    public String getVchFotoIdentificacionFrente() {
        return vchFotoIdentificacionFrente;
    }

    public void setVchFotoIdentificacionFrente(String vchFotoIdentificacionFrente) {
        this.vchFotoIdentificacionFrente = vchFotoIdentificacionFrente;
    }

    public String getVchFotoIdentificacionReverso() {
        return vchFotoIdentificacionReverso;
    }

    public void setVchFotoIdentificacionReverso(String vchFotoIdentificacionReverso) {
        this.vchFotoIdentificacionReverso = vchFotoIdentificacionReverso;
    }

    public String getVchFotoComprobante() {
        return vchFotoComprobante;
    }

    public void setVchFotoComprobante(String vchFotoComprobante) {
        this.vchFotoComprobante = vchFotoComprobante;
    }

    public String getVchFotoAnuenciaTrabajo() {
        return vchFotoAnuenciaTrabajo;
    }

    public void setVchFotoAnuenciaTrabajo(String vchFotoAnuenciaTrabajo) {
        this.vchFotoAnuenciaTrabajo = vchFotoAnuenciaTrabajo;
    }

    public String getVchFotoFachadaAntes() {
        return vchFotoFachadaAntes;
    }

    public void setVchFotoFachadaAntes(String vchFotoFachadaAntes) {
        this.vchFotoFachadaAntes = vchFotoFachadaAntes;
    }

    public String getVchFotoFachadaDespues() {
        return vchFotoFachadaDespues;
    }

    public void setVchFotoFachadaDespues(String vchFotoFachadaDespues) {
        this.vchFotoFachadaDespues = vchFotoFachadaDespues;
    }

    public String getVchFotoConfirmidad() {
        return vchFotoConfirmidad;
    }

    public void setVchFotoConfirmidad(String vchFotoConfirmidad) {
        this.vchFotoConfirmidad = vchFotoConfirmidad;
    }

    public String getVchfactura() {
        return vchfactura;
    }

    public void setVchfactura(String vchfactura) {
        this.vchfactura = vchfactura;
    }


    public void actualizarStatus(SQLiteDatabase database, String nuevoStatus) {
        if ("enviando".equals(this.status)) {
            ContentValues values = new ContentValues();
            values.put("status_envio", nuevoStatus);
            database.update("beneficiario_nuevo", values,   "curp = ?", new String[]{String.valueOf(vchCURP)});
        }
    }

    public void actualizarStatusObtenidos(SQLiteDatabase database, String nuevoStatus) {
        if ("enviando".equals(this.status)) {
            ContentValues values = new ContentValues();
            values.put("status_envio", nuevoStatus);
            database.update("beneficiario_obtenidos", values, "curp = ?", new String[]{String.valueOf(vchCURP)});
        } else {
            Log.d(TAG, "actualizarStatusObtenidos: no lo hace");
        }
    }
}
