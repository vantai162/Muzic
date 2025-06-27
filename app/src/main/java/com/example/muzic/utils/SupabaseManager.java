package com.example.muzic.utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.*;

public class SupabaseManager {
    private static final String SUPABASE_URL = "https://hcizrumrrgchmooqkrrp.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhjaXpydW1ycmdjaG1vb3FrcnJwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTEwMTI2MjAsImV4cCI6MjA2NjU4ODYyMH0.3uDZZL1IBt5XWh8k1_frBrmStGrhniEFleoTnZZOhXU";
    private static final String BUCKET_NAME = "profile-picture";

    public interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    public static void uploadImageToSupabase(Context context, Uri uri, OnUploadSuccessListener listener) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        String fullPath = "public/" + fileName;

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            byte[] bytes = readBytes(inputStream);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fullPath)
                    .addHeader("apikey", SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .addHeader("Content-Type", "image/jpeg")
                    .put(RequestBody.create(bytes, MediaType.parse("image/jpeg")))
                    .build();

            new Thread(() -> {
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fullPath;
                        ((Activity) context).runOnUiThread(() -> listener.onSuccess(publicUrl));
                    } else {
                        String errorMsg = response.body() != null ? response.body().string() : "No response body";
                        String fullError = "Upload failed: HTTP " + response.code() + " - " + errorMsg;
                        ((Activity) context).runOnUiThread(() -> listener.onFailure(fullError));
                    }
                } catch (Exception e) {
                    ((Activity) context).runOnUiThread(() -> listener.onFailure("Upload failed: " + e.getClass().getSimpleName() + ": " + e.getMessage()));
                }
            }).start();

        } catch (Exception e) {
            listener.onFailure("Upload failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static byte[] readBytes(InputStream inputStream) throws Exception {
        byte[] buffer = new byte[8192];
        int bytesRead;
        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        }
    }
}
