package com.example.muzic.model;

import java.util.ArrayList;
import java.util.List;

public class PlaylistData {
    public Artwork artwork;
    public String description;
    public String permalink;
    public String id;
    public boolean is_album;
    public String playlist_name;
    public int repost_count;
    public int favorite_count;
    public int total_play_count;
    public User user;
    public List<TrackData> tracks = new ArrayList<>();
}
