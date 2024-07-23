package com.l3mdev.AppCruzada.DB;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class LecturaModel {
    private int COLUMN_ID_ENDPOINTS;
    private String IntIdPadron;
    private String intIdPaquete;
    private String intIdSucursal;
    private String VchNombreSector;
    private String VchNombreRuta;
    private String SitNumSecuenciaRuta;
    private String NumContrato;
    private String VchDetalleDireccion;
    private String SitConsumoMes;
    private String setSitConsumoPromedio;
    private String LecturaAnterior;
    private String vchNumMedidor;
    private String LecturaActual;
    private String ClaveLectura;
    private String vchNombreLecturista;
    private String sdtfechaRecepcion;
    private String sdtfechaActualizacion;
    private String vchLatitud;
    private String vchLongitud;
    private String rutaFotografia;
    private String advertencia;
    private String status;
    private String statusEnvio;
    private String rutaFotografiaAdvertencia;


    public LecturaModel() {
    }

    public LecturaModel(int COLUMN_ID_ENDPOINTS, String IntIdPadron, String intIdPaquete, String intIdSucursal,
                        String VchNombreSector, String VchNombreRuta, String SitNumSecuenciaRuta, String NumContrato,
                        String VchDetalleDireccion, String SitConsumoMes, String setSitConsumoPromedio,
                        String LecturaAnterior, String vchNumMedidor, String LecturaActual, String ClaveLectura,
                        String vchNombreLecturista, String sdtfechaRecepcion, String sdtfechaActualizacion,
                        String vchLatitud, String vchLongitud, String rutaFotografia, String advertencia,
                        String status, String statusEnvio, String rutaFotografiaAdvertencia) {

        this.COLUMN_ID_ENDPOINTS = COLUMN_ID_ENDPOINTS;
        this.IntIdPadron = IntIdPadron;
        this.intIdPaquete = intIdPaquete;
        this.intIdSucursal = intIdSucursal;
        this.VchNombreSector = VchNombreSector;
        this.VchNombreRuta = VchNombreRuta;
        this.SitNumSecuenciaRuta = SitNumSecuenciaRuta;
        this.NumContrato = NumContrato;
        this.VchDetalleDireccion = VchDetalleDireccion;
        this.SitConsumoMes = SitConsumoMes;
        this.setSitConsumoPromedio = setSitConsumoPromedio;
        this.LecturaAnterior = LecturaAnterior;
        this.vchNumMedidor = vchNumMedidor;
        this.LecturaActual = LecturaActual;
        this.ClaveLectura = ClaveLectura;
        this.vchNombreLecturista = vchNombreLecturista;
        this.sdtfechaRecepcion = sdtfechaRecepcion;
        this.sdtfechaActualizacion = sdtfechaActualizacion;
        this.vchLatitud = vchLatitud;
        this.vchLongitud = vchLongitud;
        this.rutaFotografia = rutaFotografia;
        this.advertencia = advertencia;
        this.status = status;
        this.statusEnvio = statusEnvio;
        this.rutaFotografiaAdvertencia = rutaFotografiaAdvertencia;
    }

    public void actualizarStatus(SQLiteDatabase database, String nuevoStatus) {
        Log.d(TAG, "actualizarStatus: entra al método");
        if ("enviando".equals(this.status)) {
            Log.d(TAG, "actualizarStatus: actualiza al nuevo status");
            ContentValues values = new ContentValues();
            values.put("status", nuevoStatus);
            database.update("lecturas", values, "num_contrato = ?", new String[]{String.valueOf(NumContrato)});
            Log.d(TAG, "actualizarStatus: " + getStatus());
        }
    }

    public int getCOLUMN_ID_ENDPOINTS() {
        return COLUMN_ID_ENDPOINTS;
    }

    public void setCOLUMN_ID_ENDPOINTS(int COLUMN_ID_ENDPOINTS) {
        this.COLUMN_ID_ENDPOINTS = COLUMN_ID_ENDPOINTS;
    }

    public String getIntIdPadron() {
        return IntIdPadron;
    }

    public void setIntIdPadron(String intIdPadron) {
        IntIdPadron = intIdPadron;
    }

    public String getIntIdPaquete() {
        return intIdPaquete;
    }

    public void setIntIdPaquete(String intIdPaquete) {
        this.intIdPaquete = intIdPaquete;
    }

    public String getIntIdSucursal() {
        return intIdSucursal;
    }

    public void setIntIdSucursal(String intIdSucursal) {
        this.intIdSucursal = intIdSucursal;
    }

    public String getVchNombreSector() {
        return VchNombreSector;
    }

    public void setVchNombreSector(String vchNombreSector) {
        VchNombreSector = vchNombreSector;
    }

    public String getVchNombreRuta() {
        return VchNombreRuta;
    }

    public void setVchNombreRuta(String vchNombreRuta) {
        VchNombreRuta = vchNombreRuta;
    }

    public String getSitNumSecuenciaRuta() {
        return SitNumSecuenciaRuta;
    }

    public void setSitNumSecuenciaRuta(String sitNumSecuenciaRuta) {
        SitNumSecuenciaRuta = sitNumSecuenciaRuta;
    }

    public String getNumContrato() {
        return NumContrato;
    }

    public void setNumContrato(String numContrato) {
        NumContrato = numContrato;
    }

    public String getVchDetalleDireccion() {
        return VchDetalleDireccion;
    }

    public void setVchDetalleDireccion(String vchDetalleDireccion) {
        VchDetalleDireccion = vchDetalleDireccion;
    }

    public String getSitConsumoMes() {
        return SitConsumoMes;
    }

    public void setSitConsumoMes(String sitConsumoMes) {
        SitConsumoMes = sitConsumoMes;
    }

    public String getSetSitConsumoPromedio() {
        return setSitConsumoPromedio;
    }

    public void setSetSitConsumoPromedio(String setSitConsumoPromedio) {
        this.setSitConsumoPromedio = setSitConsumoPromedio;
    }

    public String getLecturaAnterior() {
        return LecturaAnterior;
    }

    public void setLecturaAnterior(String lecturaAnterior) {
        LecturaAnterior = lecturaAnterior;
    }

    public String getVchNumMedidor() {
        return vchNumMedidor;
    }

    public void setVchNumMedidor(String vchNumMedidor) {
        this.vchNumMedidor = vchNumMedidor;
    }

    public String getLecturaActual() {
        return LecturaActual;
    }

    public void setLecturaActual(String lecturaActual) {
        LecturaActual = lecturaActual;
    }

    public String getClaveLectura() {
        return ClaveLectura;
    }

    public void setClaveLectura(String claveLectura) {
        ClaveLectura = claveLectura;
    }

    public String getVchNombreLecturista() {
        return vchNombreLecturista;
    }

    public void setVchNombreLecturista(String vchNombreLecturista) {
        this.vchNombreLecturista = vchNombreLecturista;
    }

    public String getSdtfechaRecepcion() {
        return sdtfechaRecepcion;
    }

    public void setSdtfechaRecepcion(String sdtfechaRecepcion) {
        this.sdtfechaRecepcion = sdtfechaRecepcion;
    }

    public String getSdtfechaActualizacion() {
        return sdtfechaActualizacion;
    }

    public void setSdtfechaActualizacion(String sdtfechaActualizacion) {
        this.sdtfechaActualizacion = sdtfechaActualizacion;
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

    public String getRutaFotografia() {
        return rutaFotografia;
    }

    public void setRutaFotografia(String rutaFotografia) {
        this.rutaFotografia = rutaFotografia;
    }

    public String getRutaFotografiaAdvertencia() {
        return rutaFotografiaAdvertencia;
    }

    public void setRutaFotografiaAdvertencia(String rutaFotografiaAdvertencia) {
        this.rutaFotografiaAdvertencia = rutaFotografiaAdvertencia;
    }

    public String getAdvertencia() {
        return advertencia;
    }

    public void setAdvertencia(String advertencia) {
        this.advertencia = advertencia;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusEnvio() {
        return statusEnvio;
    }

    public void setStatusEnvio(String statusEnvio) {
        this.statusEnvio = statusEnvio;
    }

}
