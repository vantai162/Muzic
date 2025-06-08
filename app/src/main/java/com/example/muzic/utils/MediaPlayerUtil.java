package com.example.muzic.utils;

import android.content.Context;
import android.net.Uri;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import com.example.muzic.ApplicationClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import androidx.media3.common.util.UnstableApi;

@UnstableApi
public class MediaPlayerUtil {
    private static MediaPlayerUtil instance;
    private Context context;
    private SettingsSharedPrefManager settingsManager;
    private CacheManager cacheManager;

    private MediaPlayerUtil() {
    }

    public static MediaPlayerUtil getInstance() {
        if (instance == null) {
            instance = new MediaPlayerUtil();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context;
        this.settingsManager = new SettingsSharedPrefManager(context);
        this.cacheManager = ((ApplicationClass) context.getApplicationContext()).getCacheManager();
    }

    public void playMusic(String musicId, byte[] musicData, String url) {
        ExoPlayer player = ((ApplicationClass) context.getApplicationContext()).getExoPlayer();
        
        // Kiểm tra cache trước
        byte[] cachedData = cacheManager.getCachedMusic(musicId);
        
        if (cachedData != null) {
            // Phát từ cache
            playFromCache(player, cachedData, musicId);
        } else {
            // Phát từ URL và cache nếu được bật
            playFromUrl(player, url, musicId, musicData);
        }
    }

    private void playFromCache(ExoPlayer player, byte[] musicData, String musicId) {
        try {
            // Tạo file tạm thời để ExoPlayer có thể phát
            File tempFile = new File(context.getCacheDir(), "temp_" + musicId);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(musicData);
            }
            
            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(tempFile));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
            
            // Xóa file tạm sau khi ExoPlayer đã load xong
            tempFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
            // Nếu phát từ cache thất bại, fallback về URL
            playFromUrl(player, ApplicationClass.SONG_URL, musicId, musicData);
        }
    }

    private void playFromUrl(ExoPlayer player, String url, String musicId, byte[] musicData) {
        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        // Cache nếu tính năng được bật
        if (settingsManager.getStoreInCache() && musicData != null) {
            cacheManager.cacheMusic(musicId, musicData);
        }
    }

    public byte[] getMusicData(String musicId) {
        return cacheManager.getCachedMusic(musicId);
    }
}
