package com.l3mdev.AppCruzada.DB;

public class LecturaEndpoint {
    private String IntIdPadron;
    private String intidPaquete;
    private String intidSucursal;
    private String VchNombreSector;
    private String VchNombreRuta;
    private String SitNumSecuenciaRuta;
    private String NumContrato;
    private String VchDetalleDireccion;
    private String SitConsumoMes;
    private String SitConsumoPromedio;
    private String LecturaAnterior;
    private String vchNumMedidor;
    private String LecturaActual;
    private String ClaveLectura;
    private String vchNombreLecturista;
    private String sdtfechaRecepcion;
    private String sdtfechaActualizacion;
    private String vchLatitud;
    private String vchLongitud;
    private String rutaFotografiaClave;
    private String advertencia;
    private String status;
    private String statusEnvio;
    private String rutaFotografiaAdvertencia;


    public LecturaEndpoint(){

    }
    public LecturaEndpoint( String intIdPadron, String intidPaquete, String intidSucursal, String vchNombreSector, String vchNombreRuta, String sitNumSecuenciaRuta,
                            String numContrato, String vchDetalleDireccion, String sitConsumoMes, String sitConsumoPromedio,
                            String LecturaAnterior, String vchNumMedidor, String LecturaActual, String claveLectura, String vchNombreLecturista, String sdtfechaRecepcion,
                            String sdtfechaActualizacion, String vchLatitud, String vchLongitud, String rutaFotografiaClave,
                            String advertencia, String status, String statusEnvio, String rutaFotografiaAdvertencia){

        this.IntIdPadron = intIdPadron;
        this.intidPaquete = intidPaquete;
        this.intidSucursal = intidSucursal;
        this.VchNombreSector = vchNombreSector;
        this.VchNombreRuta = vchNombreRuta;
        this.SitNumSecuenciaRuta = sitNumSecuenciaRuta;
        this.NumContrato = numContrato;
        this.VchDetalleDireccion = vchDetalleDireccion;
        this.SitConsumoMes = sitConsumoMes;
        this.SitConsumoPromedio = sitConsumoPromedio;
        this.LecturaAnterior = LecturaAnterior;
        this.vchNumMedidor = vchNumMedidor;
        this.LecturaActual = LecturaActual;
        this.ClaveLectura = claveLectura;
        this.vchNombreLecturista = vchNombreLecturista;
        this.sdtfechaRecepcion = sdtfechaRecepcion;
        this.sdtfechaActualizacion = sdtfechaActualizacion;
        this.vchLatitud = vchLatitud;
        this.vchLongitud = vchLongitud;
        this.rutaFotografiaClave = rutaFotografiaClave;
        this.advertencia = advertencia;
        this.status = status;
        this.statusEnvio = statusEnvio;
        this.rutaFotografiaAdvertencia = rutaFotografiaAdvertencia;
    }

    public String getStatusEnvio() {
        return statusEnvio;
    }

    public void setStatusEnvio(String statusEnvio) {
        this.statusEnvio = statusEnvio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntIdPadron() {
        return IntIdPadron;
    }

    public void setIntIdPadron(String intIdPadron) {
        this.IntIdPadron = intIdPadron;
    }

    public String getIntidPaquete() {
        return intidPaquete;
    }

    public void setIntidPaquete(String intidPaquete) {
        this.intidPaquete = intidPaquete;
    }

    public String getIntidSucursal() {
        return intidSucursal;
    }

    public void setIntidSucursal(String intidSucursal) {
        this.intidSucursal = intidSucursal;
    }

    public String getVchNombreSector() {
        return VchNombreSector;
    }

    public void setVchNombreSector(String vchNombreSector) {
        this.VchNombreSector = vchNombreSector;
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

    public String getSitConsumoPromedio() {
        return SitConsumoPromedio;
    }

    public void setSitConsumoPromedio(String sitConsumoPromedio) {
        SitConsumoPromedio = sitConsumoPromedio;
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

    public String getRutaFotografiaClave() {
        return rutaFotografiaClave;
    }

    public void setRutaFotografiaClave(String rutaFotografiaClave) {
        this.rutaFotografiaClave = rutaFotografiaClave;
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

}
