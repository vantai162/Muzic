package com.example.muzic.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muzic.R;
import com.example.muzic.adapter.ActivityArtistProfileTopSongsAdapter;
import com.example.muzic.databinding.ActivityArtistProfileBinding;
import com.example.muzic.model.TrackData;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.network.AudiusRepository;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.AudiusUserResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.example.muzic.utils.SharedPreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistProfileActivity extends AppCompatActivity {
    private static final String TAG = "ArtistProfileActivity";
    private ActivityArtistProfileBinding binding;
    private String artistId = "";
    private User artistData;
    private SharedPreferenceManager sharedPreferenceManager;
    private AudiusRepository audiusRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtistProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize repositories and managers
        audiusRepository = new AudiusRepository();
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this);

        setupUI();
        showShimmerData();
        loadArtistData();
    }

    private void setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.collapsingToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Setup collapsing toolbar
        binding.collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Setup RecyclerView
        binding.topSongsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadArtistData() {
        Log.d(TAG, "Starting loadArtistData");
        if (getIntent().getExtras() == null) {
            Log.e(TAG, "No extras in intent");
            showError("No artist data provided");
            return;
        }

        String artistJson = getIntent().getExtras().getString("data", "null");
        Log.d(TAG, "Artist JSON from intent: " + artistJson);
        
        try {
            User artist = new Gson().fromJson(artistJson, User.class);
            if (artist == null) {
                Log.e(TAG, "Failed to parse artist data from intent");
                showError("Failed to parse artist data");
                return;
            }
            Log.d(TAG, "Successfully parsed artist: " + artist.name() + ", id: " + artist.id());

            artistId = artist.id();
            displayArtistData(artist);

            // First try to get cached artist data
            User cachedArtist = sharedPreferenceManager.getCachedArtist(artistId);
            if (cachedArtist != null) {
                Log.d(TAG, "Using cached artist data");
                displayArtistData(cachedArtist);
                loadTopSongs();
            }

            // Then fetch fresh data from API using AudiusRepository
            Log.d(TAG, "Fetching fresh artist data for handle: " + artist.handle());
            audiusRepository.getTrendingTracks(50, new retrofit2.Callback<AudiusTrackResponse>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<AudiusTrackResponse> call, 
                                     @NonNull retrofit2.Response<AudiusTrackResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Track> allTracks = response.body().data();
                        List<Track> artistTracks = filterArtistTracks(allTracks);
                        Log.d(TAG, "Fetched " + artistTracks.size() + " tracks for artist");
                        
                        if (artistTracks.isEmpty()) {
                            showError("No songs found for this artist");
                        } else {
                            runOnUiThread(() -> {
                                displayTopSongs(artistTracks);
                                sharedPreferenceManager.cacheTrendingTracks(response.body());
                            });
                        }
                    } else {
                        Log.e(TAG, "API response unsuccessful. Code: " + response.code());
                        showError("Failed to load artist songs");
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<AudiusTrackResponse> call, 
                                    @NonNull Throwable t) {
                    Log.e(TAG, "Error fetching artist data", t);
                    showError("Failed to load artist data");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading artist data", e);
            showError("Error loading artist data");
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            // Hide shimmer if showing
            binding.topSongsRecyclerview.setVisibility(View.VISIBLE);
        });
    }

    private List<Track> filterArtistTracks(List<Track> tracks) {
        List<Track> artistTracks = new ArrayList<>();
        try {
            for (Track track : tracks) {
                if (track.user() != null && track.user().id() != null && 
                    track.user().id().equals(artistId)) {
                    artistTracks.add(track);
                }
            }
            Log.d(TAG, "Filtered " + artistTracks.size() + " tracks for artist " + artistId);
        } catch (Exception e) {
            Log.e(TAG, "Error filtering artist tracks", e);
        }
        return artistTracks;
    }

    private void displayArtistData(User artist) {
        Log.d(TAG, "Displaying artist data for: " + artist.name());
        try {
            artistData = artist;
            
            // Set artist name
            binding.artistName.setText(artist.name());
            binding.collapsingToolbarLayout.setTitle(artist.name());

            // Load artist image
            if (artist.profilePicture() != null) {
                String imageUrl = artist.profilePicture().x1000();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d(TAG, "Loading artist image from: " + imageUrl);
                    Picasso.get()
                        .load(Uri.parse(imageUrl))
                        .placeholder(R.drawable.bolt_24px)
                        .error(R.drawable.bolt_24px)
                        .into(binding.artistImg);
                } else {
                    Log.w(TAG, "No high resolution image available");
                    binding.artistImg.setImageResource(R.drawable.bolt_24px);
                }
            } else {
                Log.w(TAG, "No profile picture available");
                binding.artistImg.setImageResource(R.drawable.bolt_24px);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying artist data", e);
            showError("Error displaying artist info");
        }
    }

    private void loadTopSongs() {
        Log.d(TAG, "Loading top songs for artist: " + artistId);
        try {
            // First try to get cached tracks
            AudiusTrackResponse cachedTracks = sharedPreferenceManager.getCachedTrendingTracks();
            if (cachedTracks != null) {
                Log.d(TAG, "Using cached tracks");
                displayTopSongs(filterArtistTracks(cachedTracks.data()));
            }

            // Then fetch fresh data
            audiusRepository.getTrendingTracks(50, new retrofit2.Callback<AudiusTrackResponse>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<AudiusTrackResponse> call, 
                                     @NonNull retrofit2.Response<AudiusTrackResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Track> allTracks = response.body().data();
                        Log.d(TAG, "Fetched " + allTracks.size() + " trending tracks");
                        
                        // Filter tracks on a background thread
                        new Thread(() -> {
                            List<Track> artistTracks = filterArtistTracks(allTracks);
                            Log.d(TAG, "Filtered " + artistTracks.size() + " tracks for artist");
                            runOnUiThread(() -> {
                                if (artistTracks.isEmpty()) {
                                    showError("No songs found for this artist");
                                } else {
                                    displayTopSongs(artistTracks);
                                    sharedPreferenceManager.cacheTrendingTracks(response.body());
                                }
                            });
                        }).start();

                    } else {
                        Log.e(TAG, "API response unsuccessful. Code: " + response.code());
                        showError("Failed to load artist songs");
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<AudiusTrackResponse> call, 
                                    @NonNull Throwable t) {
                    Log.e(TAG, "Error fetching artist data", t);
                    showError("Failed to load artist data");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadTopSongs", e);
            showError("Failed to load top songs");
        }
    }

    private void displayTopSongs(List<Track> songs) {
        try {
            Log.d(TAG, "Displaying " + songs.size() + " songs");
            ActivityArtistProfileTopSongsAdapter adapter = new ActivityArtistProfileTopSongsAdapter(songs);
            binding.topSongsRecyclerview.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error displaying top songs", e);
            showError("Error displaying songs");
        }
    }

    private void showShimmerData() {
        List<Track> shimmerData = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            shimmerData.add(new Track(
                null, "", "", "<shimmer>", "", "", "", 
                0, 0, "", "", null, 0, false, 0, "", false
            ));
        }
        displayTopSongs(shimmerData);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 