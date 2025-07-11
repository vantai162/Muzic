package com.example.muzic.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.adapter.SelectLibraryAdapter;
import com.example.muzic.databinding.AddNewLibraryBottomSheetBinding;
import com.example.muzic.records.Artwork;
import com.example.muzic.records.CoverPhoto;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.ProfilePicture;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.example.muzic.records.UserResponse;
import com.example.muzic.utils.AudioQualityManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.databinding.ActivityMusicOverviewBinding;
import com.example.muzic.databinding.MusicOverviewMoreInfoBottomSheetBinding;
import com.example.muzic.model.TrackData;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.utility.RequestNetwork;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.example.muzic.services.ActionPlaying;
import com.example.muzic.utils.SharedPreferenceManager;
import com.example.muzic.utils.customview.BottomSheetItemView;
import com.squareup.picasso.Picasso;
import com.example.muzic.utils.BlurUtils;
import com.example.muzic.utils.SettingsSharedPrefManager;
import com.example.muzic.utils.ThemeManager;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.records.AudiusUserResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

@UnstableApi
public class MusicOverviewActivity extends AppCompatActivity implements Player.Listener {
    private ActivityMusicOverviewBinding binding;
    private ExoPlayer player;
    private Handler handler;
    private ArrayList<TrackData> playlist;
    private ArrayList<TrackData> originalPlaylist;
    private int currentTrackIndex = 0;
    private boolean isShuffleEnabled = false;
    private int repeatMode = Player.REPEAT_MODE_OFF;
    TextView quality;
    private AudioQualityManager audioQualityManager;
    private BottomSheetDialog moreInfoBottomSheet;
    private SettingsSharedPrefManager settingsManager;
    private ImageView backgroundImageView;
    private boolean isBlurring = false;

    private final android.content.BroadcastReceiver uiReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            // Nếu cần, xử lý logic đặc biệt ở đây
            // Ví dụ: updatePlayPauseButton(player.isPlaying()); hoặc các side-effect khác
            updatePlayPauseButton(player.isPlaying());
        }
    };

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Add configuration change handling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        
        // Set up window to draw behind system bars
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        binding = ActivityMusicOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize settings manager
        settingsManager = new SettingsSharedPrefManager(this);
        
        // Initialize background image view using view binding
        backgroundImageView = binding.backgroundImage;

        // Get application instance
        ApplicationClass app = (ApplicationClass) getApplication();
        
        // Get shared ExoPlayer instance
        player = app.getExoPlayer();
        player.addListener(this);
        handler = new Handler(Looper.getMainLooper());

        // Initialize shuffle and repeat buttons with default state
        int defaultColor = getResources().getColor(isDarkMode() ? R.color.textSecDark : R.color.textSec, getTheme());
        ThemeManager themeManager = new ThemeManager(this);
        
        // Restore shuffle state
        isShuffleEnabled = ApplicationClass.isShuffleEnabled;
        binding.shuffleIcon.setImageTintList(ColorStateList.valueOf(
            isShuffleEnabled ? themeManager.getPrimaryColor() : defaultColor
        ));
        player.setShuffleModeEnabled(isShuffleEnabled);

        // Restore repeat state
        repeatMode = ApplicationClass.repeatMode;
        switch (repeatMode) {
            case Player.REPEAT_MODE_ONE:
                binding.repeatIcon.setScaleX(-1);
                binding.repeatIcon.setImageTintList(ColorStateList.valueOf(themeManager.getPrimaryColor()));
                break;
            case Player.REPEAT_MODE_ALL:
                binding.repeatIcon.setScaleX(1);
                binding.repeatIcon.setImageTintList(ColorStateList.valueOf(themeManager.getPrimaryColor()));
                break;
            default:
                binding.repeatIcon.setScaleX(1);
                binding.repeatIcon.setImageTintList(ColorStateList.valueOf(defaultColor));
                break;
        }
        player.setRepeatMode(repeatMode);

        // Setup more info bottom sheet
        setupMoreInfoBottomSheet();

        // Get playlist from ApplicationClass and store original order
        playlist = app.currentPlaylist;
        originalPlaylist = new ArrayList<>(playlist);
        currentTrackIndex = app.currentTrackIndex;
        
        // Update UI with current track
        if (app.currentTrack != null) {
            updateUIOnly(app.currentTrack);
        }

        setupClickListeners();
        setupSeekBar();
        
        // Update UI based on current player state
        if (player != null) {
            updatePlayPauseButton(player.isPlaying());
        }

        quality = findViewById(R.id.track_quality);
        audioQualityManager = new AudioQualityManager(this);
        quality.setText(audioQualityManager.getCurrentQuality());

        // Setup background blur if enabled
        updateBackgroundBlur();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupClickListeners() {
        // Play/Pause button
        binding.playPauseImage.setOnClickListener(v -> togglePlayPause());

        // Next button
        binding.nextIcon.setOnClickListener(v -> playNextTrack());

        // Previous button
        binding.prevIcon.setOnClickListener(v -> playPreviousTrack());

        // More info button
        binding.moreIcon.setOnClickListener(v -> showMoreInfoBottomSheet());

        // Share button
        binding.shareIcon.setOnClickListener(v -> {
            String songUrl = ApplicationClass.SONG_URL;
            Log.d("ShareDebug", "Song URL: " + songUrl);

            if (songUrl != null && !songUrl.isEmpty()) {
                // Convert streaming URL to share URL
                // From: https://discoveryprovider2.audius.co/v1/tracks/ID/stream
                // To: https://audius.co/tracks/ID
                String trackId = songUrl.split("/tracks/")[1].replace("/stream", "");
                String shareUrl = "https://audius.co/tracks/" + trackId;

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
                startActivity(Intent.createChooser(shareIntent, "Share track via"));
            } else {
                Toast.makeText(this, "Cannot share this track", Toast.LENGTH_SHORT).show();
            }
        });

        // Shuffle button
        binding.shuffleIcon.setOnClickListener(v -> {
            isShuffleEnabled = !isShuffleEnabled;
            ApplicationClass.isShuffleEnabled = isShuffleEnabled; // Save to ApplicationClass
            player.setShuffleModeEnabled(isShuffleEnabled);
            
            // Shuffle or restore playlist
            shufflePlaylist();
            
            // Update shuffle button color
            ThemeManager themeManager = new ThemeManager(this);
            binding.shuffleIcon.setImageTintList(ColorStateList.valueOf(
                isShuffleEnabled ? themeManager.getPrimaryColor() : 
                getResources().getColor(isDarkMode() ? R.color.textSecDark : R.color.textSec, getTheme())
            ));
        });

        // Repeat button
        binding.repeatIcon.setOnClickListener(v -> {
            ThemeManager themeManager = new ThemeManager(this);
            switch (repeatMode) {
                case Player.REPEAT_MODE_OFF:
                    repeatMode = Player.REPEAT_MODE_ONE;
                    binding.repeatIcon.setScaleX(-1);
                    binding.repeatIcon.setImageTintList(ColorStateList.valueOf(themeManager.getPrimaryColor()));
                    break;
                case Player.REPEAT_MODE_ONE:
                    repeatMode = Player.REPEAT_MODE_ALL;
                    binding.repeatIcon.setScaleX(1);
                    binding.repeatIcon.setImageTintList(ColorStateList.valueOf(themeManager.getPrimaryColor()));
                    break;
                default:
                    repeatMode = Player.REPEAT_MODE_OFF;
                    binding.repeatIcon.setScaleX(1);
                    binding.repeatIcon.setImageTintList(ColorStateList.valueOf(
                        getResources().getColor(isDarkMode() ? R.color.textSecDark : R.color.textSec, getTheme())
                    ));
                    break;
            }
            ApplicationClass.repeatMode = repeatMode; // Save to ApplicationClass
            player.setRepeatMode(repeatMode);
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupSeekBar() {
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress * 1000L);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update seekbar progress
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    binding.seekbar.setProgress((int) (player.getCurrentPosition() / 1000));
                    binding.elapsedDuration.setText(formatDuration(player.getCurrentPosition()));
                }
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void startMusicServiceIfNeeded() {
        Intent serviceIntent = new Intent(this, com.example.muzic.services.MusicService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updateTrackUI(TrackData track) {
        startMusicServiceIfNeeded();
        binding.title.setText(track.title);
        binding.description.setText(track.user.name);
        if (track.artwork != null && track.artwork._480x480 != null) {
            Picasso.get()
                    .load(track.artwork._480x480)
                    .into(binding.coverImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // After artwork is loaded, update background blur
                            updateBackgroundBlur();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("BlurDebug", "Error loading artwork", e);
                        }
                    });
        }
        
        // Set total duration
        binding.totalDuration.setText(formatDuration(track.duration * 1000L));
        binding.seekbar.setMax(track.duration);

        // Get current playback position
        long currentPosition = player.getCurrentPosition();

        // Only prepare and play if this is a completely new track
        String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + track.id + "/stream";
        if (!isCurrentTrack(track)) {
            MediaItem mediaItem = MediaItem.fromUri(streamUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        } else {
            // If it's the same track, maintain the current position
            binding.seekbar.setProgress((int) (currentPosition / 1000));
            binding.elapsedDuration.setText(formatDuration(currentPosition));
        }
        
        // Always update play/pause button state based on current player state
        updatePlayPauseButton(player.isPlaying());
    }

    @OptIn(markerClass = UnstableApi.class)
    @UnstableApi
    private boolean isCurrentTrack(TrackData track) {
        if (player.getCurrentMediaItem() == null) return false;
        
        String currentUrl = player.getCurrentMediaItem().mediaId;
        String newUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + track.id + "/stream";
        
        // Also check if the track IDs match
        return currentUrl.equals(newUrl) || 
               (ApplicationClass.MUSIC_ID != null && ApplicationClass.MUSIC_ID.equals(String.valueOf(track.id)));
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updateUIOnly(TrackData track) {
        binding.title.setText(track.title);
        binding.description.setText(track.user.name);
        if (track.artwork != null && track.artwork._480x480 != null) {
            Picasso.get()
                .load(track.artwork._480x480)
                .into(binding.coverImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // After artwork is loaded, update background blur
                        updateBackgroundBlur();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("BlurDebug", "Error loading artwork", e);
                    }
                });
        }
        
        // Set total duration
        binding.totalDuration.setText(formatDuration(track.duration * 1000L));
        binding.seekbar.setMax(track.duration);
        
        // Update elapsed duration with current position
        binding.elapsedDuration.setText(formatDuration(player.getCurrentPosition()));
        
        // Update play/pause button state based on current player state
        updatePlayPauseButton(player.isPlaying());
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            binding.playPauseImage.setImageResource(R.drawable.baseline_pause_24);
        } else {
            binding.playPauseImage.setImageResource(R.drawable.play_arrow_24px);
        }
        binding.playPauseImage.setRotation(0);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void togglePlayPause() {
        boolean wasPlaying = player.isPlaying();
        if (wasPlaying) {
            player.pause();
        } else {
            player.play();
            startMusicServiceIfNeeded();
        }
        updatePlayPauseButton(player.isPlaying());
    }

    @OptIn(markerClass = UnstableApi.class)
    @UnstableApi
    private void playNextTrack() {
        startMusicServiceIfNeeded();
        if (playlist == null || playlist.isEmpty()) return;
        
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
        ApplicationClass app = (ApplicationClass) getApplication();
        app.currentTrackIndex = currentTrackIndex;
        app.currentTrack = playlist.get(currentTrackIndex);
        
        updateUIOnly(app.currentTrack);
        // Prepare and play the track
        String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + app.currentTrack.id + "/stream";
        MediaItem mediaItem = MediaItem.fromUri(streamUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        
        updateBackgroundBlur();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void playPreviousTrack() {
        startMusicServiceIfNeeded();
        if (playlist == null || playlist.isEmpty()) return;
        
        currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
        ApplicationClass app = (ApplicationClass) getApplication();
        app.currentTrackIndex = currentTrackIndex;
        app.currentTrack = playlist.get(currentTrackIndex);
        
        updateUIOnly(app.currentTrack);
        // Prepare and play the track
        String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + app.currentTrack.id + "/stream";
        MediaItem mediaItem = MediaItem.fromUri(streamUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        
        updateBackgroundBlur();
    }

    private String formatDuration(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onPlaybackStateChanged(int state) {
        if (state == Player.STATE_ENDED && repeatMode == Player.REPEAT_MODE_OFF) {
            playNextTrack();
        }
        // Update play/pause button whenever playback state changes
        updatePlayPauseButton(player.isPlaying());
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        // Don't release the player since it's shared
        if (player != null) {
            player.removeListener(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void backPress(View view) {
        // Call onBackPressed to ensure consistent behavior
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.addListener(this);
            updatePlayPauseButton(player.isPlaying());
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(uiReceiver, new android.content.IntentFilter("com.example.muzic.ACTION_UI_UPDATE"));
        updateBackgroundBlur();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.removeListener(this);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Update background blur when activity becomes visible
        updateBackgroundBlur();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupMoreInfoBottomSheet() {
        MusicOverviewMoreInfoBottomSheetBinding bottomSheetBinding = MusicOverviewMoreInfoBottomSheetBinding.inflate(getLayoutInflater());
        moreInfoBottomSheet = new BottomSheetDialog(this);
        moreInfoBottomSheet.setContentView(bottomSheetBinding.getRoot());

        // Get current track from ApplicationClass since it's more reliable
        ApplicationClass app = (ApplicationClass) getApplication();
        TrackData currentTrack = app.currentTrack;

        if (currentTrack != null) {
            // Set track info in bottom sheet
            if (currentTrack.artwork != null && currentTrack.artwork._480x480 != null) {
                Picasso.get().load(currentTrack.artwork._480x480).into(bottomSheetBinding.coverImage);
            }
            bottomSheetBinding.albumTitle.setText(currentTrack.title);
            bottomSheetBinding.albumSubTitle.setText(currentTrack.user.name);
        }

        // Setup click listeners for bottom sheet actions
        bottomSheetBinding.goToAlbum.setOnClickListener(v -> {
            if (ApplicationClass.currentTrack != null) {
                TrackData curTrack = ApplicationClass.currentTrack;
                // Validate user id
                Log.d("MusicOverview", "curTrack.user: " + (curTrack.user != null ? new Gson().toJson(curTrack.user) : "null"));
                Log.d("MusicOverview", "curTrack.user.id: " + (curTrack.user != null ? curTrack.user.id : "null"));
                if (curTrack.user == null || curTrack.user.id == null || curTrack.user.id.isEmpty()) {
                    Log.e("MusicOverview", "Invalid artist id");
                    Toast.makeText(MusicOverviewActivity.this, "Artist id not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("MusicOverview", "Fetching artist data for id: " + curTrack.user.id);
                // Fetch user data directly from API by id
                AudiusApiService apiService = AudiusApiClient.getInstance();
                apiService.getUserById(curTrack.user.id).enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                        try {
                            String raw = response.errorBody() != null ? response.errorBody().string() : new Gson().toJson(response.body());
                            Log.d("MusicOverview", "Raw user API response: " + raw);
                        } catch (Exception e) {
                            Log.e("MusicOverview", "Error reading raw response", e);
                        }
                        if (response.isSuccessful() && response.body() != null && response.body().data() != null) {
                            User user = response.body().data();
                            Log.d("MusicOverview", "Fetched user: " + new Gson().toJson(user));
                            Log.d("MusicOverview", "User id: " + user.id());
                            Intent intent = new Intent(MusicOverviewActivity.this, ArtistProfileActivity.class);
                            intent.putExtra("data", new Gson().toJson(user));
                            startActivity(intent);
                            moreInfoBottomSheet.dismiss();
                        } else {
                            Log.e("MusicOverview", "User API failed. Code: " + response.code());
                            if (response.errorBody() != null) {
                                try {
                                    Log.e("MusicOverview", "Error body: " + response.errorBody().string());
                                } catch (IOException e) {
                                    Log.e("MusicOverview", "Error reading error body", e);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                        Log.e("MusicOverview", "API call failed", t);
                        Toast.makeText(MusicOverviewActivity.this, "Failed to load artist information: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Artist information not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to library
        bottomSheetBinding.addToLibrary.setOnClickListener(v -> {
            if (currentTrack != null) {
                // Show select library bottom sheet
                View selectLibraryView = getLayoutInflater().inflate(R.layout.select_library_bottom_sheet, null);
                BottomSheetDialog selectLibraryDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
                selectLibraryDialog.setContentView(selectLibraryView);

                // Get views
                RecyclerView librariesRecyclerView = selectLibraryView.findViewById(R.id.libraries_recycler_view);
                TextView emptyText = selectLibraryView.findViewById(R.id.empty_text);
                MaterialButton createNewLibrary = selectLibraryView.findViewById(R.id.create_new_library);

                // Get saved playlists
                SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
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

                    // Setup RecyclerView
                    librariesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    SelectLibraryAdapter adapter = new SelectLibraryAdapter(savedPlaylists, library -> {
                        // Get existing library data
                        sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(savedLibraries2 -> {
                            SavedLibrariesAudius.Library existingLibrary = null;

                            if (savedLibraries2 != null && savedLibraries2.lists() != null) {
                                for (SavedLibrariesAudius.Library lib : savedLibraries2.lists()) {
                                    if (lib.id().equals(library.id())) {
                                        existingLibrary = lib;
                                        break;
                                    }
                                }
                            }

                            // Create new track to add
                            Track newTrack = new Track(
                                    currentTrack.artwork != null ? new Artwork(currentTrack.artwork._150x150, currentTrack.artwork._480x480, currentTrack.artwork._1000x1000) : null,
                                    currentTrack.description,
                                    currentTrack.genre,
                                    currentTrack.id,
                                    currentTrack.track_cid,
                                    currentTrack.mood,
                                    currentTrack.release_date,
                                    currentTrack.repost_count,
                                    currentTrack.favorite_count,
                                    currentTrack.tags,
                                    currentTrack.title,
                                    new User(
                                            0, "", "", new CoverPhoto("", ""),
                                            0, 0, false,
                                            currentTrack.user.name, currentTrack.user.id,
                                            false, "", currentTrack.user.name,
                                            0, new ProfilePicture("", "", ""),
                                            0, 0, false, true,
                                            "", "", 0, 0, 0
                                    ),
                                    Integer.parseInt(String.valueOf(currentTrack.duration)),
                                    currentTrack.downloadable,
                                    currentTrack.play_count,
                                    currentTrack.permalink,
                                    currentTrack.is_streamable
                            );

                            // Create updated library with existing tracks plus new track
                            List<Track> updatedTracks = new ArrayList<>();
                            if (existingLibrary != null && existingLibrary.tracks() != null) {
                                // Check if track already exists in the library
                                boolean trackExists = false;
                                for (Track track : existingLibrary.tracks()) {
                                    if (track.id().equals(newTrack.id())) {
                                        trackExists = true;
                                        break;
                                    }
                                }

                                if (trackExists) {
                                    Snackbar.make(binding.getRoot(), "Track already exists in this library", Snackbar.LENGTH_SHORT).show();
                                    selectLibraryDialog.dismiss();
                                    return;
                                }

                                updatedTracks.addAll(existingLibrary.tracks());
                            }
                            updatedTracks.add(newTrack);

                            // Get artwork from first track if this is the first track
                            String artworkUrl = library.artwork() != null ? library.artwork().x480() : "";
                            if (updatedTracks.size() == 1 && newTrack.artwork() != null) {
                                artworkUrl = newTrack.artwork().x480();
                            }

                            SavedLibrariesAudius.Library updatedLibrary = new SavedLibrariesAudius.Library(
                                    library.id(),
                                    false,
                                    false,
                                    library.playlistName(),
                                    artworkUrl,
                                    library.description(),
                                    updatedTracks
                            );

                            // Remove old library and add updated one
                            if (savedLibraries2 == null) {
                                savedLibraries2 = new SavedLibrariesAudius(new ArrayList<>());
                            }
                            if (savedLibraries2.lists() == null) {
                                savedLibraries2 = new SavedLibrariesAudius(new ArrayList<>());
                            }
                            savedLibraries2.lists().removeIf(lib -> lib.id().equals(library.id()));
                            savedLibraries2.lists().add(updatedLibrary);
                            sharedPreferenceManager.setSavedLibrariesData(savedLibraries2);

                            // Show success message
                            Snackbar.make(binding.getRoot(), "Added to " + library.playlistName(), Snackbar.LENGTH_SHORT).show();
                            selectLibraryDialog.dismiss();
                        });
                    });
                    librariesRecyclerView.setAdapter(adapter);

                    // Show empty state if no libraries
                    emptyText.setVisibility(savedPlaylists.isEmpty() ? View.VISIBLE : View.GONE);
                    librariesRecyclerView.setVisibility(savedPlaylists.isEmpty() ? View.GONE : View.VISIBLE);

                    // Handle create new library button
                    createNewLibrary.setOnClickListener(createLibraryView -> {
                        selectLibraryDialog.dismiss();
                        showAddLibraryDialog();
                    });

                    selectLibraryDialog.show();
                });
            }
            moreInfoBottomSheet.dismiss();
        });

        // Download track
        bottomSheetBinding.download.setOnClickListener(v -> {
            if (currentTrack != null) {
                // Show download progress dialog
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Downloading...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Start download in background
                new Thread(() -> {
                    try {
                        String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + currentTrack.id + "/stream";
                        // TODO: Implement actual download logic here
                        // For now, just simulate download
                        for (int i = 0; i <= 100; i++) {
                            Thread.sleep(50);
                            int finalI = i;
                            runOnUiThread(() -> progressDialog.setProgress(finalI));
                        }
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Snackbar.make(binding.getRoot(), "Download completed", Snackbar.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Snackbar.make(binding.getRoot(), "Download failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
            moreInfoBottomSheet.dismiss();
        });
    }

    private void showAddLibraryDialog() {
        AddNewLibraryBottomSheetBinding dialogBinding = AddNewLibraryBottomSheetBinding.inflate(getLayoutInflater());
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.cancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.create.setOnClickListener(v -> createNewLibrary(dialogBinding, dialog));

        dialog.show();
    }

    private void createNewLibrary(AddNewLibraryBottomSheetBinding dialogBinding, BottomSheetDialog dialog) {
        String name = dialogBinding.edittext.getText().toString().trim();
        if (name.isEmpty()) {
            dialogBinding.edittext.setError("Name cannot be empty");
            return;
        }

        long currentTime = System.currentTimeMillis();
        String playlistId = "local_" + currentTime;

        // Không cần tạo Playlist và lưu local nữa
        // Tạo và lưu Library vào Firestore
        SavedLibrariesAudius.Library newLibrary = new SavedLibrariesAudius.Library(
            playlistId,
            false,
            false,
            name,
            "",  // No artwork initially
            "Created on: " + formatMillis(currentTime),
            new ArrayList<>()  // Empty tracks list
        );
        SharedPreferenceManager.getInstance(this).addLibraryToSavedLibraries(newLibrary);
        dialog.dismiss();
        // Nếu cần, có thể cập nhật lại UI hoặc gọi lại hàm load danh sách thư viện
    }

    private void showMoreInfoBottomSheet() {
        if (moreInfoBottomSheet != null) {
            moreInfoBottomSheet.show();
        }
    }

    private boolean isDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & 
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private int getBackgroundColor() {
        return isDarkMode() ? 
            getResources().getColor(R.color.dark_background, getTheme()) :
            getResources().getColor(android.R.color.white, getTheme());
    }

    private void updateBackgroundBlur() {
        boolean isBlurEnabled = settingsManager.getBlurPlayerBackground();
        View darkOverlay = findViewById(R.id.darkOverlay);
        View backgroundContainer = findViewById(R.id.background_container);

        if (isBlurEnabled) {
            Drawable drawable = binding.coverImage.getDrawable();
            if (drawable == null) {
                // Blur ảnh mặc định NGAY LẬP TỨC nếu chưa có cover
                Bitmap defaultBitmap = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
                backgroundImageView.setVisibility(View.VISIBLE);
                isBlurring = true;
                com.example.muzic.utils.BlurUtils.applyBlur(this, defaultBitmap, backgroundImageView);
            } else {
                // Blur ảnh cover hiện tại
                backgroundImageView.setVisibility(View.VISIBLE);
                isBlurring = true;
                com.example.muzic.utils.BlurUtils.applyBlur(this, binding.coverImage, backgroundImageView);
            }
            // CHỈ set background container TRANSPARENT nếu đã blur xong
            if (isBlurring) {
                // Đang blur, giữ màu nền trắng hoặc dark_background
                int bgColor = isDarkMode() ? getResources().getColor(R.color.dark_background, getTheme()) : getResources().getColor(android.R.color.white, getTheme());
                backgroundContainer.setBackgroundColor(bgColor);
            } else {
                backgroundContainer.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

            // Show dark overlay for better contrast
            darkOverlay.setVisibility(View.VISIBLE);

            // Make system bars transparent
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);

            // Set system UI visibility based on theme
            if (isDarkMode()) {
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                );
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                );
            }

            // Update text and icon colors for blur mode
            updateTextColors(true);

            // Set back button to white for better visibility on blur background
            binding.backButton.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));

        } else {
            // Reset background container to theme color
            int backgroundColor = getBackgroundColor();
            backgroundContainer.setBackgroundColor(backgroundColor);

            // Hide blur elements
            backgroundImageView.setVisibility(View.GONE);
            darkOverlay.setVisibility(View.GONE);

            // Reset system bars to theme color
            getWindow().setStatusBarColor(backgroundColor);
            getWindow().setNavigationBarColor(backgroundColor);

            // Set system UI visibility based on theme
            if (isDarkMode()) {
                getWindow().getDecorView().setSystemUiVisibility(0); // Clear all flags for dark mode
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                );
            }

            // Clear any existing blur
            com.example.muzic.utils.BlurUtils.clearBlur(backgroundImageView);

            // Reset text and icon colors for normal mode
            updateTextColors(false);

            // Reset back button color based on theme
            binding.backButton.setImageTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(isDarkMode() ? android.R.color.white : R.color.textMain, getTheme())
            ));
        }
    }

    private void updateTextColors(boolean isBlurEnabled) {
        boolean isDark = isDarkMode();
        ThemeManager themeManager = new ThemeManager(this);
        
        int textColor = isBlurEnabled ? 
            getResources().getColor(android.R.color.white, getTheme()) :
            getResources().getColor(isDark ? android.R.color.white : R.color.textMain, getTheme());
        
        int secondaryTextColor = isBlurEnabled ? 
            getResources().getColor(android.R.color.white, getTheme()) :
            getResources().getColor(isDark ? R.color.textSecDark : R.color.textSec, getTheme());

        // Update text colors
        binding.title.setTextColor(textColor);
        binding.description.setTextColor(secondaryTextColor);
        binding.elapsedDuration.setTextColor(secondaryTextColor);
        binding.totalDuration.setTextColor(secondaryTextColor);
        binding.trackQuality.setTextColor(secondaryTextColor);

        // Update icons tint based on their states
        ColorStateList iconTint = themeManager.getColorStateList();
        binding.prevIcon.setImageTintList(iconTint);
        binding.nextIcon.setImageTintList(iconTint);
        binding.shareIcon.setImageTintList(iconTint);
        binding.moreIcon.setImageTintList(iconTint);

        // Update shuffle and repeat icons based on their states
        binding.shuffleIcon.setImageTintList(ColorStateList.valueOf(
            isShuffleEnabled ? themeManager.getPrimaryColor() : secondaryTextColor
        ));
        binding.repeatIcon.setImageTintList(ColorStateList.valueOf(
            repeatMode != Player.REPEAT_MODE_OFF ? themeManager.getPrimaryColor() : secondaryTextColor
        ));

        // Update seekbar colors
        binding.seekbar.setProgressTintList(iconTint);
        binding.seekbar.setThumbTintList(iconTint);

        // Update play/pause button colors
        binding.playPauseIconBg.setCardBackgroundColor(themeManager.getPrimaryColor());
        binding.playPauseImage.setImageTintList(ColorStateList.valueOf(Color.WHITE));
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Update UI for theme changes without recreating activity
        updateBackgroundBlur();
        
        // Update playbar state if music is playing
        if (player != null) {
            updatePlaybackState();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updatePlaybackState() {
        // Update play/pause button state
        updatePlayPauseButton(player.isPlaying());
        
        // Update current position
        if (player.getCurrentMediaItem() != null) {
            binding.seekbar.setProgress((int) (player.getCurrentPosition() / 1000));
            binding.elapsedDuration.setText(formatDuration(player.getCurrentPosition()));
        }
        
        // Update track info if available
        TrackData currentTrack = getIntent().getParcelableExtra("track");
        if (currentTrack != null && ApplicationClass.MUSIC_ID != null && 
            ApplicationClass.MUSIC_ID.equals(String.valueOf(currentTrack.id))) {
            binding.title.setText(currentTrack.title);
            binding.description.setText(currentTrack.user.name);
            if (currentTrack.artwork != null && currentTrack.artwork._480x480 != null) {
                Picasso.get()
                    .load(currentTrack.artwork._480x480)
                    .into(binding.coverImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            updateBackgroundBlur();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("PlaybackState", "Error loading artwork", e);
                        }
                    });
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String formatMillis(long millis) {
        return new SimpleDateFormat("MM-dd-yyyy HH:mm a").format(new Date(millis));
    }

    private void shufflePlaylist() {
        if (isShuffleEnabled) {
            // Save current track
            TrackData currentTrack = playlist.get(currentTrackIndex);
            
            // Create a temporary list without current track
            ArrayList<TrackData> tempList = new ArrayList<>(playlist);
            tempList.remove(currentTrackIndex);
            
            // Shuffle the temporary list
            Collections.shuffle(tempList);
            
            // Clear and rebuild playlist with current track at start
            playlist = new ArrayList<>();
            playlist.add(currentTrack);  // Add current track at the beginning
            playlist.addAll(tempList);   // Add shuffled remaining tracks
            
            // Update current index to 0 since current track is now first
            currentTrackIndex = 0;
        } else {
            // Restore original playlist order
            playlist = new ArrayList<>(originalPlaylist);
            
            // Find the current track in the original playlist
            TrackData currentTrack = ApplicationClass.currentTrack;
            currentTrackIndex = 0;
            for (int i = 0; i < originalPlaylist.size(); i++) {
                if (originalPlaylist.get(i).id.equals(currentTrack.id)) {
                    currentTrackIndex = i;
                    break;
                }
            }
        }
        
        // Update playlist in ApplicationClass
        ApplicationClass app = (ApplicationClass) getApplication();
        app.currentPlaylist = playlist;
        app.currentTrackIndex = currentTrackIndex;
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        runOnUiThread(() -> updatePlayPauseButton(isPlaying));
    }
}
