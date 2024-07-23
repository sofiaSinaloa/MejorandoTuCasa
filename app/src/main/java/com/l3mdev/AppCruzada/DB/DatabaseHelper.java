package com.l3mdev.AppCruzada.DB;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.l3mdev.AppCruzada.Entradas;
import com.l3mdev.AppCruzada.ListAdapterEntradas;
import com.l3mdev.AppCruzada.ListDataEntradas;
import com.l3mdev.AppCruzada.ListDataEnviados;
import com.l3mdev.AppCruzada.OpcionesClaveData;
import com.l3mdev.AppCruzada.interfaces.ApiClient;
import com.l3mdev.AppCruzada.interfaces.ApiService;
import com.l3mdev.AppCruzada.preference.ErrorManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DatabaseHelper extends SQLiteOpenHelper {

    private DatabaseCallback databaseCallback;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private DataUpdateListener dataUpdateListener;

    private Context context;
    private static final String SHARED_PREF_NAME = "mypref";

    private static final String KEY_TOKEN = "token";


    private static final String DATABASE_NAME = "lecturas.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "lecturas";
    private static final String TABLE_ENDPOINT = "lecturas_endpoint";
    private static final String TABLE_OPCIONESCLAVE = "lecturas_opcionesclave";


    // Nombres de las columnas
    private static final String COLUMN_ID_LECTURAS = "id_lecturas";
    private static final String COLUMN_ID_ENDPOINT = "id_endpoint";
    private static final String COLUMN_ID_ENDPOINTS = "id_endpoints";
    private static final String COLUMN_ID_OPCIONESCLAVE = "id_opcionesclave";
    private static final String IntIdPadron = "int_id_padron";
    private static final String intidPaquete = "int_id_paquete";
    private static final String intidSucursal = "int_id_sucursal";
    private static final String VchNombreSector = "vch_nombre_sector";
    private static final String VchNombreRuta = "vch_nombre_ruta";
    private static final String SitNumSecuenciaRuta = "sit_num_secuencia_ruta";
    private static final String NumContrato = "num_contrato";
    private static final String VchDetalleDireccion = "vch_detalle_direccion";
    private static final String SitConsumoMes = "sit_consumo_mes";
    private static final String SitConsumoPromedio = "SitConsumoPromedio";

    private static final String LecturaAnterior = "lectura_anterior";
    private static final String vchNumMedidor = "vch_num_medidor";
    private static final String LecturaActual = "lectura_actual";
    private static final String ClaveLectura = "clave_lectura";
    private static final String vchNombreLecturista = "vch_nombre_lecturista";
    private static final String sdtfechaRecepcion = "sdt_fecha_recepcion";
    private static final String sdtfechaActualizacion = "sdt_fecha_actualizacion";
    private static final String vchLatitud1 = "vch_latitud";
    private static final String vchLongitud1 = "vch_logitud";
    private static final String rutaFotografiaClave = "ruta_fotografia_clave";
    private static final String advertencia = "advertencia";
    private static final String status = "status";
    private static final String statusEnvio = "status_envio";
    private static final String numerodeclave = "numero_de_clave";
    private static final String identificador = "identificador";
    private static final String vchnumClaveLectura = "vch_num_clave_lectura";
    private static final String aceptaLectura = "acepta_lectura";

    private static final String rutaFotografiaAdvertencia = "ruta_fotografia_advertencia";


    // Tabla BENEFICIARIOS

    private static final String TABLE_BENEFICIARIO_NUEVO = "beneficiario_nuevo";
    private static final String TABLE_BENEFICIARIO_OBTENIDOS = "beneficiario_obtenidos";


    private static final String intIdBeneficiario = "idBeneficiario";
    private static final String intIdActualizacion = "id";
    private static final String vchCURP = "curp";
    private static final String vchRFC = "rfc";
    private static final String vchNombre = "nombre";
    private static final String vchPaterno = "paterno";
    private static final String vchMaterno = "materno";
    private static final String vchCalle = "calle";
    private static final String vchNumExt = "numExt";
    private static final String vchNumInt = "numInt";
    private static final String vchColonia = "colonia";
    private static final String intEdad = "edad";
    private static final String vchSexo = "sexo";
    private static final String vchTelefono = "telefono";
    private static final String fltMetrosCasa = "metrosCasa";
    private static final String vchColor = "color";
    private static final String vchLatitud = "latitud";
    private static final String vchLongitud = "longitud";
    private static final String vchFotoIdentificacionFrente = "fotoIdentificacionFrente";
    private static final String vchFotoIdentificacionReverso = "fotoIdentificacionReverso";
    private static final String vchFotoComprobante = "fotoComprobante";
    private static final String vchFotoAnuenciaTrabajo = "fotoAnuenciaTrabajo";
    private static final String vchFotoFachadaAntes = "fotoFachadaAntes";
    private static final String vchFotoFachadaDespues = "fotoFachadaDespues";
    private static final String vchFotoConfirmidad = "fotoConformidad";
    private static final String vchFactura = "factura";
    private static final String vchFotoFirma = "fotoFirma";

    private static final String CREATE_TABLE_BENEFICIARIO_NUEVO = "CREATE TABLE " + TABLE_BENEFICIARIO_NUEVO + "( " +
            intIdBeneficiario + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            vchCURP + " VARCHAR(18) NOT NULL, " +
            vchRFC + " VARCHAR(13), " +
            vchNombre + " VARCHAR(50) NOT NULL, " +
            vchPaterno + " VARCHAR(50) NOT NULL, " +
            vchMaterno + " VARCHAR(50) NOT NULL, " +
            vchCalle + " VARCHAR(70) NOT NULL, " +
            vchNumExt + " VARCHAR(50) NOT NULL, " +
            vchNumInt + " VARCHAR(50), " +
            vchColonia + " VARCHAR(70) NOT NULL, " +
            intEdad + " INT, " +
            vchSexo + " VARCHAR(1), " +
            vchTelefono + " VARCHAR(50), " +
            fltMetrosCasa + " FLOAT, " +
            vchColor + " VARCHAR(50), " +
            vchLatitud + " VARCHAR(50) NOT NULL, " +
            vchLongitud + " VARCHAR(50) NOT NULL, " +
            vchFotoIdentificacionFrente + " VARCHAR(250), " +
            vchFotoIdentificacionReverso + " VARCHAR(250), " +
            vchFotoComprobante + " VARCHAR(250), " +
            vchFotoAnuenciaTrabajo + " VARCHAR(250), " +
            vchFotoFachadaAntes + " VARCHAR(250), " +
            vchFotoFachadaDespues + " VARCHAR(250), " +
            vchFotoConfirmidad + " VARCHAR(250), " +
            statusEnvio + " VARCHAR(50) " +
            ")";

    private static final String CREATE_TABLE_BENEFICIARIO_OBTENIDOS = "CREATE TABLE " + TABLE_BENEFICIARIO_OBTENIDOS + "( " +
            intIdBeneficiario + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            intIdActualizacion + " INT, " +
            vchCURP + " VARCHAR(18) NOT NULL, " +
            vchRFC + " VARCHAR(13), " +
            vchNombre + " VARCHAR(50) NOT NULL, " +
            vchPaterno + " VARCHAR(50) NOT NULL, " +
            vchMaterno + " VARCHAR(50) NOT NULL, " +
            vchCalle + " VARCHAR(70) NOT NULL, " +
            vchNumExt + " VARCHAR(50) NOT NULL, " +
            vchNumInt + " VARCHAR(50), " +
            vchColonia + " VARCHAR(70) NOT NULL, " +
            intEdad + " INT, " +
            vchSexo + " VARCHAR(1), " +
            vchTelefono + " VARCHAR(50), " +
            fltMetrosCasa + " FLOAT, " +
            vchColor + " VARCHAR(50), " +
            vchLatitud + " VARCHAR(50) NOT NULL, " +
            vchLongitud + " VARCHAR(50) NOT NULL, " +
            vchFotoIdentificacionFrente + " VARCHAR(250), " +
            vchFotoIdentificacionReverso + " VARCHAR(250), " +
            vchFotoComprobante + " VARCHAR(250), " +
            vchFotoAnuenciaTrabajo + " VARCHAR(250), " +
            vchFotoFachadaAntes + " VARCHAR(250), " +
            vchFotoFachadaDespues + " VARCHAR(250), " +
            vchFotoConfirmidad + " VARCHAR(250), " +
            vchFactura + " VARCHAR(250), " +
            statusEnvio + " VARCHAR(50) " +
            ")";


    private static final String CREATE_TABLE_NAME = "CREATE TABLE " + TABLE_NAME + "( " +
            COLUMN_ID_LECTURAS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ID_ENDPOINTS + " INTEGER, " +
            IntIdPadron + " VARCHAR(36) NOT NULL, " +
            intidPaquete + " INT, " +
            intidSucursal + " INT, " +
            VchNombreSector + " VARCHAR(50) NOT NULL, " +
            VchNombreRuta + " VARCHAR(50) NOT NULL, " +
            SitNumSecuenciaRuta + " INT NOT NULL, " +
            NumContrato + " VARCHAR(50), " +
            VchDetalleDireccion + " VARCHAR(50), " +
            SitConsumoMes + " INT, " +
            SitConsumoPromedio + " INT, " +
            LecturaAnterior + " INT, " +
            vchNumMedidor + " VARCHAR(30), " +
            LecturaActual + " INT, " +
            ClaveLectura + " VARCHAR(50), " +
            vchNombreLecturista + " VARCHAR(50), " +
            sdtfechaRecepcion + " DATETIME, " +
            sdtfechaActualizacion + " DATETIME, " +
            vchLatitud + " VARCHAR, " +
            vchLongitud + " VARCHAR(50), " +
            rutaFotografiaClave + " VARCHAR, " +
            advertencia + " VARCHAR, " +
            status + " VARCHAR, " +
            statusEnvio + " VARCHAR, " +
            rutaFotografiaAdvertencia + " VARCHAR, " +
            "FOREIGN KEY (" + COLUMN_ID_ENDPOINTS + ") REFERENCES " + TABLE_ENDPOINT + "(" + COLUMN_ID_ENDPOINT + ")" +
            ")";

    private static final String CREATE_TABLE_QUERY_ENDPOINT = "CREATE TABLE " + TABLE_ENDPOINT + "( " +
            COLUMN_ID_ENDPOINT + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            IntIdPadron + " VARCHAR(36) NOT NULL, " +
            intidPaquete + " INT, " +
            intidSucursal + " INT, " +
            VchNombreSector + " VARCHAR NOT NULL, " +
            VchNombreRuta + " VARCHAR NOT NULL, " +
            SitNumSecuenciaRuta + " INT NOT NULL, " +
            NumContrato + " VARCHAR(50), " +
            VchDetalleDireccion + " VARCHAR(50), " +
            SitConsumoMes + " INT, " +
            SitConsumoPromedio + " INT, " +
            LecturaAnterior + " INT, " +
            vchNumMedidor + " VARCHAR(30), " +
            LecturaActual + " INT, " +
            ClaveLectura + " VARCHAR(50), " +
            vchNombreLecturista + " VARCHAR(50), " +
            sdtfechaRecepcion + " DATETIME, " +
            sdtfechaActualizacion + " DATETIME, " +
            vchLatitud + " VARCHAR(50), " +
            vchLongitud + " VARCHAR(50), " +
            rutaFotografiaClave + " VARCHAR, " +
            advertencia + " VARCHAR, " +
            status + " VARCHAR, " +
            statusEnvio + " VARCHAR, " +
            rutaFotografiaAdvertencia + " VARCHAR " +
            ")";

    private static final String CREATE_TABLE_QUERY_OPCIONESCLAVE = "CREATE TABLE " + TABLE_OPCIONESCLAVE + "( " +
            COLUMN_ID_OPCIONESCLAVE + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            numerodeclave + " INT, " +
            vchnumClaveLectura + " VARCHAR, " +
            identificador + " VARCHAR, " +
            aceptaLectura + " INT" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_BENEFICIARIO_NUEVO);
        db.execSQL(CREATE_TABLE_BENEFICIARIO_OBTENIDOS);


        db.execSQL(CREATE_TABLE_NAME);
        db.execSQL(CREATE_TABLE_QUERY_ENDPOINT);
        db.execSQL(CREATE_TABLE_QUERY_OPCIONESCLAVE);

        fetchDataFromServer();
        fetchIdClaveDescripcionFromServer();
    }

    public void actualizarStatusEnvioAGrupo(SQLiteDatabase db) {
        try {
            String query = "UPDATE " + TABLE_ENDPOINT + " SET " + statusEnvio + " = " +
                    "(CASE WHEN " + intidPaquete + " IN (SELECT " + intidPaquete +
                    " FROM " + TABLE_ENDPOINT + " GROUP BY " + intidPaquete +
                    " HAVING COUNT(*) > 1) THEN 'grupo' ELSE 'individual' END)";
            db.execSQL(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteDatabase(Context context) {
        try {
            context.deleteDatabase(DATABASE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public double getConsumoMes(String numContrato, String padron) {
        SQLiteDatabase db = this.getReadableDatabase();
        double consumoMes = 0;

        String query = "SELECT " + SitConsumoPromedio + " FROM " + TABLE_ENDPOINT + " WHERE " + NumContrato + " = ? AND "
                + IntIdPadron + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{numContrato, padron});

        if (cursor.moveToFirst()) {
            consumoMes = cursor.getDouble(cursor.getColumnIndexOrThrow(SitConsumoPromedio));
        }

        cursor.close();
        db.close();

        return consumoMes;
    }

    public double getecturaAnterior(String numContrato, String padron) {
        SQLiteDatabase db = this.getReadableDatabase();
        double consumoMes = 0;

        String query = "SELECT " + LecturaAnterior + " FROM " + TABLE_ENDPOINT + " WHERE " + NumContrato + " = ? AND " +
                IntIdPadron + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{numContrato, padron});

        if (cursor.moveToFirst()) {
            consumoMes = cursor.getDouble(cursor.getColumnIndexOrThrow(LecturaAnterior));
        }

        cursor.close();
        db.close();

        return consumoMes;
    }

    public boolean isDuplicateContract(String contrato, String padron) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + NumContrato + " = ? AND "
                + IntIdPadron + " = ?";
        String[] selectionArgs = {contrato, padron};
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.rawQuery(query, selectionArgs);
            exists = cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

    // Metodo para insertar un nuevo registro en la base de datos
    public long insertLectura(LecturaModel lectura) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID_ENDPOINTS, lectura.getCOLUMN_ID_ENDPOINTS());
        values.put(IntIdPadron, lectura.getIntIdPadron());
        values.put(intidPaquete, lectura.getIntIdPaquete());
        values.put(intidSucursal, lectura.getIntIdSucursal());
        values.put(VchNombreSector, lectura.getVchNombreSector());
        values.put(VchNombreRuta, lectura.getVchNombreRuta());
        values.put(SitNumSecuenciaRuta, lectura.getSitNumSecuenciaRuta());
        values.put(NumContrato, lectura.getNumContrato());
        values.put(VchDetalleDireccion, lectura.getVchDetalleDireccion());
        values.put(SitConsumoMes, lectura.getSitConsumoMes());
        values.put(SitConsumoPromedio, lectura.getSetSitConsumoPromedio());
        values.put(LecturaAnterior, lectura.getLecturaAnterior());
        values.put(vchNumMedidor, lectura.getVchNumMedidor());
        values.put(LecturaActual, lectura.getLecturaActual());
        values.put(ClaveLectura, lectura.getClaveLectura());
        values.put(vchNombreLecturista, lectura.getVchNombreLecturista());
        values.put(sdtfechaRecepcion, lectura.getSdtfechaRecepcion());
        values.put(sdtfechaActualizacion, lectura.getSdtfechaActualizacion());
        values.put(vchLatitud, lectura.getVchLatitud());
        values.put(vchLongitud, lectura.getVchLongitud());
        values.put(rutaFotografiaClave, lectura.getRutaFotografia());
        values.put(advertencia, lectura.getAdvertencia());
        values.put(status, lectura.getStatus());
        values.put(statusEnvio, lectura.getStatusEnvio());
        values.put(rutaFotografiaAdvertencia, lectura.getRutaFotografiaAdvertencia());

        long insertedRowId = -1;
        try {
            insertedRowId = db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return insertedRowId;
    }

    public LecturaEndpoint getEndpointByNumContrato(String numContrato, String padron) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        LecturaEndpoint lecturaEndpoint = null;

        try {
            String query = "SELECT * FROM " + TABLE_ENDPOINT + " WHERE " + NumContrato + " = ? AND " + IntIdPadron + " = ?";
            String[] selectionArgs = {numContrato, padron};
            cursor = db.rawQuery(query, selectionArgs);

            if (cursor.moveToFirst()) {
                lecturaEndpoint = new LecturaEndpoint();
                lecturaEndpoint.setIntIdPadron(String.valueOf(cursor.getColumnIndexOrThrow(IntIdPadron)));
                lecturaEndpoint.setIntidPaquete(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidPaquete))));
                lecturaEndpoint.setIntidSucursal(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidSucursal))));
                lecturaEndpoint.setVchNombreSector(cursor.getString(cursor.getColumnIndexOrThrow(VchNombreSector)));
                lecturaEndpoint.setVchNombreRuta(cursor.getString(cursor.getColumnIndexOrThrow(VchNombreRuta)));
                lecturaEndpoint.setSitNumSecuenciaRuta(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitNumSecuenciaRuta))));
                lecturaEndpoint.setNumContrato(cursor.getString(cursor.getColumnIndexOrThrow(NumContrato)));
                lecturaEndpoint.setVchDetalleDireccion(cursor.getString(cursor.getColumnIndexOrThrow(VchDetalleDireccion)));
                lecturaEndpoint.setSitConsumoMes(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoMes))));
                lecturaEndpoint.setSitConsumoPromedio(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoPromedio))));
                lecturaEndpoint.setLecturaAnterior(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaAnterior))));
                lecturaEndpoint.setVchNumMedidor(cursor.getString(cursor.getColumnIndexOrThrow(vchNumMedidor)));
                lecturaEndpoint.setLecturaActual(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaActual))));
                lecturaEndpoint.setClaveLectura(cursor.getString(cursor.getColumnIndexOrThrow(ClaveLectura)));
                lecturaEndpoint.setVchNombreLecturista(cursor.getString(cursor.getColumnIndexOrThrow(vchNombreLecturista)));
                lecturaEndpoint.setSdtfechaRecepcion(cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion)));
                lecturaEndpoint.setSdtfechaActualizacion(cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaActualizacion)));
                lecturaEndpoint.setVchLatitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLatitud)));
                lecturaEndpoint.setVchLongitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLongitud)));
                lecturaEndpoint.setRutaFotografiaClave(cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaClave)));
                lecturaEndpoint.setAdvertencia(cursor.getString(cursor.getColumnIndexOrThrow(advertencia)));
                lecturaEndpoint.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(status)));
                lecturaEndpoint.setStatusEnvio(cursor.getString(cursor.getColumnIndexOrThrow(statusEnvio)));
                lecturaEndpoint.setRutaFotografiaAdvertencia(cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaAdvertencia)));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            // Cierra la base de datos solo si está abierta
            if (db.isOpen()) {
                db.close();
            }
        }

        return lecturaEndpoint;
    }


    public LecturaModel getLecturaByNumContrato(String numContrato, String padron) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        LecturaModel lectura = null;

        try {
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + NumContrato + "= ? AND " + IntIdPadron + " = ?";
            cursor = db.rawQuery(query, new String[]{numContrato, padron});

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_ENDPOINTS));
                String setIntIdPadron = cursor.getString(cursor.getColumnIndexOrThrow(IntIdPadron));
                String setIntidPaquete = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidPaquete)));
                String setIntidSucursal = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidSucursal)));
                String setVchNombreSector = cursor.getString(cursor.getColumnIndexOrThrow(VchNombreSector));
                String setVchNombreRuta = cursor.getString(cursor.getColumnIndexOrThrow(VchNombreRuta));
                String setSitNumSecuenciaRuta = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitNumSecuenciaRuta)));
                String setNumContrato = cursor.getString(cursor.getColumnIndexOrThrow(NumContrato));
                String setVchDetalleDireccion = cursor.getString(cursor.getColumnIndexOrThrow(VchDetalleDireccion));
                String setSitConsumoMes = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoMes)));
                String setSitConsumoPromedio = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoPromedio)));
                String setLecturaAnterior = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaAnterior)));
                String setVchNumMedidor = cursor.getString(cursor.getColumnIndexOrThrow(vchNumMedidor));
                String setLecturaActual = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaActual)));
                String setClaveLectura = cursor.getString(cursor.getColumnIndexOrThrow(ClaveLectura));
                String setVchNombreLecturista = cursor.getString(cursor.getColumnIndexOrThrow(vchNombreLecturista));
                String setSdtfechaRecepcion = cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion));
                String setSdtfechaActualizacion = cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaActualizacion));
                String setVchLatitud = cursor.getString(cursor.getColumnIndexOrThrow(vchLatitud));
                String setVchLongitud = cursor.getString(cursor.getColumnIndexOrThrow(vchLongitud));
                String setRutaFotografiaClave = cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaClave));
                String setAdvertencia = cursor.getString(cursor.getColumnIndexOrThrow(advertencia));
                String setStatus = cursor.getString(cursor.getColumnIndexOrThrow(status));
                String setStatusEnvio = cursor.getString(cursor.getColumnIndexOrThrow(statusEnvio));
                String setRutaFotografiaAdvertencia = cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaAdvertencia));
                // valores de las columnas aquí (como lo hiciste en el método insertLectura)

                // instancia de LecturaModel con los valores obtenidos y devuélvela
                lectura = new LecturaModel(id, setIntIdPadron, setIntidPaquete, setIntidSucursal, setVchNombreSector,
                        setVchNombreRuta, setSitNumSecuenciaRuta, setNumContrato, setVchDetalleDireccion, setSitConsumoMes,
                        setSitConsumoPromedio, setLecturaAnterior, setVchNumMedidor,
                        setLecturaActual, setClaveLectura, setVchNombreLecturista, setSdtfechaRecepcion, setSdtfechaActualizacion,
                        setVchLatitud, setVchLongitud, setRutaFotografiaClave, setAdvertencia, setStatus, setStatusEnvio, setRutaFotografiaAdvertencia);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return lectura;
    }

    public ArrayList<OpcionesClaveData> getAllOpcionesClaves() {
        ArrayList<OpcionesClaveData> opcionesClaveList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_OPCIONESCLAVE, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_OPCIONESCLAVE));
                String numeroDeClave = cursor.getString(cursor.getColumnIndexOrThrow(vchnumClaveLectura));

                String Identificador = cursor.getString(cursor.getColumnIndexOrThrow(identificador));
                int AceptaLectura = cursor.getColumnIndexOrThrow(aceptaLectura);

                OpcionesClaveData opcionesClaveData = new OpcionesClaveData(id, numeroDeClave, Identificador, AceptaLectura);
                opcionesClaveList.add(opcionesClaveData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return opcionesClaveList;
    }

    public ArrayList<ListDataEntradas> getAllLecturasEndpoint() {
        ArrayList<ListDataEntradas> lecturasEndpointList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_ENDPOINT;
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String sector = cursor.getString(cursor.getColumnIndexOrThrow(VchNombreSector));
                    String ruta = cursor.getString(cursor.getColumnIndexOrThrow(VchNombreRuta));
                    String secuencia = cursor.getString(cursor.getColumnIndexOrThrow(SitNumSecuenciaRuta));
                    String contrato = cursor.getString(cursor.getColumnIndexOrThrow(NumContrato));
                    String domicilio = cursor.getString(cursor.getColumnIndexOrThrow(VchDetalleDireccion));
                    String numeroMedidor = cursor.getString(cursor.getColumnIndexOrThrow(vchNumMedidor));
                    String anterior = cursor.getString(cursor.getColumnIndexOrThrow(LecturaAnterior));
                    String padron = cursor.getString(cursor.getColumnIndexOrThrow(IntIdPadron));
                    String fechaRecepcion = cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion));
                    String colonia = cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion));

                    ListDataEntradas lecturaEndpointData = new ListDataEntradas(
                            sector, ruta, secuencia, contrato, domicilio,
                            numeroMedidor, anterior, padron, fechaRecepcion, colonia
                    );
                    lecturasEndpointList.add(lecturaEndpointData);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return lecturasEndpointList;
    }

    public ArrayList<ListDataEnviados> getAllLecturasEndpointStatusEnviados() {
        ArrayList<ListDataEnviados> lecturasEndpointList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + status + " = 'exitoso'";
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String sector = cursor.getString(cursor.getColumnIndexOrThrow(VchNombreSector));
                    String ruta = cursor.getString(cursor.getColumnIndexOrThrow(VchNombreRuta));
                    String secuencia = cursor.getString(cursor.getColumnIndexOrThrow(SitNumSecuenciaRuta));
                    String contrato = cursor.getString(cursor.getColumnIndexOrThrow(NumContrato));
                    String domicilio = cursor.getString(cursor.getColumnIndexOrThrow(VchDetalleDireccion));
                    String numeroMedidor = cursor.getString(cursor.getColumnIndexOrThrow(vchNumMedidor));
                    String anterior = cursor.getString(cursor.getColumnIndexOrThrow(LecturaAnterior));
                    String Advertencia = cursor.getString(cursor.getColumnIndexOrThrow(advertencia));
                    String fechaRecepcion = cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion));

                    ListDataEnviados lecturaEndpointData = new ListDataEnviados(
                            sector, ruta, secuencia, contrato, domicilio,
                            numeroMedidor, anterior, Advertencia, fechaRecepcion
                    );
                    lecturasEndpointList.add(lecturaEndpointData);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return lecturasEndpointList;
    }


    public String getintidPaquete(String contrato, String padron) {
        String IntidPaquete = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + intidPaquete + " FROM " + TABLE_ENDPOINT
                + " WHERE " + NumContrato + " = ? AND " + IntIdPadron + " = ?";
        String[] selectionArgs = {String.valueOf(contrato), padron};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            IntidPaquete = cursor.getString(cursor.getColumnIndexOrThrow(intidPaquete));
            cursor.close();
        }
        return IntidPaquete;
    }

    public String getintidSucursal(String contrato, String padron) {
        String IntidSucursal = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + intidSucursal + " FROM " + TABLE_ENDPOINT
                + " WHERE " + NumContrato + " = ? AND " + IntIdPadron + " = ?";
        String[] selectionArgs = {String.valueOf(contrato), padron};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            IntidSucursal = cursor.getString(cursor.getColumnIndexOrThrow(intidSucursal));
            cursor.close();
        }
        return IntidSucursal;
    }

    public String getSitConsumoMes(String contrato, String padron) {
        String sitConsumoMes = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + SitConsumoMes + " FROM " + TABLE_ENDPOINT
                + " WHERE " + NumContrato + " = ? AND " + IntIdPadron + " = ?";
        String[] selectionArgs = {String.valueOf(contrato), padron};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            sitConsumoMes = cursor.getString(cursor.getColumnIndexOrThrow(SitConsumoMes));
            cursor.close();
        }
        return sitConsumoMes;
    }

    public String getSitConsumoPromedio(String contrato, String padron) {
        String sitConsumoPromedio = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + SitConsumoPromedio + " FROM " + TABLE_ENDPOINT
                + " WHERE " + NumContrato + " = ? AND " + IntIdPadron + " = ?";
        String[] selectionArgs = {String.valueOf(contrato), padron};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            sitConsumoPromedio = cursor.getString(cursor.getColumnIndexOrThrow(SitConsumoPromedio));
            cursor.close();
        }
        return sitConsumoPromedio;
    }

    public String getvchNombreLecturista(String contrato, String padron) {
        String VchNombreLecturista = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + vchNombreLecturista + " FROM " + TABLE_ENDPOINT
                + " WHERE " + NumContrato + " = ? AND " + IntIdPadron + " = ?";
        String[] selectionArgs = {String.valueOf(contrato), padron};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            VchNombreLecturista = cursor.getString(cursor.getColumnIndexOrThrow(vchNombreLecturista));
            cursor.close();
        }
        return VchNombreLecturista;
    }

    public String getFechaRecepcionFromEndpointTable() {
        String fechaRecepcion = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ENDPOINT;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            fechaRecepcion = cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion));
            cursor.close();
        }
        return fechaRecepcion;
    }

    public String getStatusEnvioFromEndpointTable(String contrato, String padron) {
        String StatusEnvio = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + statusEnvio + " FROM " + TABLE_ENDPOINT
                + " WHERE " + NumContrato + " = ? AND " + IntIdPadron + " = ?";
        String[] selectionArgs = {contrato, padron};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            StatusEnvio = cursor.getString(cursor.getColumnIndexOrThrow(statusEnvio));
            cursor.close();
        }
        return StatusEnvio;
    }


    public int getEndpointIdByNumContrato(String contrato, String padron) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID_ENDPOINT + " FROM " + TABLE_ENDPOINT +
                " WHERE " + NumContrato + " =? AND " + IntIdPadron + " = ?";
        String[] selectionArgs = {contrato, padron};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        int idEndpoint = -1;
        if (cursor.moveToFirst()) {
            idEndpoint = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_ENDPOINT));

        }
        cursor.close();
        db.close();

        return idEndpoint;
    }

    public void deleteRegistroEndpoint(String numContrato, String padron) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String whereClause = NumContrato + " = ? AND " + IntIdPadron + " = ?";
            String[] whereArgs = new String[]{numContrato, padron};
            db.delete(TABLE_ENDPOINT, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public String getNumeroDeClave(String claveLectura) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String numeroDeClave = null;

        try {
            db = this.getReadableDatabase();

            // Realiza una consulta a la tabla lecturas_opcionesclave para obtener el valor de numero_de_clave
            cursor = db.rawQuery("SELECT numero_de_clave FROM lecturas_opcionesclave WHERE vch_num_clave_lectura = ?", new String[]{claveLectura});

            if (cursor.moveToFirst()) {
                numeroDeClave = cursor.getString(cursor.getColumnIndexOrThrow("numero_de_clave"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar cursor y base de datos después de su uso
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return numeroDeClave;
    }

    private JSONObject lecturaToJson(LecturaModel lectura) {
        JSONObject jsonObject = new JSONObject();
        try {

            String claveLectura = lectura.getClaveLectura();
            String numeroDeClave = getNumeroDeClave(claveLectura);

            jsonObject.put("intIdPaquete", lectura.getIntIdPaquete());
            jsonObject.put("intIdPadron", lectura.getIntIdPadron());
            jsonObject.put("intLecturaActual", lectura.getLecturaActual());
            jsonObject.put("intIdClaveLectura", numeroDeClave);
            jsonObject.put("sdtfechaRecepcion", lectura.getSdtfechaRecepcion());
            jsonObject.put("sdtfechaActualizacion", lectura.getSdtfechaActualizacion());
            jsonObject.put("vchLatitud", lectura.getVchLatitud());
            jsonObject.put("vchLongitud", lectura.getVchLongitud());

            if (lectura.getRutaFotografia() != null && !lectura.getRutaFotografia().isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(lectura.getRutaFotografia());
                byte[] imageBytes = convertBitmapToBytes(bitmap);
                jsonObject.put("vchFotografia", convertBytesToBase64(imageBytes));
            } else {
                jsonObject.put("vchFotografia", lectura.getRutaFotografia());
            }

            if (lectura.getRutaFotografiaAdvertencia() != null && !lectura.getRutaFotografiaAdvertencia().isEmpty()) {
                Bitmap bitmapAlerta = BitmapFactory.decodeFile(lectura.getRutaFotografiaAdvertencia());
                byte[] imageBytesAlerta = convertBitmapToBytesAlerta(bitmapAlerta);
                jsonObject.put("vchFotografiaAlerta", convertBytesToBase64Alerta(imageBytesAlerta));
            } else {
                jsonObject.put("vchFotografiaAlerta", lectura.getRutaFotografiaAdvertencia());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private byte[] convertBitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] convertBitmapToBytesAlerta(Bitmap bitmapAlerta) {
        ByteArrayOutputStream byteArrayOutputStreamAlerta = new ByteArrayOutputStream();
        bitmapAlerta.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStreamAlerta);
        return byteArrayOutputStreamAlerta.toByteArray();
    }

    private String convertBytesToBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private String convertBytesToBase64Alerta(byte[] bytesAlerta) {
        return Base64.encodeToString(bytesAlerta, Base64.DEFAULT);
    }


    public void sendDataToServer(final List<LecturaModel> lecturas) {
        for (final LecturaModel lectura : lecturas) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: adentro");
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(1250, TimeUnit.SECONDS)
                            .readTimeout(1250, TimeUnit.SECONDS)
                            .build();

                    JSONObject jsonObject = lecturaToJson(lectura);

                    // Convierte el JSONObject en una cadena JSON
                    String jsonBody = jsonObject.toString();
                    // Obtener el token de las preferencias compartidas
                    SharedPreferences preferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    String authToken = preferences.getString(KEY_TOKEN, "");
                    String url = "http://201.147.15.182/ApiLecturas/api/v1/GuardarRutas/" + authToken;

                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();

                    // Realiza la solicitud HTTP
                    try (okhttp3.Response response = client.newCall(request).execute()) {
                        int statusCode = response.code();
                        Log.d(TAG, "sendSingleDataToServer onResponse: " + statusCode);

                        if (statusCode == 200) {
                            SQLiteDatabase database = getWritableDatabase();
                            updateStatusToExitoso(database, lecturas);
                        } else {
                            try {
                                String errorBody = response.body() != null ? response.body().string() : "Error body is null";
                                Log.e(TAG, "sendSingleDataToServer Error response body: " + errorBody);

                                JSONObject errorResponse = new JSONObject(errorBody);
                                String errorTitle = errorResponse.optString("Error");
                                String errorMessage = errorResponse.optString("Message", "");

                                if (databaseCallback != null) {
                                    databaseCallback.onDatabaseError("Error al realizar el envío al servidor con el Padron: " + lectura.getIntIdPadron() + ". " + errorBody + ". Código de error: " + statusCode);
                                }
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                                // Manejar errores de parsing o lectura
                                if (databaseCallback != null) {
                                    databaseCallback.onDatabaseError("Error al realizar el envío de los datos al servidor . Código de error: " + statusCode + " la descripción: " + e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (databaseCallback != null) {
                            Log.d(TAG, "onResponse: error al realizar la solicitud");
                            databaseCallback.onDatabaseError("Error al realizar el envio de los datos al servidor: " + e.getMessage() + " Reinicia la conexión e intentalo más tarde.");
                        }
                    }
                }
            });
        }
    }

    public void updateStatusToExitoso(SQLiteDatabase database, List<LecturaModel> lecturas) {
        lecturas.forEach(lectura -> {
            if ("enviando".equals(lectura.getStatus())) {
                Log.d(TAG, "updateStatusToExitoso: " + lectura.getNumContrato());
                lectura.actualizarStatus(database, "exitoso");
            }
        });
    }

    public ArrayList<LecturaModel> getPendingDataFromEndpoint() {
        ArrayList<LecturaModel> pendingDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + status + " = 'enviando'";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    // Lee los datos de la fila y crea un objeto LecturaModel
                    LecturaModel lectura = new LecturaModel();
                    // Llena los atributos de lectura desde el cursor (ajusta esto según tu estructura)
                    lectura.setIntIdPadron(cursor.getString(cursor.getColumnIndexOrThrow(IntIdPadron)));
                    lectura.setIntIdPaquete(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidPaquete))));
                    lectura.setIntIdSucursal(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidSucursal))));
                    lectura.setVchNombreSector(cursor.getString(cursor.getColumnIndexOrThrow(VchNombreSector)));
                    lectura.setVchNombreRuta(cursor.getString(cursor.getColumnIndexOrThrow(VchNombreRuta)));
                    lectura.setSitNumSecuenciaRuta(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitNumSecuenciaRuta))));
                    lectura.setNumContrato(cursor.getString(cursor.getColumnIndexOrThrow(NumContrato)));
                    lectura.setVchDetalleDireccion(cursor.getString(cursor.getColumnIndexOrThrow(VchDetalleDireccion)));
                    lectura.setSitConsumoMes(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoMes))));
                    lectura.setSetSitConsumoPromedio(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoPromedio))));
                    lectura.setLecturaAnterior(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaAnterior))));
                    lectura.setVchNumMedidor(cursor.getString(cursor.getColumnIndexOrThrow(vchNumMedidor)));
                    lectura.setLecturaActual(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaActual))));
                    lectura.setClaveLectura(cursor.getString(cursor.getColumnIndexOrThrow(ClaveLectura)));
                    lectura.setVchNombreLecturista(cursor.getString(cursor.getColumnIndexOrThrow(vchNombreLecturista)));
                    lectura.setSdtfechaRecepcion(cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion)));
                    lectura.setSdtfechaActualizacion(cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaActualizacion)));
                    lectura.setVchLatitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLatitud)));
                    lectura.setVchLongitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLongitud)));
                    lectura.setRutaFotografia(cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaClave)));
                    lectura.setAdvertencia(cursor.getString(cursor.getColumnIndexOrThrow(advertencia)));
                    lectura.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(status)));
                    lectura.setStatusEnvio(cursor.getString(cursor.getColumnIndexOrThrow(statusEnvio)));
                    lectura.setRutaFotografiaAdvertencia(cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaAdvertencia)));
                    pendingDataList.add(lectura);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return pendingDataList;
    }

    public ArrayList<LecturaModel> getPendingDataFromEndpoint1(int intIdPaquete) {
        ArrayList<LecturaModel> pendingDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + intidPaquete + " =? ";
            String[] selectionArgs = {String.valueOf(intIdPaquete)};
            Cursor cursor = db.rawQuery(query, selectionArgs);

            if (cursor.moveToFirst()) {
                do {
                    // Lee los datos de la fila y crea un objeto LecturaModel
                    LecturaModel lectura = new LecturaModel();
                    // Llena los atributos de lectura desde el cursor (ajusta esto según tu estructura)
                    lectura.setIntIdPadron(String.valueOf(cursor.getColumnIndexOrThrow(IntIdPadron)));
                    lectura.setIntIdPaquete(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidPaquete))));
                    lectura.setIntIdSucursal(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(intidSucursal))));
                    lectura.setVchNombreSector(cursor.getString(cursor.getColumnIndexOrThrow(VchNombreSector)));
                    lectura.setVchNombreRuta(cursor.getString(cursor.getColumnIndexOrThrow(VchNombreRuta)));
                    lectura.setSitNumSecuenciaRuta(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitNumSecuenciaRuta))));
                    lectura.setNumContrato(cursor.getString(cursor.getColumnIndexOrThrow(NumContrato)));
                    lectura.setVchDetalleDireccion(cursor.getString(cursor.getColumnIndexOrThrow(VchDetalleDireccion)));
                    lectura.setSitConsumoMes(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoMes))));
                    lectura.setSetSitConsumoPromedio(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(SitConsumoPromedio))));
                    lectura.setLecturaAnterior(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaAnterior))));
                    lectura.setVchNumMedidor(cursor.getString(cursor.getColumnIndexOrThrow(vchNumMedidor)));
                    lectura.setLecturaActual(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(LecturaActual))));
                    lectura.setClaveLectura(cursor.getString(cursor.getColumnIndexOrThrow(ClaveLectura)));
                    lectura.setVchNombreLecturista(cursor.getString(cursor.getColumnIndexOrThrow(vchNombreLecturista)));
                    lectura.setSdtfechaRecepcion(cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaRecepcion)));
                    lectura.setSdtfechaActualizacion(cursor.getString(cursor.getColumnIndexOrThrow(sdtfechaActualizacion)));
                    lectura.setVchLatitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLatitud)));
                    lectura.setVchLongitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLongitud)));
                    lectura.setRutaFotografia(cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaClave)));
                    lectura.setAdvertencia(cursor.getString(cursor.getColumnIndexOrThrow(advertencia)));
                    lectura.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(status)));
                    lectura.setStatusEnvio(cursor.getString(cursor.getColumnIndexOrThrow(statusEnvio)));
                    lectura.setRutaFotografiaAdvertencia(cursor.getString(cursor.getColumnIndexOrThrow(rutaFotografiaAdvertencia)));
                    pendingDataList.add(lectura);
                } while (cursor.moveToNext());
            }


            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return pendingDataList;
    }

    public void fetchDataFromServer() {
        // Obtener el token de las preferencias compartidas
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String authToken = preferences.getString(KEY_TOKEN, "");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        retrofit2.Call<ResponseBody> call = apiService.getRutas(authToken);

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: fue exitoso.");
                    try {
                        String responseData = response.body().string();
                        saveDataToDatabase(responseData);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (databaseCallback != null) {
                            Log.d(TAG, "onResponse: error al realizar la solicitud");
                            databaseCallback.onDatabaseError("Error al realizar la solicitud al servidor para el envio de datos al server. Codigo de error: " + response.code() + " y descripción: " + e.getMessage());
                        }
                    }
                } else {
                    Log.d(TAG, "onResponse: Respuesta no exitosa: " + response.code());
                    if (databaseCallback != null) {
                        Log.d(TAG, "onResponse: error al realizar la solicitud");
                        databaseCallback.onDatabaseError("Error al realizar la solicitud al servidor para el envio de datos al server. Codigo de error: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: entra al fallo: " + t);
                if (databaseCallback != null) {
                    Log.d(TAG, "onResponse: error al realizar la solicitud");
                    databaseCallback.onDatabaseError("Error al realizar el envio de los datos al servidor con la descripcion: " + t.getMessage() + ".  Reinicia la conexión e intentalo más tarde.");
                }
            }
        });
    }

    public void saveDataToDatabase(String jsonData) {
        try {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            String SdtfechaRecepcion = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + " " +
                    String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);

            JSONArray jsonArray = new JSONArray(jsonData);
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();

            // Iterar sobre los registros locales y eliminar los que no existen en el servidor
            Cursor localCursor = db.rawQuery("SELECT * FROM lecturas_endpoint", null);
            if (localCursor.moveToFirst()) {
                do {
                    String localPadron = localCursor.getString(localCursor.getColumnIndexOrThrow(IntIdPadron));
                    String localPaquete = localCursor.getString(localCursor.getColumnIndexOrThrow(intidPaquete));

                    boolean recordExistsInServer = false;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String serverPadron = jsonObject.getString("intIdPadron");
                        String serverPaquete = jsonObject.getString("intIdPaquete");

                        if (localPadron.equals(serverPadron) && localPaquete.equals(serverPaquete)) {
                            recordExistsInServer = true;
                            break;
                        }
                    }

                    // Si el registro local no existe en el servidor, eliminarlo
                    if (!recordExistsInServer) {
                        db.delete("lecturas_endpoint", IntIdPadron + " = ? AND " + intidPaquete + " = ?", new String[]{localPadron, localPaquete});
                    }
                } while (localCursor.moveToNext());
            }
            localCursor.close();

            // Continuar con la inserción o actualización de nuevos registros
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Obtener el IntIdPadron del objeto actual
                String padron = jsonObject.getString("intIdPadron");
                String paquete = jsonObject.getString("intIdPaquete");
                if (!isRecordExist(db, padron, paquete)) {
                    ContentValues values = new ContentValues();
                    values.put(intidPaquete, jsonObject.getString("intIdPaquete"));
                    values.put(IntIdPadron, jsonObject.getString("intIdPadron"));
                    values.put(intidSucursal, jsonObject.getString("intidSucursal"));
                    values.put(VchNombreSector, jsonObject.getString("vchNombreSector"));
                    values.put(VchNombreRuta, jsonObject.getString("vchNombreRuta"));
                    values.put(SitNumSecuenciaRuta, jsonObject.getString("sitNumSecuenciaRuta"));
                    values.put(NumContrato, jsonObject.getString("vchNumContrato"));
                    values.put(VchDetalleDireccion, jsonObject.getString("vchDetalleDireccion"));
                    values.put(SitConsumoMes, jsonObject.getString("sitConsumoMes"));
                    values.put(SitConsumoPromedio, jsonObject.getString("sitConsumoPromedio"));
                    values.put(LecturaAnterior, jsonObject.getString("intLecturaAnterior"));
                    values.put(vchNumMedidor, jsonObject.getString("vchNumMedidor"));
                    values.put(LecturaActual, jsonObject.getString("intLecturaActual"));
                    values.put(ClaveLectura, jsonObject.getString("intIdClaveLectura"));
                    values.put(sdtfechaRecepcion, SdtfechaRecepcion);
                    values.put(sdtfechaActualizacion, jsonObject.getString("sdtfechaActualizacion"));
                    values.put(vchLatitud, jsonObject.getString("vchLongitud"));
                    values.put(vchLongitud, jsonObject.getString("vchLongitud"));
                    values.put(status, "pendiente");
                    values.put(statusEnvio, "");

                    db.insert("lecturas_endpoint", null, values);
                } else {
                    Log.d(TAG, "saveDataToDatabase: Registro duplicado para IntIdPadron: " + padron + " " + paquete);
                }
            }
            actualizarStatusEnvioAGrupo(db);

            ArrayList<ListDataEntradas> updateData = getAllLecturasEndpoint();
            if (dataUpdateListener != null) {
                dataUpdateListener.onDataUpdated(updateData);
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            // Notificar al usuario que los datos se han guardado con éxito
        } catch (JSONException e) {
            e.printStackTrace();

            if (databaseCallback != null) {
                Log.d(TAG, "onResponse: error al realizar la solicitud");
                databaseCallback.onDatabaseError("Error al realizar el envio de los datos al servidor con la descripcion: " + e.getMessage() + ". Reinicia la conexión e intentalo más tarde.");
            }
        }
    }

    public void fetchIdClaveDescripcionFromServer() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(800, TimeUnit.SECONDS)
                .readTimeout(800, TimeUnit.SECONDS)
                .build();
        // Obtener el token de las preferencias compartidas
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String authToken = preferences.getString(KEY_TOKEN, "");
        Log.d(TAG, "fetchDataFromServer: " + authToken);

        String url = "http://jmaschihuahua.com/ApiLecturas/api/v1/VerClaveLectura/" + authToken;

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Realiza la solicitud HTTP de manera asincrónica
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG, "onFailure: Error en la solicitud: " + e);

                if (databaseCallback != null) {
                    Log.d(TAG, "onResponse: error al realizar la solicitud");
                    databaseCallback.onDatabaseError("Error al realizar el envio de los datos al servidor con la descripcion: " + e + ". Reinicia la conexión e intentalo más tarde.");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: respuesta correcta");
                    String responseData = response.body().string();
                    Log.d(TAG, "onResponse: Datos recibidos: " + responseData);
                    processIdClaveDescripcionData(responseData);

                } else {
                    // Manejo de errores en caso de respuesta no exitosa
                    Log.d(TAG, "onResponse: Respuesta no exitosa: " + response.code());
                    if (databaseCallback != null) {
                        Log.d(TAG, "onResponse: error al realizar la solicitud");
                        databaseCallback.onDatabaseError("Error al realizar la solicitud al servidor respecto claves incidencias. Codigo de error: " + response.code() + " y la descripcion " + response.body().string()) ;
                        dataUpdateListener.onDatabaseError("Error al realizar la solicitud al servidor respecto claves incidencias. Codigo de error: " + response.code() + " y la descripcion " + response.body().string()) ;
                    }
                }
            }
        });
    }

    private void processIdClaveDescripcionData(String responseData) {
        try {
            // Aquí debes parsear la respuesta JSON y extraer la idclave y la descripción
            JSONArray jsonArray = new JSONArray(responseData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String idClave = item.getString("intIdClaveLectura");
                String numClaveLectura = item.getString("vchNumClaveLectura");
                String descripcion = item.getString("vchNombreClaveLectura");
                int AceptaLectura = item.getInt("intAceptaLectura");
                // Puedes guardar estos datos en la base de datos o realizar cualquier otra acción necesaria
                saveIdClaveDescripcionToDatabase(idClave, numClaveLectura, descripcion, AceptaLectura);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            databaseCallback.onDatabaseError("Error al realizar el envio de los datos al servidor con la descripcion: " + e.getMessage() + ". Reinicia la conexión e intentalo más tarde.");
        }
    }

    private void saveIdClaveDescripcionToDatabase(String idClave, String vchNumClaveLectura, String descripcion, int AceptaLectura) {
        insertIdClaveDescripcion(idClave, vchNumClaveLectura, descripcion, AceptaLectura);
    }

    public void insertIdClaveDescripcion(String idClave, String vchNumClaveLectura, String descripcion, int AceptaLectura) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (!isIdClaveExists(db, idClave)) {
            ContentValues values = new ContentValues();

            values.put(numerodeclave, idClave);
            values.put(vchnumClaveLectura, vchNumClaveLectura);
            values.put(identificador, descripcion);
            values.put(aceptaLectura, AceptaLectura);

            long newRowId = db.insert(TABLE_OPCIONESCLAVE, null, values);

            if (newRowId != -1) {
                Log.d(TAG, "insertIdClaveDescripcion: Datos insertados correctamente");
            } else {
                Log.d(TAG, "insertIdClaveDescripcion: Error al insertar datos");
            }
        } else {
            Log.d(TAG, "insertIdClaveDescripcion: El registro con numerodeclave " + idClave + " ya existe");
        }
    }

    private boolean isIdClaveExists(SQLiteDatabase db, String idClave) {
        Cursor cursor = db.query(
                TABLE_OPCIONESCLAVE,
                new String[]{numerodeclave},
                numerodeclave + " = ?",
                new String[]{idClave},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean hasPendingData() {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT " + status + " FROM " + TABLE_NAME + " WHERE " + status + " = 'enviando'";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Hay registros pendientes
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        // No hay registros pendientes
        return false;
    }

    public boolean isRecordExist(SQLiteDatabase db, String intIdPadron, String intIdPaquete) {
        Cursor cursor = null;
        try {
            // Consulta para verificar la existencia del registro en ambas tablas
            String query = "SELECT 1 " +
                    "FROM lecturas l " +
                    "WHERE l." + IntIdPadron + " = ? AND l." + intidPaquete + " = ? " +
                    "UNION " +
                    "SELECT 1 " +
                    "FROM lecturas_endpoint le " +
                    "WHERE le." + IntIdPadron + " = ? AND le." + intidPaquete + " = ?" + "LIMIT 100 OFFSET 0";

            cursor = db.rawQuery(query, new String[]{intIdPadron, intIdPaquete, intIdPadron, intIdPaquete});

            // Si el cursor tiene al menos un resultado, entonces el registro existe en alguna de las tablas
            return cursor.moveToFirst();
        } catch (Exception e) {
            // Manejar la excepción según tus requisitos
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void setDataUpdateListener(DataUpdateListener listener) {
        this.dataUpdateListener = listener;
    }

    public interface DataUpdateListener {
        void onDataUpdated(ArrayList<ListDataEntradas> updatedData);

        void onDatabaseError(String errorMessage);
    }

    public interface DatabaseCallback {
        void onDatabaseError(String errorMessage);
    }

    public void setDatabaseCallback(DatabaseCallback callback) {
        this.databaseCallback = callback;
    }

    public boolean obtenerAceptaLectura(String selectedClave) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean ObtenerAceptaLectura = false;

        String query = "SELECT " + aceptaLectura + " FROM " + TABLE_OPCIONESCLAVE + " WHERE " + vchnumClaveLectura + " = ?";

        String[] selectionArgs = {selectedClave};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            int aceptaLecturaIndex = cursor.getColumnIndexOrThrow(aceptaLectura);
            // Obtiene el valor de aceptaLectura
            ObtenerAceptaLectura = cursor.getInt(aceptaLecturaIndex) == 1;
            Log.d(TAG, "obtenerAceptaLectura: ObtenerAceptaLectura: " + ObtenerAceptaLectura);
            cursor.close();
        }

        cursor.close();
        db.close();
        Log.d(TAG, "obtenerAceptaLectura: ObtenerAceptaLectura: " + ObtenerAceptaLectura);
        return ObtenerAceptaLectura;
    }


    // CODIGO BENEFICIARIO

    public long insertBeneficiario(LecturaBeneficiarios lectura) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        long insertedRowId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(vchCURP, lectura.getVchCURP());
            values.put(vchRFC, lectura.getVchRFC());
            values.put(vchNombre, lectura.getVchNombre());
            values.put(vchPaterno, lectura.getVchPaterno());
            values.put(vchMaterno, lectura.getVchMaterno());
            values.put(vchCalle, lectura.getVchCalle());
            values.put(vchNumExt, lectura.getVchNumExt());
            values.put(vchNumInt, lectura.getVchNumInt());
            values.put(vchColonia, lectura.getVchColonia());
            values.put(intEdad, lectura.getIntEdad());
            values.put(vchSexo, lectura.getVchSexo());
            values.put(vchTelefono, lectura.getVchTelefono());
            values.put(fltMetrosCasa, lectura.getFltMetrosCasa());
            values.put(vchColor, lectura.getVchColor());
            values.put(vchLatitud, lectura.getVchLatitud());
            values.put(vchLongitud, lectura.getVchLongitud());
            values.put(vchFotoIdentificacionFrente, lectura.getVchFotoIdentificacionFrente());
            values.put(vchFotoIdentificacionReverso, lectura.getVchFotoIdentificacionReverso());
            values.put(vchFotoComprobante, lectura.getVchFotoComprobante());
            values.put(vchFotoAnuenciaTrabajo, lectura.getVchFotoAnuenciaTrabajo());
            values.put(vchFotoFachadaAntes, lectura.getVchFotoFachadaAntes());
            values.put(vchFotoFachadaDespues, lectura.getVchFotoFachadaDespues());
            values.put(vchFotoConfirmidad, lectura.getVchFotoConfirmidad());
            values.put(statusEnvio, "enviando");

            insertedRowId = db.insertOrThrow(TABLE_BENEFICIARIO_NUEVO, null, values);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String errorMessage = "Error durante la inserción de datos: " + e.getMessage() + " causa: " + e.getCause();
            Log.e("DatabaseHelper", errorMessage);
            ErrorManager.saveErrorMessage(context, errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error: " + e.getMessage() + " causa: " + e.getCause();
            Log.e("DatabaseHelper", errorMessage);
            ErrorManager.saveErrorMessage(context, errorMessage);
        } finally {
            db.endTransaction();
            if (db != null) {
                db.close();
            }
        }
        return insertedRowId;
    }

    public long insertBeneficiarioEnObtenidosNoConexionInternet(LecturaBeneficiarios lectura) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vchCURP, lectura.getVchCURP());
        values.put(vchRFC, lectura.getVchRFC());
        values.put(vchNombre, lectura.getVchNombre());
        values.put(vchPaterno, lectura.getVchPaterno());
        values.put(vchMaterno, lectura.getVchMaterno());
        values.put(vchCalle, lectura.getVchCalle());
        values.put(vchNumExt, lectura.getVchNumExt());
        values.put(vchNumInt, lectura.getVchNumInt());
        values.put(vchColonia, lectura.getVchColonia());
        values.put(intEdad, lectura.getIntEdad());
        values.put(vchSexo, lectura.getVchSexo());
        values.put(vchTelefono, lectura.getVchTelefono());
        values.put(fltMetrosCasa, lectura.getFltMetrosCasa());
        values.put(vchColor, lectura.getVchColor());
        values.put(vchLatitud, lectura.getVchLatitud());
        values.put(vchLongitud, lectura.getVchLongitud());
        values.put(vchFotoIdentificacionFrente, lectura.getVchFotoIdentificacionFrente());
        values.put(vchFotoIdentificacionReverso, lectura.getVchFotoIdentificacionReverso());
        values.put(vchFotoComprobante, lectura.getVchFotoComprobante());
        values.put(vchFotoAnuenciaTrabajo, lectura.getVchFotoAnuenciaTrabajo());
        values.put(vchFotoFachadaAntes, lectura.getVchFotoFachadaAntes());
        values.put(vchFotoFachadaDespues, lectura.getVchFotoFachadaDespues());
        values.put(vchFotoConfirmidad, lectura.getVchFotoConfirmidad());
        values.put(statusEnvio, "enviando");

        long insertedRowId = -1;
        try {
            insertedRowId = db.insert(TABLE_BENEFICIARIO_OBTENIDOS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return insertedRowId;
    }


    public void sendBeneficiariosToServer(final List<LecturaBeneficiarios> lecturas) {
        for (final LecturaBeneficiarios lectura : lecturas) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(1250, TimeUnit.SECONDS)
                            .readTimeout(1250, TimeUnit.SECONDS)
                            .build();

                    MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM);

                    requestBodyBuilder.addFormDataPart("Curp", lectura.getVchCURP())
                            .addFormDataPart("Rfc", lectura.getVchRFC())
                            .addFormDataPart("Nombre", lectura.getVchNombre())
                            .addFormDataPart("Paterno", lectura.getVchPaterno())
                            .addFormDataPart("Materno", lectura.getVchMaterno())
                            .addFormDataPart("Calle", lectura.getVchCalle())
                            .addFormDataPart("NumExt", lectura.getVchNumExt())
                            .addFormDataPart("NumInt", lectura.getVchNumInt())
                            .addFormDataPart("Colonia", lectura.getVchColonia())
                            .addFormDataPart("Edad", String.valueOf(lectura.getIntEdad()))
                            .addFormDataPart("Sexo", lectura.getVchSexo())
                            .addFormDataPart("Telefono", lectura.getVchTelefono())
                            .addFormDataPart("MetrosCasa", lectura.getFltMetrosCasa())
                            .addFormDataPart("Color", lectura.getVchColor())
                            .addFormDataPart("Latitud", lectura.getVchLatitud())
                            .addFormDataPart("Longitud", lectura.getVchLongitud());

                    if (!lectura.getVchFotoIdentificacionFrente().isEmpty()) {
                        File fileFrente = new File(lectura.getVchFotoIdentificacionFrente());
                        requestBodyBuilder.addFormDataPart("fotoIdentificacionFrente", fileFrente.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileFrente));
                    }
                    if (!lectura.getVchFotoIdentificacionReverso().isEmpty()) {
                        File fileReverso = new File(lectura.getVchFotoIdentificacionReverso());
                        requestBodyBuilder.addFormDataPart("FotoIdentificacionReverso", fileReverso.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileReverso));
                    }
                    if (!lectura.getVchFotoComprobante().isEmpty()) {
                        File fileComprobante = new File(lectura.getVchFotoComprobante());
                        requestBodyBuilder.addFormDataPart("FotoComprobante", fileComprobante.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileComprobante));
                    }
                    if (!lectura.getVchFotoAnuenciaTrabajo().isEmpty()) {
                        File fileAnuencia = new File(lectura.getVchFotoAnuenciaTrabajo());
                        requestBodyBuilder.addFormDataPart("FotoAnuenciaTrabajo", fileAnuencia.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileAnuencia));
                    }
                    if (!lectura.getVchFotoFachadaAntes().isEmpty()) {
                        File fileFachadaAntes = new File(lectura.getVchFotoFachadaAntes());
                        requestBodyBuilder.addFormDataPart("FotoFachadaAntes", fileFachadaAntes.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileFachadaAntes));
                    }

                    SharedPreferences sharedPreferences = context.getSharedPreferences("mypref", Context.MODE_PRIVATE);
                    String authToken = sharedPreferences.getString(KEY_TOKEN, "");

                    RequestBody requestBody = requestBodyBuilder.build();


                    String url = "https://api.cruzada.changarro.online/api/v1/Beneficiarios/NuevoBeneficiario/";
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .header("Authorization", "Bearer " + authToken)
                            .build();
                    // Realizar la solicitud HTTP
                    try (okhttp3.Response response = client.newCall(request).execute()) {
                        int statusCode = response.code();
                        if (statusCode == 200) {
                            Log.d(TAG, "run: funciona");
                            SQLiteDatabase database = getWritableDatabase();
                            updateBeneficiarioStatusToExitoso(database, lecturas);
                            fetchBeneficiarioFromServer();

                            ArrayList<ListDataEntradas> updateData = getAllLecturasEndpoint();
                            if (dataUpdateListener != null) {
                                dataUpdateListener.onDataUpdated(updateData);
                            }

                        } else if (statusCode == 201) {
                            Log.d(TAG, "run: hace update");
                            SQLiteDatabase database = getWritableDatabase();
                            updateBeneficiarioStatusToExitoso(database, lecturas);

                            ArrayList<ListDataEntradas> updateData = getAllLecturasEndpoint();
                            if (dataUpdateListener != null) {
                                dataUpdateListener.onDataUpdated(updateData);
                            }


                        } else {
                            try {
                                String errorBody = response.body() != null ? response.body().string() : "Error body is null";
                                Log.e(TAG, "sendSingleDataToServer Error response body: " + errorBody);

                                JSONObject errorResponse = new JSONObject(errorBody);
                                String errorTitle = errorResponse.optString("Error");
                                String errorMessage = errorResponse.optString("Message", "");

                                if (databaseCallback != null) {
                                    databaseCallback.onDatabaseError("Error al realizar el envío al servidor con el CURP: " + lectura.getVchCURP() + ". " + errorBody + ". Código de error: " + statusCode);
                                }
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                                if (databaseCallback != null) {
                                    databaseCallback.onDatabaseError("Error al realizar el envío al servidor con el CURP: " + lectura.getVchCURP() + ". Código de error: " + statusCode + " y la descripcion " + e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (databaseCallback != null) {
                            databaseCallback.onDatabaseError("Error al realizar el envío de los datos al servidor con el CURP: "+ lectura.getVchCURP() + ". La descripcion es " + e.getMessage() + ". Reinicia la conexión e inténtalo más tarde.");
                        }
                    }
                }
            });
        }
    }

    public void updateBeneficiariosToServer(final List<LecturaBeneficiarios> lecturas) {
        for (final LecturaBeneficiarios lectura : lecturas) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(1250, TimeUnit.SECONDS)
                            .readTimeout(1250, TimeUnit.SECONDS)
                            .build();

                    MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM);

                    requestBodyBuilder.addFormDataPart("Curp", lectura.getVchCURP())
                            .addFormDataPart("Rfc", lectura.getVchRFC())

                            .addFormDataPart("Nombre", lectura.getVchNombre())
                            .addFormDataPart("Paterno", lectura.getVchPaterno())
                            .addFormDataPart("Materno", lectura.getVchMaterno())
                            .addFormDataPart("Calle", lectura.getVchCalle())
                            .addFormDataPart("NumExt", lectura.getVchNumExt())
                            .addFormDataPart("NumInt", lectura.getVchNumInt())
                            .addFormDataPart("Colonia", lectura.getVchColonia())
                            .addFormDataPart("Edad", String.valueOf(lectura.getIntEdad()))
                            .addFormDataPart("Sexo", lectura.getVchSexo())
                            .addFormDataPart("Telefono", lectura.getVchTelefono())
                            .addFormDataPart("MetrosCasa", lectura.getFltMetrosCasa())
                            .addFormDataPart("Color", lectura.getVchColor())
                            .addFormDataPart("Latitud", lectura.getVchLatitud())
                            .addFormDataPart("Longitud", lectura.getVchLongitud());

                    if (!lectura.getVchFotoIdentificacionFrente().isEmpty()) {
                        File fileFrente = new File(lectura.getVchFotoIdentificacionFrente());
                        requestBodyBuilder.addFormDataPart("fotoIdentificacionFrente", fileFrente.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileFrente));
                    }
                    if (!lectura.getVchFotoIdentificacionReverso().isEmpty()) {
                        File fileReverso = new File(lectura.getVchFotoIdentificacionReverso());
                        requestBodyBuilder.addFormDataPart("FotoIdentificacionReverso", fileReverso.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileReverso));
                    }
                    if (!lectura.getVchFotoComprobante().isEmpty()) {
                        File fileComprobante = new File(lectura.getVchFotoComprobante());
                        requestBodyBuilder.addFormDataPart("FotoComprobante", fileComprobante.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileComprobante));
                    }
                    if (!lectura.getVchFotoAnuenciaTrabajo().isEmpty()) {
                        File fileAnuencia = new File(lectura.getVchFotoAnuenciaTrabajo());
                        requestBodyBuilder.addFormDataPart("FotoAnuenciaTrabajo", fileAnuencia.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileAnuencia));
                    }
                    if (!lectura.getVchFotoFachadaAntes().isEmpty()) {
                        File fileFachadaAntes = new File(lectura.getVchFotoFachadaAntes());
                        requestBodyBuilder.addFormDataPart("FotoFachadaAntes", fileFachadaAntes.getName(), RequestBody.create(MediaType.parse("image/jpeg"), fileFachadaAntes));
                    }

                    SharedPreferences sharedPreferences = context.getSharedPreferences("mypref", Context.MODE_PRIVATE);
                    String authToken = sharedPreferences.getString(KEY_TOKEN, "");

                    RequestBody requestBody = requestBodyBuilder.build();
                    String baseUrl = "https://api.cruzada.changarro.online/api/v1/Beneficiarios/ActualizaBeneficiario/";
                    String url = baseUrl + lectura.getIntIdBeneficiario();

                    Request request = new Request.Builder()
                            .url(url)
                            .put(requestBody)
                            .header("Authorization", "Bearer " + authToken)
                            .build();

                    try (okhttp3.Response response = client.newCall(request).execute()) {
                        int statusCode = response.code();

                        if (statusCode == 204) {
                            Log.d(TAG, "run: fue exitoso");
                            SQLiteDatabase database = getWritableDatabase();
                            updateBeneficiariosToTabla(database, lectura);
                            updateBeneficiarioObtenidosStatusToExitoso(database, lecturas);

                            ArrayList<ListDataEntradas> updateData = getAllLecturasEndpoint();
                            if (dataUpdateListener != null) {
                                dataUpdateListener.onDataUpdated(updateData);
                            }
                        } else {
                            try {
                                String errorBody = response.body() != null ? response.body().string() : "Error body is null";
                                Log.e(TAG, "sendSingleDataToServer Error response body: " + errorBody);

                                JSONObject errorResponse = new JSONObject(errorBody);
                                String errorTitle = errorResponse.optString("Error");
                                String errorMessage = errorResponse.optString("Message", "");

                                if (databaseCallback != null) {
                                    databaseCallback.onDatabaseError("Error al realizar la actualización del dato al servidor con el CURP: " + lectura.getVchCURP() + ". " + errorBody + ". Código de error: " + statusCode);
                                }
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                                // Manejar errores de parsing o lectura
                                if (databaseCallback != null) {
                                    databaseCallback.onDatabaseError("Error al realizar la actualización al servidor con el CURP: " + lectura.getVchCURP() + ". Código de error: " + statusCode + " y la descripcion: " + e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (databaseCallback != null) {
                            databaseCallback.onDatabaseError("Error al realizar el envío de los datos al servidor con el CURP: " + lectura.getVchCURP() + " y la descripcion: " + e.getMessage());
                        }
                    }
                }
            });
        }
    }

    public void updateBeneficiariosToTabla(SQLiteDatabase database, LecturaBeneficiarios lectura) {
        ContentValues values = new ContentValues();
        values.put("Curp", lectura.getVchCURP());
        values.put("Rfc", lectura.getVchRFC());
        values.put("Nombre", lectura.getVchNombre());
        values.put("Paterno", lectura.getVchPaterno());
        values.put("Materno", lectura.getVchMaterno());
        values.put("Calle", lectura.getVchCalle());
        values.put("NumExt", lectura.getVchNumExt());
        values.put("NumInt", lectura.getVchNumInt());
        values.put("Colonia", lectura.getVchColonia());
        values.put("Edad", lectura.getIntEdad());
        values.put("Sexo", lectura.getVchSexo());
        values.put("Telefono", lectura.getVchTelefono());
        values.put("MetrosCasa", lectura.getFltMetrosCasa());
        values.put("Color", lectura.getVchColor());
        values.put("Latitud", lectura.getVchLatitud());
        values.put("Longitud", lectura.getVchLongitud());
        values.put("FotoIdentificacionFrente", lectura.getVchFotoIdentificacionFrente());
        values.put("FotoIdentificacionReverso", lectura.getVchFotoIdentificacionReverso());
        values.put("FotoComprobante", lectura.getVchFotoComprobante());
        values.put("FotoAnuenciaTrabajo", lectura.getVchFotoAnuenciaTrabajo());
        values.put("FotoFachadaAntes", lectura.getVchFotoFachadaAntes());

        int rowsAffected = database.update(TABLE_BENEFICIARIO_OBTENIDOS, values, "id = ?", new String[]{String.valueOf(lectura.getIntIdBeneficiario())});
        Log.d(TAG, "updateBeneficiariosToTabla: Filas actualizadas = " + rowsAffected);

    }

    public boolean updateBeneficiariosToTablaSinInternet(SQLiteDatabase database, LecturaBeneficiarios lectura) {
        boolean success = false;
        try {
            ContentValues values = new ContentValues();
            values.put("Curp", lectura.getVchCURP());
            values.put("Rfc", lectura.getVchRFC());
            values.put("Nombre", lectura.getVchNombre());
            values.put("Paterno", lectura.getVchPaterno());
            values.put("Materno", lectura.getVchMaterno());
            values.put("Calle", lectura.getVchCalle());
            values.put("NumExt", lectura.getVchNumExt());
            values.put("NumInt", lectura.getVchNumInt());
            values.put("Colonia", lectura.getVchColonia());
            values.put("Edad", lectura.getIntEdad());
            values.put("Sexo", lectura.getVchSexo());
            values.put("Telefono", lectura.getVchTelefono());
            values.put("MetrosCasa", lectura.getFltMetrosCasa());
            values.put("Color", lectura.getVchColor());
            values.put("Latitud", lectura.getVchLatitud());
            values.put("Longitud", lectura.getVchLongitud());
            values.put("FotoIdentificacionFrente", lectura.getVchFotoIdentificacionFrente());
            values.put("FotoIdentificacionReverso", lectura.getVchFotoIdentificacionReverso());
            values.put("FotoComprobante", lectura.getVchFotoComprobante());
            values.put("FotoAnuenciaTrabajo", lectura.getVchFotoAnuenciaTrabajo());
            values.put("FotoFachadaAntes", lectura.getVchFotoFachadaAntes());
            values.put("status_envio", "enviando");

            int rowsAffected = database.update(TABLE_BENEFICIARIO_OBTENIDOS, values, "id = ?", new String[]{String.valueOf(lectura.getIntIdBeneficiario())});
            Log.d(TAG, "updateBeneficiariosToTabla: Filas actualizadas = " + rowsAffected);

            if (rowsAffected > 0) {
                success = true;
            } else {
                success = false;
            }

        } catch (SQLException e) {
            String errorMessage = "Error: " + e.getMessage() + " causa: " + e.getCause();
            Log.e("DatabaseHelper", errorMessage);
            ErrorManager.saveErrorMessage(context, errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error: " + e.getMessage() + " causa: " + e.getCause();
            Log.e("DatabaseHelper", errorMessage);
            ErrorManager.saveErrorMessage(context, errorMessage);
        } finally {
            try {
                if (database != null) {
                    database.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al cerrar la base de datos: " + e.getMessage());
            }
        }

        return success;
    }

    public List<String> getAllCURPFromLocalDatabase() {
        List<String> curpList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + vchCURP + " FROM " + TABLE_BENEFICIARIO_NUEVO
                +
                " WHERE status_envio = ?", new String[]{"exitoso"});

        if (cursor.moveToFirst()) {
            do {
                String curp = cursor.getString(cursor.getColumnIndexOrThrow(vchCURP));
                curpList.add(curp);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return curpList;
    }

    public void fetchBeneficiarioFromServer() {
        List<String> curpList = getAllCURPFromLocalDatabase();

        SharedPreferences sharedPreferences = context.getSharedPreferences("mypref", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString(KEY_TOKEN, "");
        Log.d(TAG, "AuthToken: " + authToken);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(390, TimeUnit.SECONDS)
                .readTimeout(390, TimeUnit.SECONDS)
                .writeTimeout(390, TimeUnit.SECONDS)
                .build();

        for (String curp : curpList) {
            String baseUrl = "https://api.cruzada.changarro.online/api/v1/Beneficiarios/ObtenBeneficiario";
            String url = baseUrl + "?curp=" + curp;

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + authToken)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Hubo un error al obtener el token. Por favor, vuelva a iniciar sesión.");

                            // Mostrar un Toast o realizar otras operaciones en el hilo principal
                            Toast.makeText(context.getApplicationContext(), "Error de conexión. Por favor, verifica tu conexión a internet.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    int statusCode = response.code();
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        Log.d(TAG, "onResponse: curp: " + curp);
                        processBeneficiarioData(responseData, curp);

                        ArrayList<ListDataEntradas> updateData = getAllBeneficiariosEndpoint();
                        if (dataUpdateListener != null) {
                            dataUpdateListener.onDataUpdated(updateData);
                        }

                    } else {
                        try {
                            String errorBody = response.body() != null ? response.body().string() : "Error body is null";
                            Log.e(TAG, "sendSingleDataToServer Error response body: " + errorBody);

                            JSONObject errorResponse = new JSONObject(errorBody);
                            String errorTitle = errorResponse.optString("Error");
                            String errorMessage = errorResponse.optString("Message", "");

                            if (databaseCallback != null) {
                                databaseCallback.onDatabaseError("Error al recibir los datos del servidor. " + errorBody + ". Código de error: " + statusCode);
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            // Manejar errores de parsing o lectura
                            if (databaseCallback != null) {
                                databaseCallback.onDatabaseError("Error al realizar la obtención de beneficiarios del servidor con el curp: " + curp + ". Código de error: " + statusCode + " con la descripción: " + e.getMessage() );
                            }
                        }
                    }
                }
            });
        }
    }

    private void processBeneficiarioData(String jsonData, String curp) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject beneficiarioData = jsonObject.getJSONObject("data");

            if (curpExistsLocally(curp)) {
                saveBeneficiarioToDatabase(curp, beneficiarioData);

            } else {
                Log.d(TAG, "processBeneficiarioData: La CURP no existe localmente, no se guardará para su actualización.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (databaseCallback != null) {
                databaseCallback.onDatabaseError("Hubo un error al procesar los datos recibidos del servidor. Vuelva a intentarlo más tarde. Descripcion: " + e.getMessage());
            }
        }
    }

    private boolean curpExistsLocally(String curp) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_BENEFICIARIO_NUEVO + " WHERE " + vchCURP + " = ?";
            String[] selectionArgs = {curp};
            cursor = db.rawQuery(query, selectionArgs);
            return cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void saveBeneficiarioToDatabase(String curp, JSONObject beneficiarioData) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            if (!curpExists(db, curp)) {
                values.put(intIdActualizacion, beneficiarioData.getInt("id"));
                values.put(vchCURP, beneficiarioData.getString("curp"));
                values.put(vchRFC, beneficiarioData.getString("rfc"));
                values.put(vchNombre, beneficiarioData.getString("nombre"));
                values.put(vchPaterno, beneficiarioData.getString("paterno"));
                values.put(vchMaterno, beneficiarioData.getString("materno"));
                values.put(vchCalle, beneficiarioData.getString("calle"));
                values.put(vchNumExt, beneficiarioData.getString("numExt"));
                values.put(vchNumInt, beneficiarioData.getString("numInt"));
                values.put(vchColonia, beneficiarioData.getString("colonia"));
                values.put(intEdad, beneficiarioData.getInt("edad"));
                values.put(vchSexo, beneficiarioData.getString("sexo"));
                values.put(vchTelefono, beneficiarioData.getString("telefono"));
                values.put(fltMetrosCasa, beneficiarioData.getDouble("metrosCasa"));
                values.put(vchColor, beneficiarioData.getString("color"));
                values.put(vchLatitud, beneficiarioData.getString("latitud"));
                values.put(vchLongitud, beneficiarioData.getString("longitud"));

                if (!beneficiarioData.getString("fotoIdentificacionFrente").equals("null")){
                    String fotoIdentificacionFrenteUrl = beneficiarioData.getString("fotoIdentificacionFrente");
                    saveImageLocallyFrente(fotoIdentificacionFrenteUrl, beneficiarioData.getString("curp"));
                }

                if (!beneficiarioData.getString("fotoIdentificacionReverso").equals("null")){
                    String fotoIdentificacionInversoUrl = beneficiarioData.getString("fotoIdentificacionReverso");
                    saveImageLocallyInverso(fotoIdentificacionInversoUrl, beneficiarioData.getString("curp"));
                }

                if (!beneficiarioData.getString("fotoComprobante").equals("null")){
                    String fotoIdentificacionComprobanteUrl = beneficiarioData.getString("fotoComprobante");
                    saveImageLocallyComprobante(fotoIdentificacionComprobanteUrl, beneficiarioData.getString("curp"));
                }

                if (!beneficiarioData.getString("fotoAnuenciaTrabajo").equals("null")){
                    String fotoIdentificacionAnuencuaUrl = beneficiarioData.getString("fotoAnuenciaTrabajo");
                    saveImageLocallyAnuencia(fotoIdentificacionAnuencuaUrl, beneficiarioData.getString("curp"));
                }

                if (!beneficiarioData.getString("fotoFachadaAntes").equals("null")){
                    String fotoIdentificacionFachadaAntes = beneficiarioData.getString("fotoFachadaAntes");
                    saveImageLocallyAntes(fotoIdentificacionFachadaAntes, beneficiarioData.getString("curp"));
                }

                values.put(vchFotoFachadaDespues, beneficiarioData.getString("fotoFachadaDespues"));
                values.put(vchFotoConfirmidad, beneficiarioData.getString("fotoConfirmidad"));
                values.put(vchFactura, beneficiarioData.getString("factura"));
                values.put(statusEnvio, "ingresado");

                db.insert(TABLE_BENEFICIARIO_OBTENIDOS, null, values);

                ArrayList<ListDataEntradas> updateData = getAllLecturasEndpoint();
                if (dataUpdateListener != null) {
                    dataUpdateListener.onDataUpdated(updateData);
                }

            } else {
                Log.d(TAG, "saveBeneficiarioToDatabase: La CURP ya está registrada en la base de datos.");
            }

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (JSONException e) {
            e.printStackTrace();

            if (databaseCallback != null) {
                Log.d(TAG, "saveBeneficiarioToDatabase: error al procesar el JSON: " + e.getMessage());
                databaseCallback.onDatabaseError("Error al procesar los datos recibidos del servidor para la CURP: " + curp + ". No se guardará para su actualización. Descripcion del problema: " + e.getMessage());
            }
        }
    }

    private void saveImageLocallyFrente(String imageUrl, String curp) {
        Log.d(TAG, "saveImageLocallyFrente: imageUrl: " + imageUrl);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error al descargar la imagen: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_";
                        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        inputStream = response.body().byteStream();
                        File file = File.createTempFile(
                                imageFileName,  // Nombre del archivo
                                ".jpg",        // Extensión del archivo
                                storageDir     // Directorio de almacenamiento
                        );

                        String imageFile = file.getAbsolutePath();
                        Log.d(TAG, "onResponse: file: " + file);
                        outputStream = Files.newOutputStream(file.toPath());

                        byte[] buffer = new byte[4 * 1024]; // buffer de 4KB
                        int read;

                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        // Actualizar el path en la base de datos local
                        updateImagePathInDatabaseFrente(imageFile, curp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } else {
                    Log.d(TAG, "Respuesta no exitosa al descargar la imagen: " + response.code());
                }
            }
        });
    }
    private void updateImagePathInDatabaseFrente(String localFileName, String curp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vchFotoIdentificacionFrente, localFileName);

        String whereClause = "curp = ?";
        String[] whereArgs = {curp};

        int rowsUpdated = db.update(TABLE_BENEFICIARIO_OBTENIDOS, values, whereClause, whereArgs);

        Log.d(TAG, "Filas actualizadas: " + rowsUpdated);
    }

    private void saveImageLocallyInverso(String imageUrl, String curp) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error al descargar la imagen: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_";
                        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        inputStream = response.body().byteStream();
                        File file = File.createTempFile(
                                imageFileName,  // Nombre del archivo
                                ".jpg",        // Extensión del archivo
                                storageDir     // Directorio de almacenamiento
                        );

                        String imageFile = file.getAbsolutePath();
                        Log.d(TAG, "onResponse: file: " + file);
                        outputStream = Files.newOutputStream(file.toPath());

                        byte[] buffer = new byte[4 * 1024]; // buffer de 4KB
                        int read;

                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        // Actualizar el path en la base de datos local
                        updateImagePathInDatabaseInverso(imageFile, curp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } else {
                    Log.d(TAG, "Respuesta no exitosa al descargar la imagen: " + response.code());
                }
            }
        });
    }
    private void updateImagePathInDatabaseInverso(String localFileName, String curp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vchFotoIdentificacionReverso, localFileName); // Cambia esto por el campo correcto en tu base de datos

        String whereClause = "curp = ?"; // Cambia esto por la columna donde estaba la URL original
        String[] whereArgs = {curp}; // Cambia esto por el valor de la URL original

        int rowsUpdated = db.update(TABLE_BENEFICIARIO_OBTENIDOS, values, whereClause, whereArgs);

        Log.d(TAG, "Filas actualizadas: " + rowsUpdated);
    }

    private void saveImageLocallyComprobante(String imageUrl, String curp) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error al descargar la imagen: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_";
                        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        inputStream = response.body().byteStream();
                        File file = File.createTempFile(
                                imageFileName,  // Nombre del archivo
                                ".jpg",        // Extensión del archivo
                                storageDir     // Directorio de almacenamiento
                        );

                        String imageFile = file.getAbsolutePath();
                        Log.d(TAG, "onResponse: file: " + file);
                        outputStream = Files.newOutputStream(file.toPath());

                        byte[] buffer = new byte[4 * 1024]; // buffer de 4KB
                        int read;

                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        // Actualizar el path en la base de datos local
                        updateImagePathInDatabaseComprobante(imageFile, curp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } else {
                    Log.d(TAG, "Respuesta no exitosa al descargar la imagen: " + response.code());
                }
            }
        });
    }
    private void updateImagePathInDatabaseComprobante(String localFileName, String curp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vchFotoComprobante, localFileName); // Cambia esto por el campo correcto en tu base de datos

        String whereClause = "curp = ?"; // Cambia esto por la columna donde estaba la URL original
        String[] whereArgs = {curp}; // Cambia esto por el valor de la URL original

        int rowsUpdated = db.update(TABLE_BENEFICIARIO_OBTENIDOS, values, whereClause, whereArgs);

        Log.d(TAG, "Filas actualizadas: " + rowsUpdated);
    }

    private void saveImageLocallyAnuencia(String imageUrl, String curp) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error al descargar la imagen: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_";
                        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        inputStream = response.body().byteStream();
                        File file = File.createTempFile(
                                imageFileName,  // Nombre del archivo
                                ".jpg",        // Extensión del archivo
                                storageDir     // Directorio de almacenamiento
                        );

                        String imageFile = file.getAbsolutePath();
                        Log.d(TAG, "onResponse: file: " + file);
                        outputStream = Files.newOutputStream(file.toPath());

                        byte[] buffer = new byte[4 * 1024]; // buffer de 4KB
                        int read;

                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        // Actualizar el path en la base de datos local
                        updateImagePathInDatabaseAnuencia(imageFile, curp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } else {
                    Log.d(TAG, "Respuesta no exitosa al descargar la imagen: " + response.code());
                }
            }
        });
    }
    private void updateImagePathInDatabaseAnuencia(String localFileName, String curp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vchFotoAnuenciaTrabajo, localFileName); // Cambia esto por el campo correcto en tu base de datos

        String whereClause = "curp = ?"; // Cambia esto por la columna donde estaba la URL original
        String[] whereArgs = {curp}; // Cambia esto por el valor de la URL original

        int rowsUpdated = db.update(TABLE_BENEFICIARIO_OBTENIDOS, values, whereClause, whereArgs);

        Log.d(TAG, "Filas actualizadas: " + rowsUpdated);
    }

    private void saveImageLocallyAntes(String imageUrl, String curp) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error al descargar la imagen: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_";
                        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        inputStream = response.body().byteStream();
                        File file = File.createTempFile(
                                imageFileName,
                                ".jpg",
                                storageDir
                        );

                        String imageFile = file.getAbsolutePath();
                        Log.d(TAG, "onResponse: file: " + file);
                        outputStream = Files.newOutputStream(file.toPath());

                        byte[] buffer = new byte[4 * 1024]; // buffer de 4KB
                        int read;

                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        // Actualizar el path en la base de datos local
                        updateImagePathInDatabaseAntes(imageFile, curp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } else {
                    Log.d(TAG, "Respuesta no exitosa al descargar la imagen: " + response.code());
                }
            }
        });
    }
    private void updateImagePathInDatabaseAntes(String localFileName, String curp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vchFotoFachadaAntes, localFileName);

        String whereClause = "curp = ?";
        String[] whereArgs = {curp};

        int rowsUpdated = db.update(TABLE_BENEFICIARIO_OBTENIDOS, values, whereClause, whereArgs);

        Log.d(TAG, "Filas actualizadas: " + rowsUpdated);
    }

    public ArrayList<ListDataEntradas> getAllBeneficiariosEndpoint() {
        ArrayList<ListDataEntradas> lecturasEndpointList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_BENEFICIARIO_OBTENIDOS + " WHERE " + statusEnvio + " = 'ingresado'" ;
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(intIdActualizacion));
                    String curp = cursor.getString(cursor.getColumnIndexOrThrow(vchCURP));
                    String rfc = cursor.getString(cursor.getColumnIndexOrThrow(vchRFC));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow(vchNombre));
                    String paterno = cursor.getString(cursor.getColumnIndexOrThrow(vchPaterno));
                    String materno = cursor.getString(cursor.getColumnIndexOrThrow(vchMaterno));
                    String calle = cursor.getString(cursor.getColumnIndexOrThrow(vchCalle));
                    String numExt = cursor.getString(cursor.getColumnIndexOrThrow(vchNumExt));
                    String numInt = cursor.getString(cursor.getColumnIndexOrThrow(vchNumInt));
                    String colonia = cursor.getString(cursor.getColumnIndexOrThrow(vchColonia));

                    ListDataEntradas lecturaEndpointData = new ListDataEntradas(
                            id, curp, rfc, nombre, paterno,
                            materno, calle, numExt, numInt, colonia
                    );
                    lecturasEndpointList.add(lecturaEndpointData);
                    Log.d(TAG, "getAllBeneficiariosEndpoint: exitoso: " + nombre);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return lecturasEndpointList;
    }

    public LecturaBeneficiarios getEndpointBeneficiarios(String curp) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BENEFICIARIO_OBTENIDOS + " WHERE " + vchCURP + " = ? ";
        String[] selectionArgs = {curp};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        LecturaBeneficiarios lecturaBeneficiarios = null;
        if (cursor.moveToFirst()) {
            lecturaBeneficiarios = new LecturaBeneficiarios();
            lecturaBeneficiarios.setVchCURP(cursor.getString(cursor.getColumnIndexOrThrow(vchCURP)));
            lecturaBeneficiarios.setVchRFC(cursor.getString(cursor.getColumnIndexOrThrow(vchRFC)));
            lecturaBeneficiarios.setVchNombre(cursor.getString(cursor.getColumnIndexOrThrow(vchNombre)));
            lecturaBeneficiarios.setVchPaterno(cursor.getString(cursor.getColumnIndexOrThrow(vchPaterno)));
            lecturaBeneficiarios.setVchMaterno(cursor.getString(cursor.getColumnIndexOrThrow(vchMaterno)));
            lecturaBeneficiarios.setVchCalle(cursor.getString(cursor.getColumnIndexOrThrow(vchCalle)));
            lecturaBeneficiarios.setVchNumExt(cursor.getString(cursor.getColumnIndexOrThrow(vchNumExt)));
            lecturaBeneficiarios.setVchNumInt(cursor.getString(cursor.getColumnIndexOrThrow(vchNumInt)));
            lecturaBeneficiarios.setVchColonia(cursor.getString(cursor.getColumnIndexOrThrow(vchColonia)));
            lecturaBeneficiarios.setIntEdad(cursor.getString(cursor.getColumnIndexOrThrow(intEdad)));
            lecturaBeneficiarios.setVchSexo(cursor.getString(cursor.getColumnIndexOrThrow(vchSexo)));
            lecturaBeneficiarios.setVchTelefono(cursor.getString(cursor.getColumnIndexOrThrow(vchTelefono)));
            lecturaBeneficiarios.setFltMetrosCasa(cursor.getString(cursor.getColumnIndexOrThrow(fltMetrosCasa)));
            lecturaBeneficiarios.setVchColor(cursor.getString(cursor.getColumnIndexOrThrow(vchColor)));
            lecturaBeneficiarios.setVchLatitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLatitud)));
            lecturaBeneficiarios.setVchLongitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLongitud)));
            lecturaBeneficiarios.setVchFotoIdentificacionFrente(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoIdentificacionFrente)));
            lecturaBeneficiarios.setVchFotoIdentificacionReverso(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoIdentificacionReverso)));
            lecturaBeneficiarios.setVchFotoComprobante(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoComprobante)));
            lecturaBeneficiarios.setVchFotoAnuenciaTrabajo(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoAnuenciaTrabajo)));
            lecturaBeneficiarios.setVchFotoFachadaAntes(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoFachadaAntes)));
            lecturaBeneficiarios.setVchFotoFachadaDespues(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoFachadaDespues)));
            lecturaBeneficiarios.setVchFotoConfirmidad(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoConfirmidad)));
        }
        cursor.close();
        return lecturaBeneficiarios;
    }

    public boolean isDuplicateBeneficiario(String curp) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BENEFICIARIO_NUEVO + " WHERE " + vchCURP + " = ?";
        String[] selectionArgs = {curp};
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.rawQuery(query, selectionArgs);
            exists = cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

    public void updateBeneficiarioStatusToExitoso(SQLiteDatabase database, List<LecturaBeneficiarios> lecturas) {
        Log.d(TAG, "updateBeneficiarioStatusToExitoso: se actualiza");
        lecturas.forEach(lectura -> {
            if ("enviando".equals(lectura.getStatus())) {
                lectura.actualizarStatus(database, "exitoso");
            }
        });
    }

    public void updateBeneficiarioObtenidosStatusToExitoso(SQLiteDatabase database, List<LecturaBeneficiarios> lecturas) {
        lecturas.forEach(lectura -> {
            if ("enviando".equals(lectura.getStatus())) {
                lectura.actualizarStatusObtenidos(database, "ingresado");
            }
        });
    }

    private boolean curpExists(SQLiteDatabase db, String curp) {
        String[] columns = { vchCURP };
        String selection = vchCURP + " = ?";
        String[] selectionArgs = { curp };

        Cursor cursor = db.query(TABLE_BENEFICIARIO_OBTENIDOS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean hasPendingBeneficiarios() {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT " + statusEnvio + " FROM " + TABLE_BENEFICIARIO_NUEVO + " WHERE " + statusEnvio + " = 'enviando'";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Hay registros pendientes
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return false;
    }

    public ArrayList<LecturaBeneficiarios> getPendingBeneficiarios() {
        ArrayList<LecturaBeneficiarios> pendingDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_BENEFICIARIO_NUEVO + " WHERE " + statusEnvio + " = 'enviando'";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    LecturaBeneficiarios lectura = new LecturaBeneficiarios();
                    lectura.setVchCURP(cursor.getString(cursor.getColumnIndexOrThrow(vchCURP)));
                    lectura.setVchRFC(cursor.getString(cursor.getColumnIndexOrThrow(vchRFC)));
                    lectura.setVchNombre(cursor.getString(cursor.getColumnIndexOrThrow(vchNombre)));
                    lectura.setVchPaterno(cursor.getString(cursor.getColumnIndexOrThrow(vchPaterno)));
                    lectura.setVchMaterno(cursor.getString(cursor.getColumnIndexOrThrow(vchMaterno)));
                    lectura.setVchCalle(cursor.getString(cursor.getColumnIndexOrThrow(vchCalle)));
                    lectura.setVchNumExt(cursor.getString(cursor.getColumnIndexOrThrow(vchNumExt)));
                    lectura.setVchNumInt(cursor.getString(cursor.getColumnIndexOrThrow(vchNumInt)));
                    lectura.setVchColonia(cursor.getString(cursor.getColumnIndexOrThrow(vchColonia)));
                    lectura.setIntEdad(cursor.getString(cursor.getColumnIndexOrThrow(intEdad)));
                    lectura.setVchSexo(cursor.getString(cursor.getColumnIndexOrThrow(vchSexo)));
                    lectura.setVchTelefono(cursor.getString(cursor.getColumnIndexOrThrow(vchTelefono)));
                    lectura.setFltMetrosCasa(cursor.getString(cursor.getColumnIndexOrThrow(fltMetrosCasa)));
                    lectura.setVchColor(cursor.getString(cursor.getColumnIndexOrThrow(vchColor)));
                    lectura.setVchLatitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLatitud)));
                    lectura.setVchLongitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLongitud)));
                    lectura.setVchFotoIdentificacionFrente(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoIdentificacionFrente)));
                    lectura.setVchFotoIdentificacionReverso(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoIdentificacionReverso)));
                    lectura.setVchFotoComprobante(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoComprobante)));
                    lectura.setVchFotoAnuenciaTrabajo(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoAnuenciaTrabajo)));
                    lectura.setVchFotoFachadaAntes(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoFachadaAntes)));
                    lectura.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(statusEnvio)));


                    pendingDataList.add(lectura);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return pendingDataList;
    }
    public ArrayList<LecturaBeneficiarios> getPendingBeneficiariosEnObtenido() {
        ArrayList<LecturaBeneficiarios> pendingDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_BENEFICIARIO_OBTENIDOS + " WHERE " + statusEnvio + " = 'enviando'";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    LecturaBeneficiarios lectura = new LecturaBeneficiarios();
                    lectura.setIntIdBeneficiario(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(intIdActualizacion))));
                    lectura.setVchCURP(cursor.getString(cursor.getColumnIndexOrThrow(vchCURP)));
                    lectura.setVchRFC(cursor.getString(cursor.getColumnIndexOrThrow(vchRFC)));
                    lectura.setVchNombre(cursor.getString(cursor.getColumnIndexOrThrow(vchNombre)));
                    lectura.setVchPaterno(cursor.getString(cursor.getColumnIndexOrThrow(vchPaterno)));
                    lectura.setVchMaterno(cursor.getString(cursor.getColumnIndexOrThrow(vchMaterno)));
                    lectura.setVchCalle(cursor.getString(cursor.getColumnIndexOrThrow(vchCalle)));
                    lectura.setVchNumExt(cursor.getString(cursor.getColumnIndexOrThrow(vchNumExt)));
                    lectura.setVchNumInt(cursor.getString(cursor.getColumnIndexOrThrow(vchNumInt)));
                    lectura.setVchColonia(cursor.getString(cursor.getColumnIndexOrThrow(vchColonia)));
                    lectura.setIntEdad(cursor.getString(cursor.getColumnIndexOrThrow(intEdad)));
                    lectura.setVchSexo(cursor.getString(cursor.getColumnIndexOrThrow(vchSexo)));
                    lectura.setVchTelefono(cursor.getString(cursor.getColumnIndexOrThrow(vchTelefono)));
                    lectura.setFltMetrosCasa(cursor.getString(cursor.getColumnIndexOrThrow(fltMetrosCasa)));
                    lectura.setVchColor(cursor.getString(cursor.getColumnIndexOrThrow(vchColor)));
                    lectura.setVchLatitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLatitud)));
                    lectura.setVchLongitud(cursor.getString(cursor.getColumnIndexOrThrow(vchLongitud)));
                    lectura.setVchFotoIdentificacionFrente(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoIdentificacionFrente)));
                    lectura.setVchFotoIdentificacionReverso(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoIdentificacionReverso)));
                    lectura.setVchFotoComprobante(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoComprobante)));
                    lectura.setVchFotoAnuenciaTrabajo(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoAnuenciaTrabajo)));
                    lectura.setVchFotoFachadaAntes(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoFachadaAntes)));
                    lectura.setVchFotoFachadaDespues(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoFachadaDespues)));
                    lectura.setVchFotoConfirmidad(cursor.getString(cursor.getColumnIndexOrThrow(vchFotoConfirmidad)));
                    lectura.setVchfactura(cursor.getString(cursor.getColumnIndexOrThrow(vchFactura)));
                    lectura.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(statusEnvio)));

                    pendingDataList.add(lectura);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return pendingDataList;
    }

    public int getIntIdBeneficiario(String curp) {
        int intIdBeneficiario = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {intIdActualizacion};
        String selection = vchCURP + " = ?";
        String[] selectionArgs = {curp};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BENEFICIARIO_OBTENIDOS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                intIdBeneficiario = cursor.getInt(cursor.getColumnIndexOrThrow(intIdActualizacion));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return intIdBeneficiario;
    }

}