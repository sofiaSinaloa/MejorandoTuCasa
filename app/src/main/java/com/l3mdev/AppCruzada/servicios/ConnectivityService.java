package com.l3mdev.AppCruzada.servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ConnectivityService extends Service {

    private static final int SIGNAL_STRENGTH_THRESHOLD = 5;
    private static final int GSM_SIGNAL_STRENGTH_THRESHOLD = 5;
    private static final int CDMA_SIGNAL_STRENGTH_THRESHOLD = -85; // Ejemplo, puede ajustarse según sea necesario
    private static final String TAG = "ConnectivityService";

    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean isSignalWeak;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initTelephonyManager();
        Log.d(TAG, "onCreate: empieza el escucha de la intensidad de red.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopListeningSignalStrength();
        Log.d(TAG, "onDestroy: se ha detenido");
    }

    public void initTelephonyManager() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            listenSignalStrength();
        }
    }

    private void listenSignalStrength() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                int signalStrengthValue = getSignalStrengthValue(signalStrength);
                handleNetworkQuality(signalStrengthValue);
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void stopListeningSignalStrength() {
        Log.d(TAG, "stopListeningSignalStrength: se detuvo el escucha de la intensidad de red.");
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private int getSignalStrengthValue(SignalStrength signalStrength) {
        if (signalStrength.isGsm()) {
            return signalStrength.getGsmSignalStrength();
        } else {
            return signalStrength.getCdmaDbm();
        }
    }

    // Método para manejar la calidad de la red
    public void handleNetworkQuality(int signalStrengthValue) {
        int threshold;
        if (telephonyManager != null) {
            int phoneType = telephonyManager.getPhoneType();
            if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                threshold = GSM_SIGNAL_STRENGTH_THRESHOLD;
            } else {
                threshold = CDMA_SIGNAL_STRENGTH_THRESHOLD;
            }

            if (signalStrengthValue <= threshold) {
                isSignalWeak = true; // Señal débil detectada
                Log.d(TAG, "handleNetworkQuality: Se detectó una señal débil.");
            } else {
                isSignalWeak = false; // Señal normal
                Log.d(TAG, "handleNetworkQuality: Se detectó una señal normal.");
            }
            sendSignalStrengthBroadcast(isSignalWeak);
        }
    }

    private void sendSignalStrengthBroadcast(boolean isSignalWeak) {
        Intent intent = new Intent("com.example.SIGNAL_STRENGTH_CHANGED");
        intent.putExtra("isSignalWeak", isSignalWeak);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }
}
