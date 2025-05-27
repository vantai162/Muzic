package com.example.muzic.utils;

import android.content.Context;
import android.util.LruCache;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheManager {
    private static final int CACHE_SIZE = 50 * 1024 * 1024; // 50MB cache
    private static final long MAX_CACHE_AGE = 7 * 24 * 60 * 60 * 1000; // 7 ngày
    private static final long MIN_FREE_SPACE = 500 * 1024 * 1024; // 500MB
    private final Context context;
    private final LruCache<String, byte[]> memoryCache;
    private final File cacheDir;
    private final ExecutorService executorService;

    public CacheManager(Context context) {
        this.context = context;
        this.cacheDir = new File(context.getCacheDir(), "music_cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        
        this.memoryCache = new LruCache<String, byte[]>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, byte[] value) {
                return value.length;
            }
        };
        
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Tự động kiểm tra và dọn cache khi khởi tạo
        cleanupCacheIfNeeded();
    }

    public void cacheMusic(String musicId, byte[] musicData) {
        if (musicData == null) return;
        
        executorService.execute(() -> {
            // Kiểm tra dung lượng trống trước khi cache
            if (shouldCleanupCache()) {
                cleanupCacheIfNeeded();
            }
            
            // Cache in memory
            memoryCache.put(musicId, musicData);
            
            // Cache to disk
            File cacheFile = new File(cacheDir, musicId);
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(musicData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean shouldCleanupCache() {
        // Kiểm tra dung lượng trống
        long freeSpace = cacheDir.getFreeSpace();
        return freeSpace < MIN_FREE_SPACE;
    }

    private void cleanupCacheIfNeeded() {
        executorService.execute(() -> {
            // 1. Xóa các file cũ hơn 7 ngày
            long currentTime = System.currentTimeMillis();
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (currentTime - file.lastModified() > MAX_CACHE_AGE) {
                        file.delete();
                    }
                }
            }

            // 2. Nếu vẫn thiếu dung lượng, xóa các file cũ nhất
            if (shouldCleanupCache()) {
                File[] remainingFiles = cacheDir.listFiles();
                if (remainingFiles != null) {
                    // Sắp xếp file theo thời gian truy cập
                    java.util.Arrays.sort(remainingFiles, (f1, f2) -> 
                        Long.compare(f1.lastModified(), f2.lastModified()));
                    
                    // Xóa 20% file cũ nhất
                    int filesToDelete = Math.max(1, remainingFiles.length / 5);
                    for (int i = 0; i < filesToDelete; i++) {
                        remainingFiles[i].delete();
                    }
                }
            }
        });
    }

    public byte[] getCachedMusic(String musicId) {
        // First try memory cache
        byte[] cachedData = memoryCache.get(musicId);
        if (cachedData != null) {
            return cachedData;
        }

        // Then try disk cache
        File cacheFile = new File(cacheDir, musicId);
        if (cacheFile.exists()) {
            try {
                return java.nio.file.Files.readAllBytes(cacheFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void clearCache() {
        memoryCache.evictAll();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    // Gọi khi tắt Store in Cache trong Settings
    public void onCacheDisabled() {
        clearCache();
    }
} 