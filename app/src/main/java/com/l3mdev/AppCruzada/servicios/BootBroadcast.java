package com.l3mdev.AppCruzada.servicios;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.e("RestartServiceBroadcastReceiver", "Service Stopped, but is starting again");
            context.startService(new Intent(context, ConnectivityService.class));
        }
    }
}