package com.example.muzic.player;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.muzic.object.Song;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import java.io.File;

public class PlayerManager {
    private static ExoPlayer exoPlayer;
    private static Player.Listener listener;
    private static Song currentSong;
    private static PlayerUIListener uiListener;
    // Interface để cập nhật UI từ các fragment (MiniPlayer, BottomSheet, ...)
    public interface PlayerUIListener {
        void onPlayStateChanged(boolean isPlaying);
        void onTrackChanged(Song song);
    }

    // Gán listener từ UI
    public static void setPlayerUIListener(PlayerUIListener listener) {
        uiListener = listener;
    }
    public static ExoPlayer getPlayer(Context context) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
        }
        // Lắng nghe trạng thái phát
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                notifyPlayStateChanged(isPlaying);
            }
        });
        return exoPlayer;
    }

    // Phát một bài hát
    public static void play(Context context, Song song) {
        release(); // Dọn dẹp trước khi phát bài mới
        getPlayer(context);

        String url = song.getYoutubeUrl();
        MediaItem mediaItem;

        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "URL bài hát rỗng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (url.startsWith("http")) {
            // Online stream
            mediaItem = MediaItem.fromUri(Uri.parse(url));

        } else if (url.startsWith("rawresource://")) {
            // Phát từ res/raw
            String resourceName = url.replace("rawresource://", "");
            int resId = context.getResources().getIdentifier(resourceName, "raw", context.getPackageName());
            if (resId == 0) {
                Toast.makeText(context, "Không tìm thấy tài nguyên trong res/raw", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri rawUri = RawResourceDataSource.buildRawResourceUri(resId);
            mediaItem = MediaItem.fromUri(rawUri);

        } else if (url.startsWith("/") || url.startsWith("file://")) {
            // Phát từ file cục bộ
            Uri fileUri = url.startsWith("file://") ? Uri.parse(url) : Uri.fromFile(new File(url));
            mediaItem = MediaItem.fromUri(fileUri);

        } else {
            Toast.makeText(context, "Không hỗ trợ định dạng URL này!", Toast.LENGTH_SHORT).show();
            return;
        }

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        currentSong = song;
        notifyTrackChanged(song);
        notifyPlayStateChanged(true);
    }


    public static void pause() {
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.pause();
            if (uiListener != null) uiListener.onPlayStateChanged(false);
        }
    }

    public static void resume() {
        if (exoPlayer != null && !exoPlayer.isPlaying()) {
            exoPlayer.play();
            if (uiListener != null) uiListener.onPlayStateChanged(true);
        }
    }

    public static void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        currentSong = null;
    }

    public static boolean isPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }
    public static void seekTo(long positionMs) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(positionMs);
        }
    }
    public static void setListener(Player.Listener newListener) {
        listener = newListener;
        if (exoPlayer != null) {
            exoPlayer.addListener(listener);
        }
    }
    public static long getCurrentPosition() {
        return exoPlayer != null ? exoPlayer.getCurrentPosition() : 0;
    }

    public static long getDuration() {
        return exoPlayer != null ? exoPlayer.getDuration() : 0;
    }
    public static Song getCurrentSong() {
        return currentSong;
    }

    // --- Các hàm thông báo cho UI fragment ---
    private static void notifyPlayStateChanged(boolean isPlaying) {
        if (uiListener != null) {
            uiListener.onPlayStateChanged(isPlaying);
        }
    }

    private static void notifyTrackChanged(Song song) {
        if (uiListener != null) {
            uiListener.onTrackChanged(song);
        }
    }
}
