/*package com.example.muzic;

package com.harsh.shah.saavnmp3;

import static com.harsh.shah.saavnmp3.activities.MusicOverviewActivity.convertPlayCount;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.media.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.ExoDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.FileDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.CacheEvictor;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.harsh.shah.saavnmp3.activities.MusicOverviewActivity;
import com.harsh.shah.saavnmp3.activities.SettingsActivity;
import com.harsh.shah.saavnmp3.network.ApiManager;
import com.harsh.shah.saavnmp3.network.TrackManager;
import com.harsh.shah.saavnmp3.network.utility.RequestNetwork;
import com.harsh.shah.saavnmp3.records.SongResponse;
import com.harsh.shah.saavnmp3.services.NotificationReceiver;
import com.harsh.shah.saavnmp3.utils.MediaPlayerUtil;
import com.harsh.shah.saavnmp3.utils.SharedPreferenceManager;
import com.harsh.shah.saavnmp3.utils.TrackCacheHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID_1 = "channel_1";
    public static final String CHANNEL_ID_2 = "channel_2";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PREV = "prev";
    public static final String ACTION_PLAY = "play";
    public static final MediaPlayerUtil mediaPlayerUtil = MediaPlayerUtil.getInstance();
    public static SongResponse CURRENT_TRACK = null;

    public static ExoPlayer player;
    public static String TRACK_QUALITY = "320kbps";
    public static boolean isTrackDownloaded = false;
    private MediaSessionCompat mediaSession;
    public static List<String> trackQueue = new ArrayList<>();
    public static String MUSIC_TITLE = "";
    public static String MUSIC_DESCRIPTION = "";
    public static String IMAGE_URL = "";
    public static String MUSIC_ID = "";
    public static String SONG_URL = "";
    public static int track_position = -1;
    public static SharedPreferenceManager sharedPreferenceManager;
    private final String TAG = "ApplicationClass";
    public static int IMAGE_BG_COLOR = Color.argb(255, 25, 20, 20);
    public static int TEXT_ON_IMAGE_COLOR = IMAGE_BG_COLOR ^ 0x00FFFFFF;
    public static int TEXT_ON_IMAGE_COLOR1 = IMAGE_BG_COLOR ^ 0x00FFFFFF;
    private static Activity currentActivity = null;

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public static void setTrackQuality(String string) {
        TRACK_QUALITY = string;
        sharedPreferenceManager.setTrackQuality(string);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();

        File cacheDir = new File(getCacheDir(), "audio_cache");

        // Step 2: Set up SimpleCache
        DatabaseProvider databaseProvider = new ExoDatabaseProvider(this);
        long cacheSize = 100 * 1024 * 1024; // 100 MB
        CacheEvictor cacheEvictor = new LeastRecentlyUsedCacheEvictor(cacheSize);
        final SimpleCache simpleCache = new SimpleCache(cacheDir, cacheEvictor, databaseProvider);

        // Step 3: Create CacheDataSourceFactory
        DataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent(Util.getUserAgent(this, "AudioCachingApp"));
        CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(cacheDataSourceFactory))
                .build();
        mediaSession = new MediaSessionCompat(this, "ApplicationClass");
        mediaSession.setActive(true);
        createNotificationChannel();
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        TRACK_QUALITY = sharedPreferenceManager.getTrackQuality();

    }

    public static void updateTheme() {
        SettingsActivity.SettingsSharedPrefManager settingsSharedPrefManager = new SettingsActivity.SettingsSharedPrefManager(getCurrentActivity());
        final String theme = settingsSharedPrefManager.getTheme();
        AppCompatDelegate.setDefaultNightMode(theme.equals("dark") ? AppCompatDelegate.MODE_NIGHT_YES : theme.equals("light") ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel1 = new NotificationChannel(CHANNEL_ID_1, "Media Controls", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel1.setDescription("Notifications for media playback");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel1);
        }
    }

    public void setMusicDetails(String image, String title, String description, String id) {
        if (image != null) IMAGE_URL = image;
        if (title != null) MUSIC_TITLE = title;
        if (description != null) MUSIC_DESCRIPTION = description;
        MUSIC_ID = id;
        Log.i(TAG, "setMusicDetails: " + MUSIC_TITLE + "\t" + MUSIC_ID);
    }

    public void setSongUrl(String songUrl) {
        SONG_URL = songUrl;
    }

    public void setTrackQueue(List<String> que) {
        track_position = -1;
        trackQueue = que;
    }

    public List<String> getTrackQueue() {
        return trackQueue;
    }

    public void showNotification(int playPauseButton) {
        try {

            Log.i(TAG, "showNotification: " + MUSIC_TITLE + "\t" + MUSIC_ID);

            int reqCode = MUSIC_ID.hashCode();

            Intent intent = new Intent(this, MusicOverviewActivity.class);
            intent.putExtra("id", MUSIC_ID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent contentIntent = PendingIntent.getActivity(this, reqCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
            PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);

            Intent playIntent = new Intent(this, NotificationReceiver.class).setAction(ApplicationClass.ACTION_PLAY);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);

            Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ApplicationClass.ACTION_NEXT);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);

            Glide.with(this)
                    .asBitmap()
                    .load(IMAGE_URL)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            //IMAGE_BG_COLOR = calculateDominantColor(resource);
                            //TEXT_ON_IMAGE_COLOR = invertColor(IMAGE_BG_COLOR);

                            Palette.from(resource)
                                    .generate(palette -> {
                                        Palette.Swatch textSwatch = palette.getDominantSwatch();
                                        if (textSwatch == null) {
                                            Log.i("ApplicationClass", "Null swatch :(");
                                            return;
                                        }
                                        IMAGE_BG_COLOR = (textSwatch.getRgb());
                                        TEXT_ON_IMAGE_COLOR = (textSwatch.getTitleTextColor());
                                        TEXT_ON_IMAGE_COLOR1 = (textSwatch.getBodyTextColor());
                                    });

                            Notification notification = new androidx.core.app.NotificationCompat.Builder(ApplicationClass.this, CHANNEL_ID_1)
                                    .setSmallIcon(R.drawable.headphone)
                                    .setLargeIcon(resource)
                                    .setContentTitle(MUSIC_TITLE)
                                    .setOngoing(playPauseButton != R.drawable.play_arrow_24px)
                                    .setContentText(MUSIC_DESCRIPTION)
                                    .setStyle(new NotificationCompat.MediaStyle()
                                            .setMediaSession(mediaSession.getSessionToken())
                                            .setShowActionsInCompactView(0, 1, 2))
//                                    .addAction(R.drawable.skip_previous_24px, "prev", prevPendingIntent)
//                                    .addAction(playPauseButton, "play", playPendingIntent)
//                                    .addAction(R.drawable.skip_next_24px, "next", nextPendingIntent)
                                    .addAction(new androidx.core.app.NotificationCompat.Action(R.drawable.skip_previous_24px, "prev", prevPendingIntent))
                                    .addAction(new androidx.core.app.NotificationCompat.Action(playPauseButton, "play", playPendingIntent))
                                    .addAction(new androidx.core.app.NotificationCompat.Action(R.drawable.skip_next_24px, "next", nextPendingIntent))
                                    .setPriority(Notification.PRIORITY_DEFAULT)
                                    .setContentIntent(contentIntent)
                                    .setOnlyAlertOnce(true)
                                    .build();

                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.notify(0, notification);

                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // Handle placeholder if needed
                        }
                    });

        } catch (Exception e) {
            Log.e("ApplicationClass", "showNotification: ", e);
        }
    }

    public static void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getCurrentActivity().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    public void togglePlayPause() {
//        if (mediaPlayerUtil.isPlaying()) {
//            mediaPlayerUtil.pause();
//        } else {
//            mediaPlayerUtil.start();
//        }
        if (player.isPlaying())
            player.pause();
        else
            player.play();

        showNotification();
    }

    public void nextTrack() {
        if (!trackQueue.isEmpty() && track_position < trackQueue.size() - 1) {
            if (player.getShuffleModeEnabled())
                track_position = (int) (Math.random() * trackQueue.size());
            else
                track_position++;
            MUSIC_ID = trackQueue.get(track_position);
            playTrack();
            //startActivity(new Intent(this, MusicOverviewActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("id", MUSIC_ID));
        }
        showNotification();
    }

    public void prevTrack() {
        if (track_position > 0) {
            if (player.getShuffleModeEnabled())
                track_position = (int) (Math.random() * trackQueue.size());
            else
                track_position--;
            MUSIC_ID = trackQueue.get(track_position);
            playTrack();
            //startActivity(new Intent(this, MusicOverviewActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("id", MUSIC_ID));
        }
        showNotification();
    }

    @UnstableApi
    public void prepareMediaPlayer() {
        try {
            MediaItem mediaItem = MediaItem.fromUri(SONG_URL);
            isTrackDownloaded = false;
            final TrackCacheHelper trackCacheHelper = new TrackCacheHelper(currentActivity);
            if (trackCacheHelper.isTrackInCache(MUSIC_ID)) {
                isTrackDownloaded = true;
                mediaItem = MediaItem.fromUri(Uri.parse("file://" + trackCacheHelper.getTrackFromCache(MUSIC_ID)));

                ProgressiveMediaSource.Factory mediaSourceFactory = new ProgressiveMediaSource.Factory(FileDataSource::new);
                ProgressiveMediaSource mediaSource = mediaSourceFactory.createMediaSource(mediaItem);

                // Prepare and play the media
                player.setMediaSource(mediaSource);
            }

            if (!trackCacheHelper.isTrackInCache(MUSIC_ID)) {
                if (new SettingsActivity.SettingsSharedPrefManager(currentActivity).getStoreInCache()) {
                    new TrackManager(
                            currentActivity,
                            SONG_URL,
                            MUSIC_TITLE,
                            MUSIC_ID,
                            IMAGE_URL,
                            true
                    ).execute();
                }
                player.setMediaItem(mediaItem);
            }

            player.prepare();
            player.play();
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    showNotification();
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
                    if (playbackState == Player.STATE_ENDED)
                        nextTrack();
                }
            });

//            try {
//                mediaPlayerUtil.reset();
//            } catch (Exception ignored) {
//            }
//            mediaPlayerUtil.setDataSource(SONG_URL);
//            mediaPlayerUtil.prepare();
//            mediaPlayerUtil.start();
        } catch (Exception e) {
            Log.e(TAG, "prepareMediaPlayer: ", e);
        }
    }


    private void playTrack() {
        ApiManager apiManager = new ApiManager(currentActivity);
        apiManager.retrieveSongById(MUSIC_ID, null, new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                SongResponse songResponse = new Gson().fromJson(response, SongResponse.class);
                if (songResponse.success()) {
                    MUSIC_TITLE = (songResponse.data().get(0).name());
                    MUSIC_DESCRIPTION = (
                            String.format("%s plays | %s | %s",
                                    convertPlayCount(songResponse.data().get(0).playCount()),
                                    songResponse.data().get(0).year(),
                                    songResponse.data().get(0).copyright())
                    );
                    List<SongResponse.Image> image = songResponse.data().get(0).image();
                    IMAGE_URL = image.get(image.size() - 1).url();

                    List<SongResponse.DownloadUrl> downloadUrls = songResponse.data().get(0).downloadUrl();

                    SONG_URL = getDownloadUrl(downloadUrls);
                    setMusicDetails(IMAGE_URL, MUSIC_TITLE, MUSIC_DESCRIPTION, MUSIC_ID);
                    prepareMediaPlayer();
                    //showNotification();
                }
            }

            @Override
            public void onErrorResponse(String tag, String message) {

            }
        });
    }

    public static String getDownloadUrl(List<SongResponse.DownloadUrl> downloadUrlList) {
        if (downloadUrlList.isEmpty()) return "";
        for (SongResponse.DownloadUrl downloadUrl : downloadUrlList) {
            if (downloadUrl.quality().equals(TRACK_QUALITY))
                return downloadUrl.url();
        }

        return downloadUrlList.get(downloadUrlList.size() - 1).url();
    }

    public void showNotification() {
        //showNotification(mediaPlayerUtil.isPlaying() ? R.drawable.baseline_pause_24 : R.drawable.play_arrow_24px);
        showNotification(player.isPlaying() ? R.drawable.baseline_pause_24 : R.drawable.play_arrow_24px);
    }

    private int invertColor(int color) {
        return (color ^ 0x00FFFFFF);
    }

    int calculateDominantColor(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;

        for (int pixel : pixels) {
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            redSum += red;
            greenSum += green;
            blueSum += blue;
        }

        int dominantRed = redSum / pixels.length;
        int dominantGreen = greenSum / pixels.length;
        int dominantBlue = blueSum / pixels.length;

        return Color.argb(255, dominantRed, dominantGreen, dominantBlue);
    }

}*/

