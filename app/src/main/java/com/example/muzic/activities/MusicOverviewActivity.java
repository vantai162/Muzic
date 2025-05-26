package com.example.muzic.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.util.UnstableApi;

import com.example.muzic.utils.AudioQualityManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.databinding.ActivityMusicOverviewBinding;
import com.example.muzic.databinding.MusicOverviewMoreInfoBottomSheetBinding;
//import com.example.muzic.model.AlbumItem;
import com.example.muzic.model.TrackData;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.utility.RequestNetwork;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.example.muzic.services.ActionPlaying;
//import com.example.muzic.services.MusicService;
import com.example.muzic.utils.SharedPreferenceManager;
//import com.example.muzic.utils.TrackCacheHelper;
import com.example.muzic.utils.customview.BottomSheetItemView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MusicOverviewActivity extends AppCompatActivity implements Player.Listener {
    private ActivityMusicOverviewBinding binding;
    private ExoPlayer player;
    private Handler handler;
    private List<TrackData> playlist;
    private int currentTrackIndex = 0;
    private boolean isShuffleEnabled = false;
    private int repeatMode = Player.REPEAT_MODE_OFF;
    TextView quality;
    private AudioQualityManager audioQualityManager;
    private BottomSheetDialog moreInfoBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get shared ExoPlayer instance
        player = ApplicationClass.player;
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            ApplicationClass.player = player;
        }
        player.addListener(this);
        handler = new Handler(Looper.getMainLooper());

        // Setup more info bottom sheet
        setupMoreInfoBottomSheet();

        // Get track data from intent
        TrackData currentTrack = getIntent().getParcelableExtra("track");
        playlist = new ArrayList<>();
        if (currentTrack != null) {
            playlist.add(currentTrack);
            // Store current track ID in ApplicationClass for reference
            ApplicationClass.MUSIC_ID = String.valueOf(currentTrack.id);
            
            // Update UI without restarting playback
            updateUIOnly(currentTrack);
            
            // Update seekbar to current position
            if (getIntent().hasExtra("currentPosition")) {
                long currentPosition = getIntent().getLongExtra("currentPosition", 0);
                binding.seekbar.setProgress((int) (currentPosition / 1000));
                binding.elapsedDuration.setText(formatDuration(currentPosition));
            }
        }

        setupClickListeners();
        setupSeekBar();
        
        // Update UI based on current player state
        if (player != null) {
            updatePlayPauseButton(player.isPlaying());
        }

        quality = (TextView) findViewById(R.id.track_quality);
        audioQualityManager = new AudioQualityManager(this);
        quality.setText(audioQualityManager.getCurrentQuality());
    }

    private void setupClickListeners() {
        // Play/Pause button
        binding.playPauseImage.setOnClickListener(v -> togglePlayPause());

        // Next button
        binding.nextIcon.setOnClickListener(v -> playNextTrack());

        // Previous button
        binding.prevIcon.setOnClickListener(v -> playPreviousTrack());

        // More info button
        binding.moreIcon.setOnClickListener(v -> showMoreInfoBottomSheet());

        // Shuffle button
        binding.shuffleIcon.setOnClickListener(v -> {
            isShuffleEnabled = !isShuffleEnabled;
            player.setShuffleModeEnabled(isShuffleEnabled);
            binding.shuffleIcon.setImageTintList(ColorStateList.valueOf(
                    isShuffleEnabled ? getColor(R.color.textMain) : getColor(R.color.textSec)
            ));
        });

        // Repeat button
        binding.repeatIcon.setOnClickListener(v -> {
            switch (repeatMode) {
                case Player.REPEAT_MODE_OFF:
                    repeatMode = Player.REPEAT_MODE_ONE;
                    binding.repeatIcon.setScaleX(-1);
                    binding.repeatIcon.setImageTintList(ColorStateList.valueOf(getColor(R.color.textMain)));
                    break;
                case Player.REPEAT_MODE_ONE:
                    repeatMode = Player.REPEAT_MODE_ALL;
                    binding.repeatIcon.setScaleX(1);
                    binding.repeatIcon.setImageTintList(ColorStateList.valueOf(getColor(R.color.textMain)));
                    break;
                default:
                    repeatMode = Player.REPEAT_MODE_OFF;
                    binding.repeatIcon.setScaleX(1);
                    binding.repeatIcon.setImageTintList(ColorStateList.valueOf(getColor(R.color.textSec)));
                    break;
            }
            player.setRepeatMode(repeatMode);
        });
    }

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

    private void updateTrackUI(TrackData track) {
        binding.title.setText(track.title);
        binding.description.setText(track.user.name);
        if (track.artwork != null && track.artwork._480x480 != null) {
            Picasso.get().load(track.artwork._480x480).into(binding.coverImage);
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

    private boolean isCurrentTrack(TrackData track) {
        if (player.getCurrentMediaItem() == null) return false;
        
        String currentUrl = player.getCurrentMediaItem().mediaId;
        String newUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + track.id + "/stream";
        
        // Also check if the track IDs match
        return currentUrl.equals(newUrl) || 
               (ApplicationClass.MUSIC_ID != null && ApplicationClass.MUSIC_ID.equals(String.valueOf(track.id)));
    }

    private void updateUIOnly(TrackData track) {
        binding.title.setText(track.title);
        binding.description.setText(track.user.name);
        if (track.artwork != null && track.artwork._480x480 != null) {
            Picasso.get().load(track.artwork._480x480).into(binding.coverImage);
        }
        
        // Set total duration
        binding.totalDuration.setText(formatDuration(track.duration * 1000L));
        binding.seekbar.setMax(track.duration);
        
        // Update elapsed duration with current position
        binding.elapsedDuration.setText(formatDuration(player.getCurrentPosition()));
        
        // Update play/pause button state based on current player state
        updatePlayPauseButton(player.isPlaying());
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            binding.playPauseImage.setImageResource(R.drawable.baseline_pause_24);
        } else {
            binding.playPauseImage.setImageResource(R.drawable.play_arrow_24px);
        }
        binding.playPauseImage.setRotation(0);
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
        updatePlayPauseButton(player.isPlaying());
    }

    private void playNextTrack() {
        if (playlist.size() > 1) {
            currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
            updateTrackUI(playlist.get(currentTrackIndex));
        }
    }

    private void playPreviousTrack() {
        if (playlist.size() > 1) {
            currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
            updateTrackUI(playlist.get(currentTrackIndex));
        }
    }

    private String formatDuration(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        if (state == Player.STATE_ENDED && repeatMode == Player.REPEAT_MODE_OFF) {
            playNextTrack();
        }
        // Update play/pause button whenever playback state changes
        updatePlayPauseButton(player.isPlaying());
    }

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
        // Always update play/pause button state when activity resumes
        if (player != null) {
            updatePlayPauseButton(player.isPlaying());
        }
    }

    private void setupMoreInfoBottomSheet() {
        moreInfoBottomSheet = new BottomSheetDialog(this);
        MusicOverviewMoreInfoBottomSheetBinding bottomSheetBinding = MusicOverviewMoreInfoBottomSheetBinding.inflate(getLayoutInflater());
        moreInfoBottomSheet.setContentView(bottomSheetBinding.getRoot());

        // Get current track
        TrackData currentTrack = getIntent().getParcelableExtra("track");
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
            // Handle go to album click
            moreInfoBottomSheet.dismiss();
        });

        bottomSheetBinding.addToLibrary.setOnClickListener(v -> {
            // Handle add to library click
            moreInfoBottomSheet.dismiss();
        });

        bottomSheetBinding.download.setOnClickListener(v -> {
            // Handle download click
            moreInfoBottomSheet.dismiss();
        });
    }

    private void showMoreInfoBottomSheet() {
        if (moreInfoBottomSheet != null) {
            moreInfoBottomSheet.show();
        }
    }
}
