package com.example.muzic;


//import static com.example.muzic.activities.MusicOverviewActivity.convertPlayCount;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

//import com.example.muzic.activities.MusicOverviewActivity;
//import com.example.muzic.activities.SettingsActivity;
//import com.example.muzic.network.TrackManager;
//import com.example.muzic.services.NotificationReceiver;
import com.example.muzic.utils.MediaPlayerUtil;
import com.example.muzic.utils.ThemeManager;
import com.example.muzic.utils.AudioQualityManager;
import com.example.muzic.utils.CacheManager;
//import com.example.muzic.utils.SharedPreferenceManager;
//import com.example.muzic.utils.TrackCacheHelper;
import com.example.muzic.model.TrackData;

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
    private AudioQualityManager audioQualityManager;
    private DefaultTrackSelector trackSelector;
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
    private ThemeManager themeManager;
    private CacheManager cacheManager;
    public static TrackData currentTrack;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize theme
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        
        // Initialize track selector for quality control
        trackSelector = new DefaultTrackSelector(this);
        
        // Initialize ExoPlayer with track selector
        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        
        // Initialize audio quality manager
        audioQualityManager = new AudioQualityManager(this);
        TRACK_QUALITY = audioQualityManager.getCurrentQuality();

        cacheManager = new CacheManager(this);
    }

    public AudioQualityManager getAudioQualityManager() {
        if (audioQualityManager == null) {
            audioQualityManager = new AudioQualityManager(this);
        }
        return audioQualityManager;
    }

    public ExoPlayer getExoPlayer() {
        if (player == null) {
            trackSelector = new DefaultTrackSelector(this);
            player = new ExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();
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

    public void updateTrackQuality() {
        TRACK_QUALITY = audioQualityManager.getCurrentQuality();
        audioQualityManager.updatePlayerQuality();
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    // Add new methods for track management
    public void updateCurrentTrack(TrackData track, List<String> newQueue, int position) {
        currentTrack = track;
        if (newQueue != null) {
            trackQueue.clear();
            trackQueue.addAll(newQueue);
        }
        track_position = position;
        MUSIC_ID = track.id;
        MUSIC_TITLE = track.title;
        MUSIC_DESCRIPTION = track.user.name;
        IMAGE_URL = track.artwork != null ? track.artwork._480x480 : "";
        
        // Stop current playback before changing track
        if (player != null) {
            player.stop();
            player.clearMediaItems();
        }
    }
    
    public void playCurrentTrack() {
        if (currentTrack != null && player != null) {
            // Get streaming URL based on track_cid
            String streamUrl = "https://audius.co/tracks/" + currentTrack.track_cid;
            SONG_URL = streamUrl;
            
            // Prepare and play
            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(streamUrl));
            player.prepare();
            player.play();
        }
    }
    
    public boolean isPlayingTrack(String trackId) {
        return currentTrack != null && currentTrack.id.equals(trackId) && player != null && player.isPlaying();
    }
}

