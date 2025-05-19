package com.example.muzic;


//import static com.example.muzic.activities.MusicOverviewActivity.convertPlayCount;

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
import com.example.muzic.records.TrackResponse;
import com.google.gson.Gson;
//import com.example.muzic.activities.MusicOverviewActivity;
//import com.example.muzic.activities.SettingsActivity;
import com.example.muzic.network.ApiManager;
//import com.example.muzic.network.TrackManager;
import com.example.muzic.network.utility.RequestNetwork;
//import com.example.muzic.services.NotificationReceiver;
import com.example.muzic.utils.MediaPlayerUtil;
//import com.example.muzic.utils.SharedPreferenceManager;
//import com.example.muzic.utils.TrackCacheHelper;

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
    public static TrackResponse CURRENT_TRACK = null;

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
    //public static SharedPreferenceManager sharedPreferenceManager;
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

    

}

