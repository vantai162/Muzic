package com.example.muzic.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.activities.MainActivity;
import com.example.muzic.model.TrackData;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UnstableApi
public class MusicService extends Service {

    private MediaSessionCompat mediaSession;
    private ExoPlayer player;
    private final IBinder binder = new MusicBinder();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Get ExoPlayer instance from ApplicationClass
        player = ((ApplicationClass) getApplication()).getExoPlayer();
        
        // Initialize MediaSession
        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setActive(true);
        
        // Add player listener
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                updatePlaybackState();
                updateNotificationImmediate();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlaybackState();
                updateNotificationImmediate();
            }
        });
        
        // Create notification channel for Android O and above
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                ApplicationClass.CHANNEL_ID_1,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Controls for the music player");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void updatePlaybackState() {
        long actions = PlaybackStateCompat.ACTION_PLAY |
                      PlaybackStateCompat.ACTION_PAUSE |
                      PlaybackStateCompat.ACTION_PLAY_PAUSE |
                      PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                      PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                      PlaybackStateCompat.ACTION_STOP |
                      PlaybackStateCompat.ACTION_SEEK_TO;

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(player.isPlaying() ? 
                     PlaybackStateCompat.STATE_PLAYING : 
                     PlaybackStateCompat.STATE_PAUSED,
                     player.getCurrentPosition(),
                     1.0f);
        
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void updateNotification() {
        TrackData currentTrack = ApplicationClass.currentTrack;
        if (currentTrack == null) return;

        // Create pending intent for notification click
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE);

        // Create notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_1)
            .setSmallIcon(R.drawable.music_note_24px)
            .setContentTitle(currentTrack.title)
            .setContentText(currentTrack.user != null ? currentTrack.user.name : "")
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false);

        // Add media controls
        builder.addAction(R.drawable.skip_previous_24px, "Previous", 
            getPendingIntent(ApplicationClass.ACTION_PREV));
        builder.addAction(player.isPlaying() ? 
            R.drawable.baseline_pause_24 : R.drawable.play_arrow_24px,
            player.isPlaying() ? "Pause" : "Play",
            getPendingIntent(ApplicationClass.ACTION_PLAY));
        builder.addAction(R.drawable.skip_next_24px, "Next", 
            getPendingIntent(ApplicationClass.ACTION_NEXT));

        // Set media style
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = 
            new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2);
        builder.setStyle(mediaStyle);

        // Start with basic notification
        startForeground(1, builder.build());

        // Load artwork in background
        if (currentTrack.artwork != null && currentTrack.artwork._480x480 != null) {
            executorService.execute(() -> {
                try {
                    Bitmap artwork = Picasso.get()
                        .load(currentTrack.artwork._480x480)
                        .get();

                    // Update notification with artwork on main thread
                    mainHandler.post(() -> {
                        builder.setLargeIcon(artwork);

                        // Update media metadata
                        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.title)
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, 
                                currentTrack.user != null ? currentTrack.user.name : "")
                            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork);
                        mediaSession.setMetadata(metadataBuilder.build());

                        // Update notification
                        NotificationManager notificationManager = 
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(1, builder.build());
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, NotificationReceiver.class)
            .setAction(action)
            .setPackage(getPackageName()); // Add package name to make intent explicit
            
        int requestCode;
        switch (action) {
            case ApplicationClass.ACTION_PREV:
                requestCode = 0;
                break;
            case ApplicationClass.ACTION_PLAY:
                requestCode = 1;
                break;
            case ApplicationClass.ACTION_NEXT:
                requestCode = 2;
                break;
            default:
                requestCode = -1;
                break;
        }

        // Use FLAG_CANCEL_CURRENT to ensure we get a new PendingIntent each time
        return PendingIntent.getBroadcast(this, requestCode, intent, 
            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Luôn cập nhật notification khi service được start
        updateNotificationImmediate();
        return START_NOT_STICKY;
    }

    private void updateNotificationImmediate() {
        TrackData currentTrack = ApplicationClass.currentTrack;
        if (currentTrack == null) return;

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);

        // Set title & artist
        notificationLayout.setTextViewText(R.id.notification_title, currentTrack.title);
        notificationLayout.setTextViewText(R.id.notification_artist, currentTrack.user != null ? currentTrack.user.name : "");

        // Set play/pause icon
        int playPauseIcon = player.isPlaying() ? R.drawable.baseline_pause_24 : R.drawable.play_arrow_24px;
        notificationLayout.setImageViewResource(R.id.notification_play_pause, playPauseIcon);

        // Set click actions
        notificationLayout.setOnClickPendingIntent(R.id.notification_prev, getPendingIntent(ApplicationClass.ACTION_PREV));
        notificationLayout.setOnClickPendingIntent(R.id.notification_play_pause, getPendingIntent(ApplicationClass.ACTION_PLAY));
        notificationLayout.setOnClickPendingIntent(R.id.notification_next, getPendingIntent(ApplicationClass.ACTION_NEXT));

        // Set default album art (trước khi load ảnh)
        notificationLayout.setImageViewResource(R.id.notification_album_art, R.drawable.ic_launcher_foreground);

        // Tạo notification cơ bản và startForeground NGAY LẬP TỨC
        Intent intent = new Intent(this, MainActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_1)
            .setSmallIcon(R.drawable.music_note_24px)
            .setContentIntent(contentIntent)
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setOngoing(true)
            .setShowWhen(false)
            .build();

        // LUÔN gọi startForeground NGAY LẬP TỨC
        startForeground(1, notification);

        // Sau đó mới load ảnh và update notification nếu có ảnh
        if (currentTrack.artwork != null && currentTrack.artwork._480x480 != null) {
            executorService.execute(() -> {
                try {
                    Bitmap artwork = Picasso.get().load(currentTrack.artwork._480x480).get();
                    mainHandler.post(() -> {
                        notificationLayout.setImageViewBitmap(R.id.notification_album_art, artwork);
                        NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(1, notification);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        if (player != null) {
            player.release();
        }
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
    }
} 