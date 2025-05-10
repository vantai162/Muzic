package com.example.muzic;

import java.util.List;

public class Playlist {
    private String title;
    private List<Song> songs;
    private int imageResId; // resource ID cho ảnh bìa playlist (local)

    public Playlist(String title, List<Song> songs, int imageResId) {
        this.title = title;
        this.songs = songs;
        this.imageResId = imageResId;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public int getImageResId() {
        return imageResId;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    // Optional: get number of songs
    public int getSongCount() {
        return songs != null ? songs.size() : 0;
    }
}