package com.example.muzic.model;


import com.example.muzic.records.Track;
import java.util.List;
import com.example.muzic.model.Artwork;

public class MoodPlaylist {
    private final String mood;
    private final List<Track> tracks;
    private Artwork artwork;

    public MoodPlaylist(String mood, List<Track> tracks) {
        this.mood = mood;
        this.tracks = tracks;
    }

    public String getMood() {
        return mood;
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
}
