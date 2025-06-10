package com.example.muzic.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.muzic.ApplicationClass;

@UnstableApi
public class NotificationReceiver extends BroadcastReceiver {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(Context context, Intent intent) {
        ApplicationClass app = (ApplicationClass) context.getApplicationContext();
        ExoPlayer player = app.getExoPlayer();
        String actionName = intent.getAction();

        if (actionName != null) {
            // Execute action on main thread to avoid any synchronization issues
            mainHandler.post(() -> {
                switch (actionName) {
                    case ApplicationClass.ACTION_PLAY:
                        if (player.isPlaying()) {
                            player.pause();
                        } else {
                            player.play();
                        }
                        break;

                    case ApplicationClass.ACTION_PREV:
                        app.playPreviousTrack();
                        break;

                    case ApplicationClass.ACTION_NEXT:
                        app.playNextTrack();
                        break;
                }
            });

            // Start service to update notification
            Intent serviceIntent = new Intent(context, MusicService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
} 