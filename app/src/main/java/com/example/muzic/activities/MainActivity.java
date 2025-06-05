package com.example.muzic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.adapter.MoodPlaylistAdapter;
import com.example.muzic.adapter.PopularUserAdapter;
import com.example.muzic.adapter.TrendingPlaylistAdapter;
import com.example.muzic.adapter.TrendingTracksAdapter;
import com.example.muzic.databinding.ActivityMainBinding;
import com.example.muzic.databinding.PlayBarBinding;
import com.example.muzic.model.MoodPlaylist;
import com.example.muzic.model.TrackData;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.network.AudiusRepository;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private MoodPlaylistAdapter moodPlaylistAdapter;
    private ActivityMainBinding binding;
    private PlayBarBinding playBarBinding;
    private SlidingRootNav slidingRootNavBuilder;
    private TrackData currentTrack;
    private AudiusRepository audiusRepository;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate and setContentView
        ApplicationClass app = (ApplicationClass) getApplication();
        app.reapplyTheme();
        
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

        // Initialize AudiusRepository
        audiusRepository = new AudiusRepository();

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
        
        // Setup play bar listeners
        setupPlayBarListeners();
        
        // Load data
        loadTrendingTracks();
        
        // Restore play bar state if needed
        restorePlayBarState();
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

        // Mood
        binding.moodPlaylistRecyclerView.setLayoutManager(
                new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)
        );
        moodPlaylistAdapter = new MoodPlaylistAdapter(this, new ArrayList<>());
        binding.moodPlaylistRecyclerView.setAdapter(moodPlaylistAdapter);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openMusicOverview() {
        if (currentTrack != null) {
            // Convert all tracks to TrackData and update ApplicationClass
            ArrayList<TrackData> playlist = new ArrayList<>();
            for (int i = 0; i < trendingTracksAdapter.getItemCount(); i++) {
                Track track = trendingTracksAdapter.getTrack(i);
                if (track != null) {
                    playlist.add(convertToTrackData(track));
                }
            }
            
            // Update playlist in ApplicationClass
            ApplicationClass app = (ApplicationClass) getApplication();
            app.updatePlaylist(playlist, trendingTracksAdapter.getCurrentTrackIndex());
            
            // Start MusicOverviewActivity
            Intent intent = new Intent(this, MusicOverviewActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void playTrack(Track track) {
        // Convert Track to TrackData and store as current
        currentTrack = convertToTrackData(track);
        
        // Update UI
        updatePlayBarContent(currentTrack);

        // Convert all tracks to TrackData and update ApplicationClass
        ArrayList<TrackData> playlist = new ArrayList<>();
        for (int i = 0; i < trendingTracksAdapter.getItemCount(); i++) {
            Track t = trendingTracksAdapter.getTrack(i);
            if (t != null) {
                playlist.add(convertToTrackData(t));
            }
        }
        
        // Update playlist in ApplicationClass
        ApplicationClass app = (ApplicationClass) getApplication();
        app.updatePlaylist(playlist, trendingTracksAdapter.getCurrentTrackIndex());
        app.playCurrentTrack();
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

    @OptIn(markerClass = UnstableApi.class)
    private void playPreviousTrack() {
        ApplicationClass app = (ApplicationClass) getApplication();
        app.playPreviousTrack();
        currentTrack = app.currentTrack;
        updatePlayBarContent(currentTrack);
        trendingTracksAdapter.setCurrentTrackIndex(app.currentTrackIndex);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void playNextTrack() {
        ApplicationClass app = (ApplicationClass) getApplication();
        app.playNextTrack();
        currentTrack = app.currentTrack;
        updatePlayBarContent(currentTrack);
        trendingTracksAdapter.setCurrentTrackIndex(app.currentTrackIndex);
    }

    private void loadTrendingTracks() {
        audiusRepository.getTrendingTracks(15, new Callback<AudiusTrackResponse>() {
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

                    // === Tạo danh sách mood playlists ===
                    Map<String, List<Track>> moodMap = new HashMap<>();
                    for (Track track : tracks) {
                        if (track.mood() == null) continue;
                        String mood = track.mood().toLowerCase();
                        if (!moodMap.containsKey(mood)) {
                            moodMap.put(mood, new ArrayList<>());
                        }
                        moodMap.get(mood).add(track);
                    }

                    List<MoodPlaylist> moodPlaylists = new ArrayList<>();
                    for (String mood : moodMap.keySet()) {
                        List<Track> moodTracks = moodMap.get(mood);
                        if (moodTracks != null && !moodTracks.isEmpty()) {
                            // Sử dụng thông tin từ track đầu tiên cho playlist
                            Track firstTrack = moodTracks.get(0);
                            String playlistId = "mood-" + mood + "-" + System.currentTimeMillis();
                            String description = "A collection of " + capitalize(mood) + " tracks";
                            
                            moodPlaylists.add(new MoodPlaylist(
                                playlistId,
                                capitalize(mood),
                                description,
                                moodTracks,
                                firstTrack.user() // Sử dụng user của track đầu tiên
                            ));
                        }
                    }

                    Log.d("MoodPlaylist", "Mood playlists size: " + moodPlaylists.size());
                    for (MoodPlaylist playlist : moodPlaylists) {
                        Log.d("MoodPlaylist", "Title: " + playlist.getMood());
                    }
                    // Cập nhật adapter
                    moodPlaylistAdapter.setMoodPlaylists(moodPlaylists);
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
            }
        });

        // Load trending playlists
        audiusRepository.getTrendingPlaylists(10, new Callback<PlaylistResponse>() {
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

    @OptIn(markerClass = UnstableApi.class)
    @UnstableApi
    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when activity resumes
        ((ApplicationClass) getApplication()).reapplyTheme();
        
        if (ApplicationClass.currentTrack != null) {
            currentTrack = ApplicationClass.currentTrack;
            updatePlayBarContent(currentTrack);
        } else if (ApplicationClass.MUSIC_ID != null) {
            fetchCurrentTrackData(ApplicationClass.MUSIC_ID, track -> {
                if (track != null) {
                    currentTrack = track;
                    ApplicationClass.currentTrack = track;
                    updatePlayBarContent(track);
                }
            });
        }
    }

    //khuc menu la cai ham nay ne
    private void onDrawerItemsClicked() {
        slidingRootNavBuilder.getLayout().findViewById(R.id.settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            slidingRootNavBuilder.closeMenu();
        });

        slidingRootNavBuilder.getLayout().findViewById(R.id.logo).setOnClickListener(view -> slidingRootNavBuilder.closeMenu());

        /*slidingRootNavBuilder.getLayout().findViewById(R.id.library).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SavedLibrariesActivity.class));
            slidingRootNavBuilder.closeMenu();
        });*/

        slidingRootNavBuilder.getLayout().findViewById(R.id.about).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            slidingRootNavBuilder.closeMenu();
        });
    }
    private String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Restore play bar state from ApplicationClass
        if (ApplicationClass.currentTrack != null) {
            currentTrack = ApplicationClass.currentTrack;
            updatePlayBarContent(currentTrack);
        }
        // If no current track in ApplicationClass but music is playing
        else if (ApplicationClass.MUSIC_ID != null) {
            fetchCurrentTrackData(ApplicationClass.MUSIC_ID, track -> {
                if (track != null) {
                    currentTrack = track;
                    ApplicationClass.currentTrack = track;
                    updatePlayBarContent(track);
                }
            });
        }
    }

    private void updatePlayControls() {
        if (player != null) {
            binding.playBarPlayPauseIcon.setImageResource(
                player.isPlaying() ? 
                R.drawable.baseline_pause_24 : 
                R.drawable.play_arrow_24px
            );
        }
    }

    private void updatePlayBarContent(TrackData track) {
        if (track == null) return;
        
        runOnUiThread(() -> {
            binding.playBarBackground.setVisibility(View.VISIBLE);
            binding.playBarMusicTitle.setText(track.title);
            binding.playBarMusicDesc.setText(track.user != null ? track.user.name : "");
            
            if (track.artwork != null && track.artwork._480x480 != null) {
                Picasso.get()
                    .load(track.artwork._480x480)
                    .into(binding.playBarCoverImage);
            }
            
            // Update play/pause button state
            if (player != null) {
                binding.playBarPlayPauseIcon.setImageResource(
                    player.isPlaying() ? 
                    R.drawable.baseline_pause_24 : 
                    R.drawable.play_arrow_24px
                );
            }

            // Ensure click listeners are set
            binding.playBarBackground.setOnClickListener(v -> openMusicOverview());
            binding.playBarPlayPauseIcon.setOnClickListener(v -> togglePlayPause());
            binding.playBarPrevIcon.setOnClickListener(v -> playPreviousTrack());
            binding.playBarNextIcon.setOnClickListener(v -> playNextTrack());
        });
    }

    private void reattachClickListeners() {
        runOnUiThread(() -> {
            // Reattach main play bar click listener
            binding.playBarBackground.setOnClickListener(v -> {
                if (currentTrack != null) {
                    openMusicOverview();
                }
            });

            // Reattach control listeners
            binding.playBarPlayPauseIcon.setOnClickListener(v -> togglePlayPause());
            binding.playBarPrevIcon.setOnClickListener(v -> playPreviousTrack());
            binding.playBarNextIcon.setOnClickListener(v -> playNextTrack());
        });
    }

    private void setupPlayBarListeners() {
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

    @OptIn(markerClass = UnstableApi.class)
    private void restorePlayBarState() {
        // First try to restore from currentTrack
        if (currentTrack != null) {
            binding.playBarBackground.setVisibility(View.VISIBLE);
            binding.playBarMusicTitle.setText(currentTrack.title);
            binding.playBarMusicDesc.setText(currentTrack.user.name);
            
            if (currentTrack.artwork != null && currentTrack.artwork._480x480 != null) {
                Picasso.get()
                    .load(currentTrack.artwork._480x480)
                    .into(binding.playBarCoverImage);
            }

            binding.playBarPlayPauseIcon.setImageResource(
                player != null && player.isPlaying() ? 
                R.drawable.baseline_pause_24 : 
                R.drawable.play_arrow_24px
            );
            return;
        }

        // If no currentTrack but music is playing from ApplicationClass
        if (ApplicationClass.player != null && ApplicationClass.MUSIC_ID != null) {
            binding.playBarBackground.setVisibility(View.VISIBLE);
            
            // Set initial play/pause state
            binding.playBarPlayPauseIcon.setImageResource(
                ApplicationClass.player.isPlaying() ? 
                R.drawable.baseline_pause_24 : 
                R.drawable.play_arrow_24px
            );

            // Fetch track data
            fetchCurrentTrackData(ApplicationClass.MUSIC_ID, track -> {
                if (track != null) {
                    currentTrack = track;
                    binding.playBarMusicTitle.setText(track.title);
                    binding.playBarMusicDesc.setText(track.user.name);
                    
                    if (track.artwork != null && track.artwork._480x480 != null) {
                        Picasso.get()
                            .load(track.artwork._480x480)
                            .into(binding.playBarCoverImage);
                    }
                }
            });
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void fetchCurrentTrackData(String trackId, OnTrackDataFetchedListener listener) {
        // First try to get from ApplicationClass
        if (ApplicationClass.currentTrack != null && 
            ApplicationClass.currentTrack.id.equals(trackId)) {
            listener.onTrackDataFetched(ApplicationClass.currentTrack);
            return;
        }

        // Then try to get from cache
        TrackData cachedTrack = getTrackFromCache(trackId);
        if (cachedTrack != null) {
            ApplicationClass.currentTrack = cachedTrack;
            listener.onTrackDataFetched(cachedTrack);
            return;
        }

        // If not in cache, fetch from API
        AudiusApiClient.getInstance()
            .getTrack(trackId)
            .enqueue(new retrofit2.Callback<AudiusTrackResponse>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<AudiusTrackResponse> call,
                                     @NonNull retrofit2.Response<AudiusTrackResponse> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().data().isEmpty()) {
                        Track apiTrack = response.body().data().get(0);
                        TrackData track = convertToTrackData(apiTrack);
                        cacheTrackData(track);
                        ApplicationClass.currentTrack = track;
                        runOnUiThread(() -> listener.onTrackDataFetched(track));
                    } else {
                        runOnUiThread(() -> listener.onTrackDataFetched(null));
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<AudiusTrackResponse> call,
                                    @NonNull Throwable t) {
                    Log.e(TAG, "Error fetching track data", t);
                    runOnUiThread(() -> listener.onTrackDataFetched(null));
                }
            });
    }

    private TrackData getTrackFromCache(String trackId) {
        SharedPreferences prefs = getSharedPreferences("track_cache", MODE_PRIVATE);
        String trackJson = prefs.getString("track_" + trackId, null);
        if (trackJson != null) {
            return new Gson().fromJson(trackJson, TrackData.class);
        }
        return null;
    }

    private void cacheTrackData(TrackData track) {
        SharedPreferences prefs = getSharedPreferences("track_cache", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("track_" + track.id, new Gson().toJson(track));
        editor.apply();
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

    interface OnTrackDataFetchedListener {
        void onTrackDataFetched(TrackData track);
    }
}
