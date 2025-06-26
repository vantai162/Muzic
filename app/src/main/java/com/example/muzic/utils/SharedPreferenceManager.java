package com.example.muzic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.muzic.model.Library;
import com.example.muzic.model.TrackData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.stream.Collectors;

public class SharedPreferenceManager {
    private final SharedPreferences sharedPreferences;
    private static SharedPreferenceManager instance;
    private final Gson gson;
    
    private static final String CACHE_NAME = "audius_cache";
    private static final String KEY_TRACK_PREFIX = "track:";
    private static final String KEY_PLAYLIST_PREFIX = "playlist:";
    private static final String KEY_ARTIST_PREFIX = "artist:";
    private static final String KEY_SAVED_LIBRARIES = "saved_libraries";
    private static final String KEY_HOME_TRENDING_TRACKS = "home_trending_tracks";
    private static final String KEY_HOME_TRENDING_PLAYLISTS = "home_trending_playlists";
    private static final String KEY_HOME_TRENDING_ARTISTS = "home_trending_artists";

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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        for (SavedLibrariesAudius.Library li : savedLibraries.lists()) {
            Library library = FirebaseConverters.toLibraryData(li);
            String libraryId = library.id;

            db.collection("users")
                    .document(userID)
                    .collection("libraries")
                    .document(libraryId)
                    .set(library);

            for (Track track : li.tracks()) {
                TrackData trackData = FirebaseConverters.toTrackData(track);

                db.collection("users")
                        .document(userID)
                        .collection("libraries")
                        .document(libraryId)
                        .collection("tracks")
                        .document(trackData.id)
                        .set(trackData);
            }
        }
    }

    public Task<SavedLibrariesAudius> getSavedLibrariesData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TaskCompletionSource<SavedLibrariesAudius> source = new TaskCompletionSource<>();

        db.collection("users")
                .document(userID)
                .collection("libraries")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Library> classLibraries = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Library lib = doc.toObject(Library.class);
                        if (lib != null) classLibraries.add(lib);
                    }

                    List<SavedLibrariesAudius.Library> recordLibraries = classLibraries.stream()
                            .map(FirebaseConverters::toLibraryRecord)
                            .collect(Collectors.toList());

                    SavedLibrariesAudius result = new SavedLibrariesAudius(recordLibraries);

                    saveToPreferences(KEY_SAVED_LIBRARIES, result);

                    source.setResult(result);
                })
                .addOnFailureListener(e -> {
                    SavedLibrariesAudius cached = getFromPreferences(KEY_SAVED_LIBRARIES, SavedLibrariesAudius.class);
                    if (cached != null) {
                        source.setResult(cached);
                    } else {
                        source.setResult(new SavedLibrariesAudius(List.of()));
                    }
                });

        return source.getTask();
    }

    public void addLibraryToSavedLibraries(SavedLibrariesAudius.Library library) {
        getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
            if (savedLibraries == null) {
                savedLibraries = new SavedLibrariesAudius(new ArrayList<>());
            }
            savedLibraries.lists().add(library);
            setSavedLibrariesData(savedLibraries);
        });
    }

    public void removeLibraryFromSavedLibraries(int index) {
        getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
            if (savedLibraries == null || savedLibraries.lists() == null) return;
            if (index >= 0 && index < savedLibraries.lists().size()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (SavedLibrariesAudius.Library li : savedLibraries.lists()) {
                    Library library = FirebaseConverters.toLibraryData(li);
                    String libraryId = library.id;

                    db.collection("users")
                            .document(userID)
                            .collection("libraries")
                            .document(libraryId)
                            .delete();
                }

                savedLibraries.lists().remove(index);
            }
        });
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
