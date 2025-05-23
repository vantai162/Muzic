package com.example.muzic.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import androidx.media3.common.C;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.common.TrackSelectionParameters;

import com.example.muzic.ApplicationClass;

public class AudioQualityManager {
    private final SettingsSharedPrefManager settingsManager;
    private final Context context;
    private final ExoPlayer player;

    public static final String QUALITY_HIGH = "320kbps";
    public static final String QUALITY_MEDIUM = "192kbps";
    public static final String QUALITY_LOW = "128kbps";

    // Bitrate constraints for different quality levels
    private static final int HIGH_BITRATE = 320 * 1024;  // 320kbps
    private static final int MEDIUM_BITRATE = 192 * 1024;  // 192kbps
    private static final int LOW_BITRATE = 128 * 1024;  // 128kbps

    public AudioQualityManager(Context context) {
        this.context = context;
        this.settingsManager = new SettingsSharedPrefManager(context);
        this.player = ((ApplicationClass) context.getApplicationContext()).getExoPlayer();
        
        // Initialize player with quality settings
        updatePlayerQuality();
    }

    public String getCurrentQuality() {
        boolean isHighQualityEnabled = settingsManager.getHighQualityTrack();
        
        if (!isHighQualityEnabled) {
            return QUALITY_LOW;
        }

        // Check if we're on WiFi
        if (isWifiConnection()) {
            return QUALITY_HIGH;
        }

        // If high quality is enabled but we're on cellular
        boolean allowCellular = settingsManager.getDownloadOverCellular();
        if (allowCellular) {
            return QUALITY_HIGH;
        } else {
            return QUALITY_MEDIUM;
        }
    }

    private boolean isWifiConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        }
        return false;
    }

    public boolean shouldUseCellular() {
        return settingsManager.getDownloadOverCellular();
    }

    public boolean isHighQualityEnabled() {
        return settingsManager.getHighQualityTrack();
    }

    public void setHighQuality(boolean enabled) {
        settingsManager.setHighQualityTrack(enabled);
        updatePlayerQuality();
    }

    public void updatePlayerQuality() {
        if (player == null) return;

        String quality = getCurrentQuality();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
        TrackSelectionParameters.Builder parametersBuilder = player.getTrackSelectionParameters().buildUpon();
        
        // Set maximum bitrate based on quality setting
        int maxBitrate;
        switch (quality) {
            case QUALITY_HIGH:
                maxBitrate = HIGH_BITRATE;
                break;
            case QUALITY_MEDIUM:
                maxBitrate = MEDIUM_BITRATE;
                break;
            case QUALITY_LOW:
            default:
                maxBitrate = LOW_BITRATE;
                break;
        }

        // Apply constraints to audio tracks
        trackSelector.setParameters(trackSelector.buildUponParameters()
                .setMaxAudioBitrate(maxBitrate)
                .setForceHighestSupportedBitrate(quality.equals(QUALITY_HIGH))
                .setConstrainAudioChannelCountToDeviceCapabilities(true));

        // Update player configuration
        player.setTrackSelectionParameters(parametersBuilder.build());
    }
} 