package com.example.radiosantidad1025fm.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.radiosantidad1025fm.Configs.Config;
import com.example.radiosantidad1025fm.R;

import java.io.IOException;

public class ServiceAudio extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private final int STATUS_INIT = 3;
    private final int STATUS_ERROR = 2;
    private final int STATUS_PLAY = 1;
    private final int STATUS_STOP = 0;
    private int statusAudio;
    protected MediaPlayer mediaPlayer;
    private Context context;
    private Config config;
    private ImageButton buttonPlayStop;
    private float volume;
    private Intent intent;

    public ServiceAudio() { }

    public ServiceAudio(Context context, Config config, ImageButton buttonPlayStop) {
        this.context = context;
        this.config = config;
        this.buttonPlayStop = buttonPlayStop;
        this.statusAudio = STATUS_INIT;
        this.volume = 0;
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
        intent = new Intent(context, this.getClass());
        Toast.makeText(context, "Estableciendo conexion.......", Toast.LENGTH_SHORT).show();
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(config.getUrlSound());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepareAsync();
            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Fallo al con el servidor de radio init", Toast.LENGTH_SHORT).show();
        }
    }

    public void changeVolume(int progress) {
        volume = (float) progress/100;
        mediaPlayer.setVolume(volume, volume);
    }

    public void toggleAudio() {
        if (statusAudio == STATUS_INIT)
            Toast.makeText(context, "Cargando audio.......", Toast.LENGTH_SHORT).show();
        else if(statusAudio == STATUS_ERROR) {
            Toast.makeText(context, "Servidor sin conexion, intente mas tarde", Toast.LENGTH_LONG).show();
            buttonPlayStop.setImageResource(R.drawable.ic_baseline_play_circle_filled_55);
            mediaPlayer.reset();
            statusAudio = STATUS_STOP;
        } else if (statusAudio == STATUS_STOP) {
            Toast.makeText(context, "Preparando para reproducir.....", Toast.LENGTH_SHORT).show();
            initMediaPlayer();
            statusAudio = STATUS_INIT;
        } else if (statusAudio == STATUS_PLAY) {
            statusAudio = STATUS_STOP;
            context.stopService(intent);
            buttonPlayStop.setImageResource(R.drawable.ic_baseline_play_circle_filled_55);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        buttonPlayStop.setImageResource(R.drawable.ic_baseline_play_circle_filled_55);
        Toast.makeText(context, "Sin conexion con el servidor", Toast.LENGTH_SHORT).show();
        statusAudio = STATUS_ERROR;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Toast.makeText(context, "Iniciando.....", Toast.LENGTH_SHORT).show();
        buttonPlayStop.setImageResource(R.drawable.ic_baseline_stop_circle_55);
        context.startService(intent);
        statusAudio = STATUS_PLAY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    /*private class BackgroundAudio extends Service {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            mediaPlayer.start();
            return START_STICKY;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }*/
}
