package com.example.muzic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.adapter.MoodPlaylistAdapter;
import com.example.muzic.adapter.PopularUserAdapter;
import com.example.muzic.adapter.TrendingPlaylistAdapter;
import com.example.muzic.adapter.TrendingTracksAdapter;
import com.example.muzic.adapter.MainSavedLibrariesAdapter;
import com.example.muzic.databinding.ActivityMainBinding;
import com.example.muzic.databinding.PlayBarBinding;
import com.example.muzic.model.MoodPlaylist;
import com.example.muzic.records.ProfilePicture;
import com.example.muzic.model.TrackData;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.network.AudiusRepository;
import com.example.muzic.records.Artwork;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.CoverPhoto;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.example.muzic.services.MusicService;
import com.example.muzic.utils.SettingsSharedPrefManager;
import com.example.muzic.utils.SharedPreferenceManager;
import com.example.muzic.utils.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@UnstableApi
public class MainActivity extends AppCompatActivity implements Player.Listener {
    private static final String TAG = "AudiusAPI";
    private ExoPlayer player;
    private TrendingTracksAdapter trendingTracksAdapter;
    private PopularUserAdapter popularUserAdapter;
    private TrendingPlaylistAdapter trendingPlaylistAdapter;
    private MoodPlaylistAdapter moodPlaylistAdapter;
    private MainSavedLibrariesAdapter savedLibrariesAdapter;
    private ActivityMainBinding binding;
    private PlayBarBinding playBarBinding;
    private SlidingRootNav slidingRootNavBuilder;
    private TrackData currentTrack;
    private AudiusRepository audiusRepository;
    private Player.Listener playerListener;
    private SharedPreferenceManager sharedPreferenceManager;
    private final android.content.BroadcastReceiver uiReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            // Nếu cần, xử lý logic đặc biệt ở đây
            // Ví dụ: updatePlayControls(); hoặc các side-effect khác
            updatePlayControls();
        }
    };

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

        // Add player listener
        playerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                updatePlayControls();
                // Check if playback has ended
                if (state == Player.STATE_ENDED) {
                    playNextTrack();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlayControls();
            }
        };
        player.addListener(playerListener);

        // Initialize managers
        audiusRepository = new AudiusRepository();
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this);

        // Setup navigation drawer
        slidingRootNavBuilder = new SlidingRootNavBuilder(this)
                .withMenuLayout(R.layout.main_drawer_layout)
                .withContentClickableWhenMenuOpened(false)
                .withDragDistance(250)
                .inject();
        binding.profileIcon.setOnClickListener(view -> slidingRootNavBuilder.openMenu(true));
        
        // Add search button click handler
        binding.searchIcon.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        onDrawerItemsClicked();

        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Setup SwipeRefreshLayout
        binding.refreshLayout.setOnRefreshListener(() -> {
            // Reset shimmer state
            if (binding.songsShimmerContainer != null) {
                binding.songsShimmerContainer.setVisibility(View.VISIBLE);
            }
            if (binding.artistsShimmerContainer != null) {
                binding.artistsShimmerContainer.setVisibility(View.VISIBLE);
            }
            if (binding.playlistsShimmerContainer != null) {
                binding.playlistsShimmerContainer.setVisibility(View.VISIBLE);
            }
            if (binding.popularSongsRecyclerView != null) {
                binding.popularSongsRecyclerView.setVisibility(View.GONE);
            }
            if (binding.popularArtistsRecyclerView != null) {
                binding.popularArtistsRecyclerView.setVisibility(View.GONE);
            }
            if (binding.popularPlaylistRecyclerView != null) {
                binding.popularPlaylistRecyclerView.setVisibility(View.GONE);
            }
            
            // Reload all data
            loadTrendingTracks();
            loadSavedLibraries();
            
            // Hide refresh animation after 1 second
            new Handler().postDelayed(() -> {
                binding.refreshLayout.setRefreshing(false);
            }, 1000);
        });
        
        // Set SwipeRefreshLayout colors
        ThemeManager themeManager = new ThemeManager(this);
        binding.refreshLayout.setColorSchemeColors(themeManager.getPrimaryColor());
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(isDarkMode() ? 
            R.color.dark_background : android.R.color.white, getTheme()));
        
        // Setup play bar listeners
        setupPlayBarListeners();
        
        // Load data
        loadTrendingTracks();
        loadSavedLibraries();
        
        // Restore play bar state if needed
        restorePlayBarState();

        // Link xac thuc email
        Intent intent = getIntent();
        Uri deepLink = intent.getData();

        if (deepLink != null && FirebaseAuth.getInstance().isSignInWithEmailLink(deepLink.toString())) {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String email = prefs.getString("emailForSignIn", null);

            if (email != null) {
                FirebaseAuth.getInstance().signInWithEmailLink(email, deepLink.toString())
                        .addOnSuccessListener(authResult -> {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent newintent = new Intent(this, MainActivity.class);
                            newintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(newintent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "No email found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupRecyclerViews() {
        // Trending Tracks
        binding.popularSongsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        trendingTracksAdapter = new TrendingTracksAdapter(this, track -> {
            // Play the track first
            playTrack(track);
            // Then open MusicOverview
            openMusicOverview();
        });
        binding.popularSongsRecyclerView.setAdapter(trendingTracksAdapter);

        // Popular Users
        binding.popularArtistsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        popularUserAdapter = new PopularUserAdapter(this, new ArrayList<>(), user -> {
            Intent intent = new Intent(this, ArtistProfileActivity.class);
            intent.putExtra("data", new Gson().toJson(user));
            startActivity(intent);
        });
        binding.popularArtistsRecyclerView.setAdapter(popularUserAdapter);

        // Popular Playlists
        binding.popularPlaylistRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        trendingPlaylistAdapter = new TrendingPlaylistAdapter(this, new ArrayList<>());
        binding.popularPlaylistRecyclerView.setAdapter(trendingPlaylistAdapter);

        // Saved Libraries
        binding.savedRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        savedLibrariesAdapter = new MainSavedLibrariesAdapter(this, new ArrayList<>());
        binding.savedRecyclerView.setAdapter(savedLibrariesAdapter);

        // Mood
        binding.moodPlaylistRecyclerView.setLayoutManager(
                new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)
        );
        moodPlaylistAdapter = new MoodPlaylistAdapter(this, new ArrayList<>());
        binding.moodPlaylistRecyclerView.setAdapter(moodPlaylistAdapter);

        // Initialize shimmer containers
        if (binding.songsShimmerContainer != null) {
            binding.songsShimmerContainer.setVisibility(View.VISIBLE);
        }
        if (binding.artistsShimmerContainer != null) {
            binding.artistsShimmerContainer.setVisibility(View.VISIBLE);
        }
        if (binding.playlistsShimmerContainer != null) {
            binding.playlistsShimmerContainer.setVisibility(View.VISIBLE);
        }
        if (binding.popularSongsRecyclerView != null) {
            binding.popularSongsRecyclerView.setVisibility(View.GONE);
        }
        if (binding.popularArtistsRecyclerView != null) {
            binding.popularArtistsRecyclerView.setVisibility(View.GONE);
        }
        if (binding.popularPlaylistRecyclerView != null) {
            binding.popularPlaylistRecyclerView.setVisibility(View.GONE);
        }
    }

    private void loadSavedLibraries() {
        if (sharedPreferenceManager != null) {
            sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
                List<Playlist> savedPlaylists = new ArrayList<>();
                if (savedLibraries != null && savedLibraries.lists() != null) {
                    for (SavedLibrariesAudius.Library library : savedLibraries.lists()) {
                        // Convert Library sang Playlist
                        Playlist convertedPlaylist = new Playlist(
                            new Artwork("", "", ""),
                            library.description(),
                            library.id(),
                            library.id(),
                            false,
                            library.name(),
                            0, 0, 0,
                            new User(0, "", "", new CoverPhoto("",""), 0, 0, false,
                                    "local", "local", false, "", "Local Library", 0,
                                    new ProfilePicture("","",""), 0, 0, false, true,
                                    "", "", 0, 0, 0)
                        );
                        savedPlaylists.add(convertedPlaylist);
                    }
                }
                if (!savedPlaylists.isEmpty()) {
                    savedLibrariesAdapter.updatePlaylists(savedPlaylists);
                    binding.savedLibrariesSection.setVisibility(View.VISIBLE);
                } else {
                    binding.savedLibrariesSection.setVisibility(View.GONE);
                }
            });
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openMusicOverview() {
        if (currentTrack != null) {
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
        
        // Update playlist in ApplicationClass and start playing
        ApplicationClass app = (ApplicationClass) getApplication();
        app.updatePlaylist(playlist, trendingTracksAdapter.getCurrentTrackIndex());
        app.playCurrentTrack();

        // Start MusicService
        Intent serviceIntent = new Intent(this, MusicService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
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
        // Start shimmer animations
        if (binding.songsShimmerContainer != null) {
            binding.songsShimmerContainer.startShimmer();
        }
        if (binding.artistsShimmerContainer != null) {
            binding.artistsShimmerContainer.startShimmer();
        }
        if (binding.playlistsShimmerContainer != null) {
            binding.playlistsShimmerContainer.startShimmer();
        }

        // Load trending tracks
        audiusRepository.getTrendingTracks(15, new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null && !response.body().data().isEmpty()) {
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

                    // Hide shimmer and show recycler views
                    if (binding.songsShimmerContainer != null) {
                        binding.songsShimmerContainer.stopShimmer();
                        binding.songsShimmerContainer.setVisibility(View.GONE);
                    }
                    if (binding.artistsShimmerContainer != null) {
                        binding.artistsShimmerContainer.stopShimmer();
                        binding.artistsShimmerContainer.setVisibility(View.GONE);
                    }
                    if (binding.popularSongsRecyclerView != null) {
                        binding.popularSongsRecyclerView.setVisibility(View.VISIBLE);
                    }
                    if (binding.popularArtistsRecyclerView != null) {
                        binding.popularArtistsRecyclerView.setVisibility(View.VISIBLE);
                    }

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

                    //Log.d("MoodPlaylist", "Mood playlists size: " + moodPlaylists.size());
                    //for (MoodPlaylist playlist : moodPlaylists) {
                        //Log.d("MoodPlaylist", "Title: " + playlist.getMood());
                    //}
                    // Cập nhật adapter
                    moodPlaylistAdapter.setMoodPlaylists(moodPlaylists);
                } else {
                    // No data available
                    if (binding.songsShimmerContainer != null) {
                        binding.songsShimmerContainer.stopShimmer();
                        binding.songsShimmerContainer.setVisibility(View.GONE);
                    }
                    if (binding.artistsShimmerContainer != null) {
                        binding.artistsShimmerContainer.stopShimmer();
                        binding.artistsShimmerContainer.setVisibility(View.GONE);
                    }
                    // Optionally hide sections if no data
                    // binding.popularSongsSection.setVisibility(View.GONE);
                    // binding.popularArtistsSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                // Hide shimmer and show error state if needed
                if (binding.songsShimmerContainer != null) {
                    binding.songsShimmerContainer.stopShimmer();
                    binding.songsShimmerContainer.setVisibility(View.GONE);
                }
                if (binding.artistsShimmerContainer != null) {
                    binding.artistsShimmerContainer.stopShimmer();
                    binding.artistsShimmerContainer.setVisibility(View.GONE);
                }
                if (binding.playlistsShimmerContainer != null) {
                    binding.playlistsShimmerContainer.stopShimmer();
                    binding.playlistsShimmerContainer.setVisibility(View.GONE);
                }
                // Optionally hide sections on failure
                // binding.popularSongsSection.setVisibility(View.GONE);
                // binding.popularArtistsSection.setVisibility(View.GONE);
                // binding.popularPlaylistsSection.setVisibility(View.GONE);
            }
        });

        // Load trending playlists
        audiusRepository.getTrendingPlaylists(10, new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data() != null && !response.body().data().isEmpty()) {
                    trendingPlaylistAdapter.setPlaylists(response.body().data());
                    // Hide shimmer and show RecyclerView
                    if (binding.playlistsShimmerContainer != null) {
                        binding.playlistsShimmerContainer.stopShimmer();
                        binding.playlistsShimmerContainer.setVisibility(View.GONE);
                    }
                    if (binding.popularPlaylistRecyclerView != null) {
                        binding.popularPlaylistRecyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    // No data available
                    if (binding.playlistsShimmerContainer != null) {
                        binding.playlistsShimmerContainer.stopShimmer();
                        binding.playlistsShimmerContainer.setVisibility(View.GONE);
                    }
                    // Optionally hide the entire section if no playlists
                    // binding.popularPlaylistsSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                Log.e(TAG, "Playlist API Call Failed: " + t.getMessage());
                // Hide shimmer on failure
                if (binding.playlistsShimmerContainer != null) {
                    binding.playlistsShimmerContainer.stopShimmer();
                    binding.playlistsShimmerContainer.setVisibility(View.GONE);
                }
                // Optionally show error state or hide section
                // binding.popularPlaylistsSection.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners before clearing player reference
        if (player != null) {
            player.removeListener(playerListener);
        }
        // Don't release the player since it's shared
        player = null;
    }

    @OptIn(markerClass = UnstableApi.class)
    @UnstableApi
    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.addListener(this);
            updatePlayControls();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(uiReceiver, new android.content.IntentFilter("com.example.muzic.ACTION_UI_UPDATE"));
        // Reapply theme when activity resumes
        ((ApplicationClass) getApplication()).reapplyTheme();
        
        // Update play bar colors based on theme
        updatePlayBarThemeColors();

        // Setup slidingRootNav
        onDrawerItemsClicked();
        
        // Reload saved libraries as they might have changed
        loadSavedLibraries();
        
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

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.removeListener(this);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiReceiver);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updatePlayBarThemeColors() {
        SettingsSharedPrefManager settingsManager = new SettingsSharedPrefManager(this);
        String currentTheme = settingsManager.getTheme();
        int themeColor;
        
        // Get color based on current theme
        switch (currentTheme) {
            case "nebula":
                themeColor = getResources().getColor(R.color.nebula, getTheme());
                break;
            case "aqua":
                themeColor = getResources().getColor(R.color.aqua, getTheme());
                break;
            case "tangerine":
                themeColor = getResources().getColor(R.color.tangerine, getTheme());
                break;
            case "crimson love":
                themeColor = getResources().getColor(R.color.crimson_love, getTheme());
                break;
            case "blue depths":
                themeColor = getResources().getColor(R.color.blue_depths, getTheme());
                break;
            default: // original
                themeColor = getResources().getColor(R.color.spotify_green, getTheme());
                break;
        }

        if (ApplicationClass.currentTrack == null) {
            // Set background color for cover image
            binding.playBarCoverImage.setBackgroundColor(themeColor);
            binding.playBarCoverImage.setImageResource(R.drawable.music_note_24px);
            binding.playBarCoverImage.setColorFilter(Color.WHITE);
            
            // Set text colors
            binding.playBarMusicTitle.setTextColor(themeColor);
            binding.playBarMusicDesc.setTextColor(themeColor);
        } else {
            // When track is playing, update text colors
            binding.playBarMusicTitle.setTextColor(themeColor);
            binding.playBarMusicDesc.setTextColor(themeColor);
        }
        
        // Always update control buttons color
        binding.playBarPlayPauseIcon.setImageTintList(ColorStateList.valueOf(themeColor));
        binding.playBarPrevIcon.setImageTintList(ColorStateList.valueOf(themeColor));
        binding.playBarNextIcon.setImageTintList(ColorStateList.valueOf(themeColor));
    }

    //khuc menu la cai ham nay ne
    private void onDrawerItemsClicked() {
        slidingRootNavBuilder.getLayout().findViewById(R.id.settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            slidingRootNavBuilder.closeMenu();
        });

        View userView = slidingRootNavBuilder.getLayout().findViewById(R.id.user);
        View loginView = slidingRootNavBuilder.getLayout().findViewById(R.id.login);

        TextView loginText = loginView.findViewById(R.id.text);
        TextView welcome = slidingRootNavBuilder.getLayout().findViewById(R.id.tv_welcome);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            userView.setVisibility(View.GONE);
        } else {
            userView.setVisibility(View.VISIBLE);
            userView.setOnClickListener(v -> {
                startActivity(new Intent(this, UserActivity.class));
                slidingRootNavBuilder.closeMenu();
            });
            loginText.setText("Log out");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userID = user.getUid();

            db.collection("users")
                    .document(userID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String welcomeName = documentSnapshot.getString("name");
                            if (welcomeName != null) {
                                welcome.setText("Welcome back " + welcomeName);
                            } else {
                                welcome.setText("Welcome back");
                            }
                        } else {
                            welcome.setText("Welcome back");
                        }
                    })
                    .addOnFailureListener(e -> {
                        welcome.setText("Welcome back");
                    });
        }

        slidingRootNavBuilder.getLayout().findViewById(R.id.logo).setOnClickListener(view -> slidingRootNavBuilder.closeMenu());

        slidingRootNavBuilder.getLayout().findViewById(R.id.library).setOnClickListener(view -> {
            if (user == null) {
                Toast.makeText(MainActivity.this, "You need to log in!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(MainActivity.this, SavedLibrariesActivity.class));
                slidingRootNavBuilder.closeMenu();
            }
        });

        slidingRootNavBuilder.getLayout().findViewById(R.id.login).setOnClickListener(view -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                slidingRootNavBuilder.closeMenu();
            } else {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                slidingRootNavBuilder.closeMenu();
                finish();
            }

        });

        slidingRootNavBuilder.getLayout().findViewById(R.id.about).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            slidingRootNavBuilder.closeMenu();
        });
    }
    private String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Update theme colors
        ApplicationClass app = (ApplicationClass) getApplication();
        app.reapplyTheme();
        
        // Update play bar colors
        updatePlayBarThemeColors();
        
        // Update SwipeRefreshLayout colors
        ThemeManager themeManager = new ThemeManager(this);
        binding.refreshLayout.setColorSchemeColors(themeManager.getPrimaryColor());
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(isDarkMode() ? 
            R.color.dark_background : android.R.color.white, getTheme()));
        
        // Update playback state if needed
        if (player != null) {
            updatePlayControls();
        }
    }

    private void updatePlayControls() {
        if (player != null) {
            runOnUiThread(() -> {
                binding.playBarPlayPauseIcon.setImageResource(
                    player.isPlaying() ? 
                    R.drawable.baseline_pause_24 : 
                    R.drawable.play_arrow_24px
                );
            });
        }
    }

    private void updatePlayBarContent(TrackData track) {
        if (track == null) return;
        
        runOnUiThread(() -> {
            binding.playBarBackground.setVisibility(View.VISIBLE);
            binding.playBarMusicTitle.setText(track.title);
            binding.playBarMusicDesc.setText(track.user != null ? track.user.name : "");
            
            // Clear any background color and tint from theme
            binding.playBarCoverImage.setBackgroundColor(Color.TRANSPARENT);
            binding.playBarCoverImage.setColorFilter(null);
            
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
    
    private boolean isDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & 
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        runOnUiThread(this::updatePlayControls);
    }
}

