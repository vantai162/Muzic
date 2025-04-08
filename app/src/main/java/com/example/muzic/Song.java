package com.example.muzic;

public class Song {
    private int id;
    private String title;

    private String youtubeUrl;
    public Song(int _id,String _title,String _URL){
        id = _id;
        title = _title;
        youtubeUrl = _URL;
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
}