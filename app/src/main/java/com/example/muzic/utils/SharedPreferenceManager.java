package com.example.muzic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.AudiusUserResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferenceManager {
    private final SharedPreferences sharedPreferences;
    private static SharedPreferenceManager instance;
    private final Gson gson;
    
    private static final String CACHE_NAME = "audius_cache";
    private static final String KEY_TRACK_PREFIX = "track:";
    private static final String KEY_PLAYLIST_PREFIX = "playlist:";
    private static final String KEY_ARTIST_PREFIX = "artist:";
    private static final String KEY_SAVED_LIBRARIES = "saved_libraries";
    private static final String KEY_SAVED_PLAYLISTS = "saved_playlists";
    private static final String KEY_HOME_TRENDING_TRACKS = "home_trending_tracks";
    private static final String KEY_HOME_TRENDING_PLAYLISTS = "home_trending_playlists";
    private static final String KEY_HOME_TRENDING_ARTISTS = "home_trending_artists";
    private static final String PLAYLISTS_KEY = "saved_playlists";

    public static SharedPreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceManager(context);
        }
        return instance;
    }

    private SharedPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(CACHE_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Track related methods
    public void cacheTrack(String id, Track track) {
        saveToPreferences(KEY_TRACK_PREFIX + id, track);
    }

    public Track getCachedTrack(String id) {
        return getFromPreferences(KEY_TRACK_PREFIX + id, Track.class);
    }

    public void cacheTrendingTracks(AudiusTrackResponse response) {
        saveToPreferences(KEY_HOME_TRENDING_TRACKS, response);
    }

    public AudiusTrackResponse getCachedTrendingTracks() {
        return getFromPreferences(KEY_HOME_TRENDING_TRACKS, AudiusTrackResponse.class);
    }

    // Playlist related methods
    public void cachePlaylist(String id, Playlist playlist) {
        saveToPreferences(KEY_PLAYLIST_PREFIX + id, playlist);
    }

    public Playlist getCachedPlaylist(String id) {
        return getFromPreferences(KEY_PLAYLIST_PREFIX + id, Playlist.class);
    }

    public void cacheTrendingPlaylists(PlaylistResponse response) {
        saveToPreferences(KEY_HOME_TRENDING_PLAYLISTS, response);
    }

    public PlaylistResponse getCachedTrendingPlaylists() {
        return getFromPreferences(KEY_HOME_TRENDING_PLAYLISTS, PlaylistResponse.class);
    }

    // Artist related methods
    public void cacheArtist(String id, User artist) {
        saveToPreferences(KEY_ARTIST_PREFIX + id, artist);
    }

    public User getCachedArtist(String id) {
        return getFromPreferences(KEY_ARTIST_PREFIX + id, User.class);
    }

    public void cacheTrendingArtists(AudiusUserResponse response) {
        saveToPreferences(KEY_HOME_TRENDING_ARTISTS, response);
    }

    public AudiusUserResponse getCachedTrendingArtists() {
        return getFromPreferences(KEY_HOME_TRENDING_ARTISTS, AudiusUserResponse.class);
    }

    // Legacy Library related methods - Keep these for backward compatibility
    public void setSavedLibrariesData(SavedLibrariesAudius savedLibraries) {
        saveToPreferences(KEY_SAVED_LIBRARIES, savedLibraries);
    }

    public SavedLibrariesAudius getSavedLibrariesData() {
        return getFromPreferences(KEY_SAVED_LIBRARIES, SavedLibrariesAudius.class);
    }

    public void addLibraryToSavedLibraries(SavedLibrariesAudius.Library library) {
        SavedLibrariesAudius savedLibraries = getSavedLibrariesData();
        if (savedLibraries == null) {
            savedLibraries = new SavedLibrariesAudius(new ArrayList<>());
        }
        savedLibraries.lists().add(library);
        setSavedLibrariesData(savedLibraries);
    }

    public void removeLibraryFromSavedLibraries(int index) {
        SavedLibrariesAudius savedLibraries = getSavedLibrariesData();
        if (savedLibraries == null || savedLibraries.lists() == null) return;
        if (index >= 0 && index < savedLibraries.lists().size()) {
            savedLibraries.lists().remove(index);
            setSavedLibrariesData(savedLibraries);
        }
    }

    // New Modern Playlist methods
    public List<Playlist> getSavedPlaylists() {
        String json = sharedPreferences.getString(KEY_SAVED_PLAYLISTS, null);
        if (json == null) return new ArrayList<>();
        
        Type type = new TypeToken<List<Playlist>>(){}.getType();
        try {
            List<Playlist> playlists = gson.fromJson(json, type);
            return playlists != null ? playlists : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setSavedPlaylists(List<Playlist> playlists) {
        if (playlists == null) playlists = new ArrayList<>();
        saveToPreferences(KEY_SAVED_PLAYLISTS, playlists);
    }

    public void addPlaylistToSavedPlaylists(Playlist playlist) {
        List<Playlist> playlists = getSavedPlaylists();
        playlists.add(playlist);
        setSavedPlaylists(playlists);
    }

    public void removePlaylistFromSavedPlaylists(String playlistId) {
        List<Playlist> playlists = getSavedPlaylists();
        playlists.removeIf(p -> p.id().equals(playlistId));
        setSavedPlaylists(playlists);
    }

    public void removePlaylistFromSavedPlaylists(int index) {
        List<Playlist> playlists = getSavedPlaylists();
        if (index >= 0 && index < playlists.size()) {
            playlists.remove(index);
            setSavedPlaylists(playlists);
        }
    }

    public boolean isPlaylistSaved(String playlistId) {
        List<Playlist> playlists = getSavedPlaylists();
        return playlists.stream().anyMatch(p -> p.id().equals(playlistId));
    }

    // Cache clearing methods
    public void clearTrendingCache() {
        sharedPreferences.edit()
                .remove(KEY_HOME_TRENDING_TRACKS)
                .remove(KEY_HOME_TRENDING_PLAYLISTS)
                .remove(KEY_HOME_TRENDING_ARTISTS)
                .apply();
    }

    public void clearAllCache() {
        sharedPreferences.edit().clear().apply();
    }

    // Helper methods
    private <T> void saveToPreferences(String key, T data) {
        if (data == null) return;
        sharedPreferences.edit()
                .putString(key, gson.toJson(data))
                .apply();
    }

    private <T> T getFromPreferences(String key, Class<T> type) {
        String json = sharedPreferences.getString(key, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasCache(String key) {
        return sharedPreferences.contains(key);
    }
}
