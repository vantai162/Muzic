package com.example.muzic.records;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private List<Track> tracks;
    private List<Playlist> playlists;
    private List<User> users;

    public SearchResult() {
        this.tracks = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
} 