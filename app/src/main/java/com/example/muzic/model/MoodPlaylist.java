package com.example.muzic.model;

import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import java.util.List;
import com.example.muzic.model.Artwork;

public class MoodPlaylist {
    private String id;
    private final String mood;
    private String description;
    private final List<Track> tracks;
    private Artwork artwork;
    private User user;

    public MoodPlaylist(String id, String mood, String description, List<Track> tracks, User user) {
        this.id = id;
        this.mood = mood;
        this.description = description;
        this.tracks = tracks;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public String getMood() {
        return mood;
    }

    public String getDescription() {
        return description;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
