package com.example.muzic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.adapter.PopularUserAdapter;
import com.example.muzic.adapter.TrendingPlaylistAdapter;
import com.example.muzic.adapter.TrendingTracksAdapter;
import com.example.muzic.databinding.ActivityMainBinding;
import com.example.muzic.databinding.PlayBarBinding;
import com.example.muzic.model.TrackData;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.squareup.picasso.Picasso;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AudiusAPI";
    private ExoPlayer player;
    private TrendingTracksAdapter trendingTracksAdapter;
    private PopularUserAdapter popularUserAdapter;
    private TrendingPlaylistAdapter trendingPlaylistAdapter;
    private ActivityMainBinding binding;
    private PlayBarBinding playBarBinding;
    private SlidingRootNav slidingRootNavBuilder;
    private TrackData currentTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize play bar binding
        playBarBinding = PlayBarBinding.bind(binding.playBar.getRoot());

        // Get shared ExoPlayer instance
        player = ApplicationClass.player;
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            ApplicationClass.player = player;
        }

        // Setup navigation drawer
        slidingRootNavBuilder = new SlidingRootNavBuilder(this)
                .withMenuLayout(R.layout.main_drawer_layout)
                .withContentClickableWhenMenuOpened(false)
                .withDragDistance(250)
                .inject();
        binding.profileIcon.setOnClickListener(view -> slidingRootNavBuilder.openMenu(true));
        onDrawerItemsClicked();

        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Load data
        loadTrendingTracks();
        
        // Setup play bar click listener
        binding.playBarBackground.setOnClickListener(v -> {
            if (currentTrack != null) {
                openMusicOverview();
            }
        });

        // Setup play bar controls
        binding.playBarPlayPauseIcon.setOnClickListener(v -> togglePlayPause());
        binding.playBarPrevIcon.setOnClickListener(v -> playPreviousTrack());
        binding.playBarNextIcon.setOnClickListener(v -> playNextTrack());
    }

    private void setupRecyclerViews() {
        // Trending Tracks
        binding.popularSongsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        trendingTracksAdapter = new TrendingTracksAdapter(this, track -> {
            playTrack(track);
            // Open MusicOverviewActivity immediately after starting playback
            openMusicOverview();
        });
        binding.popularSongsRecyclerView.setAdapter(trendingTracksAdapter);

        // Popular Users
        binding.popularArtistsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        popularUserAdapter = new PopularUserAdapter(this, new ArrayList<>(), user -> {
            Log.d(TAG, "User clicked: " + user.name());
        });
        binding.popularArtistsRecyclerView.setAdapter(popularUserAdapter);

        // Popular Playlists
        binding.popularPlaylistRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        trendingPlaylistAdapter = new TrendingPlaylistAdapter(this, new ArrayList<>());
        binding.popularPlaylistRecyclerView.setAdapter(trendingPlaylistAdapter);
    }

    private void openMusicOverview() {
        if (currentTrack != null) {
            Intent intent = new Intent(this, MusicOverviewActivity.class);
            intent.putExtra("track", currentTrack);
            intent.putExtra("isPlaying", player.isPlaying());
            // Add current playback position
            intent.putExtra("currentPosition", player.getCurrentPosition());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
        }
    }

    private void playTrack(Track track) {
        // Convert Track to TrackData and handle type conversion
        currentTrack = new TrackData();
        currentTrack.id = track.id();
        currentTrack.title = track.title();
        
        // Convert Artwork
        currentTrack.artwork = new com.example.muzic.model.Artwork();
        if (track.artwork() != null) {
            currentTrack.artwork._150x150 = track.artwork().x150();
            currentTrack.artwork._480x480 = track.artwork().x480();
            currentTrack.artwork._1000x1000 = track.artwork().x1000();
        }
        
        // Convert User
        currentTrack.user = new com.example.muzic.model.User();
        currentTrack.user.name = track.user().name();
        currentTrack.user.id = track.user().id();
        currentTrack.duration = track.duration();

        // Update play bar UI
        binding.playBarBackground.setVisibility(View.VISIBLE);
        binding.playBarMusicTitle.setText(track.title());
        binding.playBarMusicDesc.setText(track.user().name());
        if (track.artwork() != null && track.artwork().x480() != null) {
            Picasso.get().load(track.artwork().x480()).into(binding.playBarCoverImage);
        }

        // Play the track using shared ExoPlayer instance
        String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + track.id() + "/stream";
        MediaItem mediaItem = MediaItem.fromUri(streamUrl);
        
        // Only start playback if this is a new track
        if (player.getCurrentMediaItem() == null || 
            !player.getCurrentMediaItem().mediaId.equals(streamUrl)) {
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
        
        binding.playBarPlayPauseIcon.setImageResource(R.drawable.baseline_pause_24);
        binding.playBarPlayPauseIcon.setRotation(0);
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
            binding.playBarPlayPauseIcon.setImageResource(R.drawable.play_arrow_24px);
            binding.playBarPlayPauseIcon.setRotation(0);
        } else {
            player.play();
            binding.playBarPlayPauseIcon.setImageResource(R.drawable.baseline_pause_24);
            binding.playBarPlayPauseIcon.setRotation(0);
        }
    }

    private void playPreviousTrack() {
        // Get current track index from adapter
        int currentIndex = trendingTracksAdapter.getCurrentTrackIndex();
        if (currentIndex > 0) {
            Track previousTrack = trendingTracksAdapter.getTrack(currentIndex - 1);
            playTrack(previousTrack);
            trendingTracksAdapter.setCurrentTrackIndex(currentIndex - 1);
        }
    }

    private void playNextTrack() {
        // Get current track index from adapter
        int currentIndex = trendingTracksAdapter.getCurrentTrackIndex();
        if (currentIndex < trendingTracksAdapter.getItemCount() - 1) {
            Track nextTrack = trendingTracksAdapter.getTrack(currentIndex + 1);
            playTrack(nextTrack);
            trendingTracksAdapter.setCurrentTrackIndex(currentIndex + 1);
        }
    }

    private void loadTrendingTracks() {
        AudiusApiService apiService = AudiusApiClient.getInstance();
        Call<AudiusTrackResponse> call = apiService.getTrendingTracks(10);

        call.enqueue(new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = response.body().data();
                    trendingTracksAdapter.setTracks(tracks);

                    // Get unique users for popular artists
                    Set<String> seenIds = new HashSet<>();
                    List<User> uniqueUsers = new ArrayList<>();
                    for (Track track : tracks) {
                        User user = track.user();
                        if (user != null && seenIds.add(user.id())) {
                            uniqueUsers.add(user);
                        }
                    }
                    popularUserAdapter.setUsers(uniqueUsers);
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
            }
        });

        // Load trending playlists
        Call<PlaylistResponse> playlistCall = apiService.getTrendingPlaylists(10);
        playlistCall.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    trendingPlaylistAdapter.setPlaylists(response.body().data());
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                Log.e(TAG, "Playlist API Call Failed: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't release the player since it's shared
        player = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update play bar state if needed
        if (currentTrack != null) {
            binding.playBarPlayPauseIcon.setRotation(player.isPlaying() ? 0 : 0);
        }
    }

    private void onDrawerItemsClicked() {
        // Implement drawer menu item clicks
    }
}
