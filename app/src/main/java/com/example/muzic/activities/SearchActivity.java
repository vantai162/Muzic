package com.example.muzic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.adapter.SearchAdapter;
import com.example.muzic.model.TrackData;
import com.example.muzic.network.SearchRepository;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.SearchResult;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnItemClickListener {
    private EditText searchEditText;
    private ImageView clearButton;
    private ChipGroup chipGroup;
    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;
    private SearchRepository searchRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        searchEditText = findViewById(R.id.edittext);
        clearButton = findViewById(R.id.clear_icon);
        chipGroup = findViewById(R.id.chip_group);
        recyclerView = findViewById(R.id.recycler_view);

        // Initialize repository
        searchRepository = new SearchRepository();

        // Setup RecyclerView
        searchAdapter = new SearchAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchAdapter);

        // Setup search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup clear button
        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            clearButton.setVisibility(View.GONE);
        });

        // Setup chip group listener
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!searchEditText.getText().toString().isEmpty()) {
                performSearch(searchEditText.getText().toString());
            }
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            searchAdapter.updateData(new SearchResult());
            return;
        }

        int checkedChipId = chipGroup.getCheckedChipId();
        SearchRepository.SearchCallback callback = new SearchRepository.SearchCallback() {
            @Override
            public void onSuccess(SearchResult result) {
                runOnUiThread(() -> searchAdapter.updateData(result));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        };
        
        if (checkedChipId == R.id.chip_song) {
            searchRepository.searchTracks(query, callback);
        } else if (checkedChipId == R.id.chip_playlists) {
            searchRepository.searchPlaylists(query, callback);
        } else if (checkedChipId == R.id.chip_artists) {
            searchRepository.searchUsers(query, callback);
        } else {
            // Search all (default)
            searchRepository.search(query, callback);
        }
    }

    @Override
    public void onTrackClick(Track track) {
        // Convert Track to TrackData
        TrackData trackData = convertToTrackData(track);
        
        // Update ApplicationClass
        ApplicationClass app = (ApplicationClass) getApplication();
        app.currentTrack = trackData;
        app.MUSIC_ID = track.id();
        
        // Create playlist with single track
        ArrayList<TrackData> playlist = new ArrayList<>();
        playlist.add(trackData);
        app.updatePlaylist(playlist, 0);
        
        // Start playing
        app.playCurrentTrack();
        
        // Open MusicOverviewActivity
        Intent intent = new Intent(this, MusicOverviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("playlist_id", playlist.id());
        startActivity(intent);
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(this, ArtistProfileActivity.class);
        intent.putExtra("data", new Gson().toJson(user));
        startActivity(intent);
    }

    private TrackData convertToTrackData(Track track) {
        TrackData trackData = new TrackData();
        trackData.id = track.id();
        trackData.title = track.title();
        trackData.duration = track.duration();
        trackData.description = track.description();
        trackData.genre = track.genre();
        trackData.track_cid = track.trackCid();
        trackData.mood = track.mood();
        trackData.release_date = track.releaseDate();
        trackData.repost_count = track.repostCount();
        trackData.favorite_count = track.favoriteCount();
        trackData.tags = track.tags();
        trackData.downloadable = track.downloadable();
        trackData.play_count = track.playCount();
        trackData.permalink = track.permalink();
        trackData.is_streamable = track.isStreamable();

        // Convert artwork
        if (track.artwork() != null) {
            trackData.artwork = new com.example.muzic.model.Artwork();
            trackData.artwork._150x150 = track.artwork().x150();
            trackData.artwork._480x480 = track.artwork().x480();
            trackData.artwork._1000x1000 = track.artwork().x1000();
        }

        // Convert user
        if (track.user() != null) {
            trackData.user = new com.example.muzic.model.User();
            trackData.user.name = track.user().name();
            trackData.user.id = track.user().id();
            // Add other user fields as needed
        }

        return trackData;
    }

    public void backPress(View view) {
        onBackPressed();
    }
} 