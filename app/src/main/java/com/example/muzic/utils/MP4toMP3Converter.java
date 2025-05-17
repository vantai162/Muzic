package com.example.muzic.utils;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

public class MP4toMP3Converter {
    private static final String TAG = "MP4toMP3Converter";

    public static void convertToMP3WithMetadata(Context context, String inputFilePath, String title, String artist, String album) {
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.e(TAG, "onFailure: " + "FAILED TO LOAD FFMPEG BINARY");
                }

                @Override
                public void onSuccess() {

                    File ffmpegBinary = new File(context.getFilesDir(), "ffmpeg");
                    if (ffmpegBinary.exists()) {
                        if (!ffmpegBinary.setExecutable(true)) {
                            Log.e(TAG, "Failed to make ffmpeg executable.");
                        } else {
                            Log.d(TAG, "FFmpeg binary is now executable.");
                        }
                    } else {
                        Log.e(TAG, "FFmpeg binary not found.");
                    }

                    File tempCoverFile = new File(context.getCacheDir(), "cover_image.jpg");

                    String[] cmd = new String[]{
                            "-i", inputFilePath,                 // Input video/audio file
                            "-i", tempCoverFile.getAbsolutePath(), // Cover image file
                            "-map", "0:a",                       // Use only the audio stream
                            "-c:a", "libmp3lame",                // Use MP3 encoder (libmp3lame)
                            "-q:a", "0",                         // High-quality audio encoding
                            "-metadata", "title=" + title,       // Set metadata: title
                            "-metadata", "artist=" + artist,     // Set metadata: artist
                            "-metadata", "album=" + album,       // Set metadata: album
                            "-id3v2_version", "3",               // Use ID3v2 for metadata
                            "-map", "1",                         // Map the cover image to the output file
                            "-disposition:v:0", "attached_pic",  // Attach the cover image as a picture
                            inputFilePath.concat("_new.mp3")                        // Output MP3 file path
                    };

                    try {
                        ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                            @Override
                            public void onSuccess(String message) {
                                Log.d(TAG, "MP3 conversion and metadata addition successful: " + message);
                            }

                            @Override
                            public void onFailure(String message) {
                                Log.e(TAG, "MP3 conversion failed: " + message);
                            }

                            @Override
                            public void onProgress(String message) {
                                Log.d(TAG, "Conversion in progress: " + message);
                            }

                            @Override
                            public void onStart() {
                                Log.d(TAG, "Started MP3 conversion and metadata addition.");

                            }

                            @Override
                            public void onFinish() {
                                Log.d(TAG, "Finished MP3 conversion and metadata addition.");
                                Log.i(TAG, "onFinish: imageDeletion? " + tempCoverFile.delete());
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error executing FFmpeg command: ", e);
                    }
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.e(TAG, "convertToMP3WithMetadata: ", e);
        }
    }
}
