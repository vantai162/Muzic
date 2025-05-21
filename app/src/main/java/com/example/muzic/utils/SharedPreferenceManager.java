package com.example.muzic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.AudiusUserResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;

import java.util.ArrayList;

public class SharedPreferenceManager {

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    private final SharedPreferences sharedPreferences;

    private static SharedPreferenceManager instance;

    public static SharedPreferenceManager getInstance(Context context) {
        if (instance == null) instance = new SharedPreferenceManager(context);
        return instance;
    }

    private SharedPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
    }

    public void setAudiusTrackById(String id, AudiusTrackResponse track) {
        sharedPreferences.edit().putString("track:" + id, new Gson().toJson(track)).apply();
    }

    public AudiusTrackResponse getAudiusTrackById(String id) {
        return sharedPreferences.contains("track:" + id) ?
                new Gson().fromJson(sharedPreferences.getString("track:" + id, ""), AudiusTrackResponse.class) :
                null;
    }


    public void setHomeArtistsRecommended(AudiusUserResponse artistsRecommended) {
        sharedPreferences.edit().putString("home_artists_recommended", new Gson().toJson(artistsRecommended)).apply();
    }

    public AudiusUserResponse getHomeArtistsRecommended() {
        return new Gson().fromJson(sharedPreferences.getString("home_artists_recommended", ""), AudiusUserResponse.class);
    }


    public void setHomePlaylistRecommended(PlaylistResponse playlistsSearch) {
        sharedPreferences.edit().putString("home_playlists_recommended", new Gson().toJson(playlistsSearch)).apply();
    }

    public PlaylistResponse getHomePlaylistRecommended() {
        return new Gson().fromJson(sharedPreferences.getString("home_playlists_recommended", ""), PlaylistResponse.class);
    }

    /*public void setSongResponseById(String id, AudiusTrackResponse songSearch) {
        sharedPreferences.edit().putString(id, new Gson().toJson(songSearch)).apply();
    }

    public AudiusTrackResponse getSongResponseById(String id) {
        return new Gson().fromJson(sharedPreferences.getString(id, ""), AudiusTrackResponse.class);
    }

    public boolean isSongResponseById(String id) {
        return sharedPreferences.contains(id);
    }


    public void setPlaylistResponseById(String id, PlaylistSearch playlistSearch) {
        sharedPreferences.edit().putString(id, new Gson().toJson(playlistSearch)).apply();
    }

    public PlaylistSearch getPlaylistResponseById(String id) {
        return new Gson().fromJson(sharedPreferences.getString(id, ""), PlaylistSearch.class);
    }

    public void setTrackQuality(String string) {
        sharedPreferences.edit().putString("track_quality", string).apply();
    }

    public String getTrackQuality() {
        return sharedPreferences.getString("track_quality", "320kbps");
    }
*/
    public void setSavedLibrariesData(SavedLibrariesAudius savedLibraries) {
        sharedPreferences.edit().putString("saved_libraries", new Gson().toJson(savedLibraries)).apply();
    }

    public SavedLibrariesAudius getSavedLibrariesData() {
        return new Gson().fromJson(sharedPreferences.getString("saved_libraries", ""), SavedLibrariesAudius.class);
    }


    public void addLibraryToSavedLibraries(SavedLibrariesAudius.Library library) {
        SavedLibrariesAudius savedLibraries = getSavedLibrariesData();
        if (savedLibraries == null) savedLibraries = new SavedLibrariesAudius(new ArrayList<>());
        savedLibraries.lists().add(library);
        setSavedLibrariesData(savedLibraries);
    }

    public void removeLibraryFromSavedLibraries(int index) {
        SavedLibrariesAudius savedLibraries = getSavedLibrariesData();
        if (savedLibraries == null) return;
        savedLibraries.lists().remove(index);
        setSavedLibrariesData(savedLibraries);
    }

    public void setSavedLibraryDataById(String id, SavedLibrariesAudius.Library library) {
        sharedPreferences.edit().putString("library:" + id, new Gson().toJson(library)).apply();
    }

    public SavedLibrariesAudius.Library getSavedLibraryDataById(String id) {
        return sharedPreferences.contains("library:" + id) ?
                new Gson().fromJson(sharedPreferences.getString("library:" + id, ""), SavedLibrariesAudius.Library.class) :
                null;
    }


    public void setArtistData(String artistID, User artistSearch) {
        sharedPreferences.edit().putString("artistData://" + artistID, new Gson().toJson(artistSearch)).apply();
    }

    public User getArtistData(String artistId) {
        return sharedPreferences.contains("artistData://" + artistId) ? new Gson().fromJson(sharedPreferences.getString("artistData://" + artistId, ""), User.class) : null;
    }
}
