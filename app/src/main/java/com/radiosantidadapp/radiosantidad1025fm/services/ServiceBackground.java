package com.radiosantidadapp.radiosantidad1025fm.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.radiosantidadapp.radiosantidad1025fm.Configs.Config;

public class ServiceBackground extends Service {
    private final int STATUS_INIT = 3;
    private final int STATUS_PLAY = 1;
    private final int STATUS_STOP = 0;
    private int statusAudio;
    private ServiceNotification serviceNotification;
    private ServiceStateCall serviceStateCall;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private Config config;
    private float volume;
    private ServiceAudio serviceAudio;
    private Intent intentBackground;

    public ServiceBackground() {}

    @Override
    public void onCreate() {
        super.onCreate();
        this.config = new Config();
        this.intentBackground = new Intent(getApplicationContext(), ServiceBackground.class);
        serviceNotification = new ServiceNotification(getApplicationContext());
        serviceStateCall = new ServiceStateCall(getApplicationContext());
        phoneStateListener = serviceStateCall.getPhoneStateListener();
        telephonyManager = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            String statusActual = intent.getExtras().getString("event");
            if (statusActual.equals("Play")) {
                statusAudio = STATUS_PLAY;
                serviceNotification.showNotification("Stop");
                getApplicationContext().stopService(intentBackground);
            } else if(statusActual.equals("Stop")) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                statusAudio = STATUS_STOP;
                serviceNotification.showNotification("Load");
                serviceAudio = new ServiceAudio(getApplicationContext(), serviceNotification, config);
                serviceAudio.initMediaPlayer();
                Toast.makeText(getApplicationContext(), "Estableciendo conexion", Toast.LENGTH_SHORT).show();
            } else if(statusActual.equals("Close")) {
                statusAudio = STATUS_INIT;
                getApplicationContext().stopService(intentBackground);
            } else if (statusActual.equals("Volume")){
                volume = intent.getExtras().getFloat("volume");
                serviceAudio.setVolume(volume);
            }
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            serviceNotification.showNotification("Load");
            serviceAudio = new ServiceAudio(getApplicationContext(), serviceNotification, config);
            serviceAudio.initMediaPlayer();
            Toast.makeText(getApplicationContext(), "Estableciendo conexion", Toast.LENGTH_SHORT).show();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceAudio != null) serviceAudio.stopMediaPlayer();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        if(statusAudio == STATUS_INIT) {
            serviceNotification.hiddeNotification();
            serviceNotification = null;
        } else
            serviceNotification.showNotification("Stop");
    }
}
