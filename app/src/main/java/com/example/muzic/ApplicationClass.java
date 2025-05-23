package com.example.muzic;


//import static com.example.muzic.activities.MusicOverviewActivity.convertPlayCount;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.media3.exoplayer.ExoPlayer;

//import com.example.muzic.activities.MusicOverviewActivity;
//import com.example.muzic.activities.SettingsActivity;
//import com.example.muzic.network.TrackManager;
//import com.example.muzic.services.NotificationReceiver;
import com.example.muzic.utils.MediaPlayerUtil;
//import com.example.muzic.utils.SharedPreferenceManager;
//import com.example.muzic.utils.TrackCacheHelper;

import java.util.ArrayList;
import java.util.List;

public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "channel_1";
    public static final String CHANNEL_ID_2 = "channel_2";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PREV = "prev";
    public static final String ACTION_PLAY = "play";
    public static final MediaPlayerUtil mediaPlayerUtil = MediaPlayerUtil.getInstance();
    //public static TrackResponse CURRENT_TRACK = null;

    public static ExoPlayer player;
    public static String TRACK_QUALITY = "320kbps";
    public static boolean isTrackDownloaded = false;
    private MediaSessionCompat mediaSession;
    public static List<String> trackQueue = new ArrayList<>();
    public static String MUSIC_TITLE = "";
    public static String MUSIC_DESCRIPTION = "";
    public static String IMAGE_URL = "";
    public static String MUSIC_ID = null;
    public static String SONG_URL = "";
    public static int track_position = -1;
    //public static SharedPreferenceManager sharedPreferenceManager;
    private final String TAG = "ApplicationClass";
    public static int IMAGE_BG_COLOR = Color.argb(255, 25, 20, 20);
    public static int TEXT_ON_IMAGE_COLOR = IMAGE_BG_COLOR ^ 0x00FFFFFF;
    public static int TEXT_ON_IMAGE_COLOR1 = IMAGE_BG_COLOR ^ 0x00FFFFFF;
    private static Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
    }

    public ExoPlayer getExoPlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
        }
        return player;
    }

    public void setExoPlayer(ExoPlayer exoPlayer) {
        player = exoPlayer;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}

