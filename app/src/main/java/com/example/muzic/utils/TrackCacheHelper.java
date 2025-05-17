package com.example.muzic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TrackCacheHelper {
    private static final String TAG = "TrackCacheHelper";
    private final Context context;

    public TrackCacheHelper(Context context) {
        this.context = context;
    }

    private SharedPreferences getTrackPreference() {
        return context.getSharedPreferences("TrackCacheHelper", Context.MODE_PRIVATE);
    }

    public void setTrackToCache(String id, String uri) {
        if (getTrackPreference().contains(id)) return;
        getTrackPreference().edit().putString(id, uri).apply();
    }

    public boolean isTrackInCache(String id) {
        return getTrackPreference().contains(id);
    }

    public String getTrackFromCache(String id) {
        return getTrackPreference().getString(id, "");
    }

    public void copyFileToMusicDir(String sourcePath, String newFileName) {
        try {
            // Source file in cache directory
            File sourceFile = new File(sourcePath);

            // Destination directory in Music folder
            File musicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Melotune");
            if (!musicDir.exists()) {
                musicDir.mkdirs(); // Create the directory if it doesn't exist
            }

            // Destination file
            File destFile = new File(musicDir, newFileName + ".mp3");

            // Copy file
            try (FileInputStream inputStream = new FileInputStream(sourceFile);
                 FileOutputStream outputStream = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            Log.d("FileCopy", "File copied successfully to: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "copyFileToMusicDir: ", e);
        }
    }
}
