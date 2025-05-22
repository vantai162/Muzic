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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.util.UnstableApi;

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

        // Get track data from intent
        TrackData currentTrack = getIntent().getParcelableExtra("track");
        playlist = new ArrayList<>();
        if (currentTrack != null) {
            playlist.add(currentTrack);
            updateTrackUI(currentTrack);
        }

        setupClickListeners();
        setupSeekBar();
        
        // Update UI based on current player state
        updatePlayPauseButton(player.isPlaying());
    }

    private void setupClickListeners() {
        // Play/Pause button
        binding.playPauseImage.setOnClickListener(v -> togglePlayPause());

        // Next button
        binding.nextIcon.setOnClickListener(v -> playNextTrack());

        // Previous button
        binding.prevIcon.setOnClickListener(v -> playPreviousTrack());

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

        // Only prepare and play if not already playing this track
        if (!isCurrentTrack(track)) {
            String streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + track.id + "/stream";
            MediaItem mediaItem = MediaItem.fromUri(streamUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
        updatePlayPauseButton(player.isPlaying());
    }

    private boolean isCurrentTrack(TrackData track) {
        if (player.getCurrentMediaItem() == null) return false;
        String currentUrl = player.getCurrentMediaItem().mediaId;
        String newUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + track.id + "/stream";
        return currentUrl.equals(newUrl);
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            binding.playPauseImage.setImageResource(R.drawable.baseline_pause_24);
            binding.playPauseImage.setRotation(0);
        } else {
            binding.playPauseImage.setImageResource(R.drawable.play_arrow_24px);
            binding.playPauseImage.setRotation(0);
        }
    }

    private void togglePlayPause() {
        boolean isPlaying = !player.isPlaying();
        if (isPlaying) {
            player.play();
            binding.playPauseImage.setImageResource(R.drawable.baseline_pause_24);
        } else {
            player.pause();
            binding.playPauseImage.setImageResource(R.drawable.play_arrow_24px);
        }
        binding.playPauseImage.setRotation(0);
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
        // Apply slide down animation when going back
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down);
    }

    public void backPress(View view) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync with MainActivity's player state
        if (getIntent().hasExtra("isPlaying")) {
            boolean isPlaying = getIntent().getBooleanExtra("isPlaying", false);
            updatePlayPauseButton(isPlaying);
        }
    }
}
