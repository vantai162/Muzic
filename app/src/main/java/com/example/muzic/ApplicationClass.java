package com.example.muzic;


//import static com.example.muzic.activities.MusicOverviewActivity.convertPlayCount;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.support.v4.media.session.MediaSessionCompat;
import android.content.res.ColorStateList;
import android.content.Intent;

import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
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

@UnstableApi
public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "channel_1";
    public static final String CHANNEL_ID_2 = "channel_2";
    public static final String ACTION_NEXT = "com.example.muzic.NEXT";
    public static final String ACTION_PREV = "com.example.muzic.PREV";
    public static final String ACTION_PLAY = "com.example.muzic.PLAY";
    public static final MediaPlayerUtil mediaPlayerUtil = MediaPlayerUtil.getInstance();
    //public static TrackResponse CURRENT_TRACK = null;

    public static ExoPlayer player;  // Changed back to public static
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
    
    // Add new fields for playlist management
    public static TrackData currentTrack;
    public static ArrayList<TrackData> currentPlaylist = new ArrayList<>();
    public static int currentTrackIndex = -1;
    public static boolean isShuffleEnabled = false;
    public static int repeatMode = Player.REPEAT_MODE_OFF;

    private ColorStateList currentThemeColors;

    @Override
    public void onCreate() {
        super.onCreate();
        audioQualityManager = new AudioQualityManager(this);
        themeManager = new ThemeManager(this);
        
        // Initialize and apply theme first
        themeManager.applyTheme();
        updateThemeColors();
        
        // Initialize track selector for quality control
        trackSelector = new DefaultTrackSelector(this);
        
        // Initialize ExoPlayer with track selector
        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        
        // Initialize audio quality manager
        TRACK_QUALITY = audioQualityManager.getCurrentQuality();

        cacheManager = new CacheManager(this);
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

    public void setExoPlayer(ExoPlayer newPlayer) {
        player = newPlayer;
    }

    public AudioQualityManager getAudioQualityManager() {
        return audioQualityManager;
    }

    public void updateTrackQuality() {
        if (audioQualityManager != null) {
        audioQualityManager.updatePlayerQuality();
        }
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

    // Add new methods for playlist management
    public void updatePlaylist(ArrayList<TrackData> playlist, int position) {
        if (playlist == null || playlist.isEmpty() || position < 0 || position >= playlist.size()) {
            return;
        }
        
        // Update current playlist
        currentPlaylist.clear();
        currentPlaylist.addAll(playlist);
        
        // Update current track and queue
        TrackData selectedTrack = playlist.get(position);
        List<String> newQueue = new ArrayList<>();
        for (TrackData track : playlist) {
            newQueue.add(track.id);
        }
        
        updateCurrentTrack(selectedTrack, newQueue, position);
    }

    public void playNextTrack() {
        if (currentPlaylist != null && !currentPlaylist.isEmpty()) {
            if (currentTrackIndex < currentPlaylist.size() - 1) {
                currentTrackIndex++;
                currentTrack = currentPlaylist.get(currentTrackIndex);
                playCurrentTrack();
            } else if (player.getRepeatMode() == Player.REPEAT_MODE_ALL) {
                currentTrackIndex = 0;
                currentTrack = currentPlaylist.get(currentTrackIndex);
                playCurrentTrack();
            }
        }
    }

    public void playPreviousTrack() {
        if (currentPlaylist != null && !currentPlaylist.isEmpty()) {
            if (currentTrackIndex > 0) {
                currentTrackIndex--;
                currentTrack = currentPlaylist.get(currentTrackIndex);
                playCurrentTrack();
            } else if (player.getRepeatMode() == Player.REPEAT_MODE_ALL) {
                currentTrackIndex = currentPlaylist.size() - 1;
                currentTrack = currentPlaylist.get(currentTrackIndex);
                playCurrentTrack();
            }
        }
    }

    public void updateCurrentTrack(TrackData track, List<String> newQueue, int position) {
        currentTrack = track;
        if (newQueue != null) {
            trackQueue.clear();
            trackQueue.addAll(newQueue);
        }
        currentTrackIndex = position;
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
            // Start MusicService for notification
            startMusicService();
            
            // Get streaming URL using the correct format
            String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + currentTrack.id + "/stream";
            SONG_URL = streamUrl;
            
            // Prepare and play
            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(streamUrl));
            player.prepare();
            player.play();
        }
    }
    
    private void startMusicService() {
        Intent serviceIntent = new Intent(this, com.example.muzic.services.MusicService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    
    public boolean isPlayingTrack(String trackId) {
        return currentTrack != null && currentTrack.id.equals(trackId) && player != null && player.isPlaying();
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public void updateThemeColors() {
        if (themeManager != null) {
            currentThemeColors = themeManager.getColorStateList();
        }
    }

    public ColorStateList getCurrentThemeColors() {
        return currentThemeColors;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public void reapplyTheme() {
        if (themeManager != null) {
            themeManager.applyTheme();
            updateThemeColors();
        }
    }
}

