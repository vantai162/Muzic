package com.example.muzic.object;

public class Song {
    private int id;
    private String title;

    private String youtubeUrl;
    private String imageUrl;
    private String artist;

    public Song(int id, String title, String youtubeUrl, String imageUrl,String artist) {
        this.id = id;
        this.title = title;
        this.youtubeUrl = youtubeUrl;
        this.imageUrl = imageUrl;
        this.artist = artist;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}