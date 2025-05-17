package com.example.muzic.utils;

import android.media.MediaPlayer;

public class MediaPlayerUtil extends MediaPlayer {
    private static MediaPlayerUtil instance;

    private final String TAG = "MediaPlayerUtil";

    public static MediaPlayerUtil getInstance() {
        if (instance != null) return instance;
        return new MediaPlayerUtil();
    }

    private MediaPlayerUtil() {
    }

}
