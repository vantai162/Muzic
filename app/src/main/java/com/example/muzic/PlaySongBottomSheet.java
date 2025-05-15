package com.example.muzic;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.muzic.object.Song;
import com.example.muzic.player.PlayerManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlaySongBottomSheet extends BottomSheetDialogFragment {

    private Song song;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;
    private ExoPlayer exoPlayer;
    private boolean isSeekBarUpdating = false;
    ImageView imgSong;

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    public PlaySongBottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_play_song, container, false);
        ImageView btnCollapse = view.findViewById(R.id.btn_collapse);
        imgSong = view.findViewById(R.id.img_song_play);
        TextView tvTitle = view.findViewById(R.id.tv_song_title_play);
        ImageView imgBackgroundBlur = view.findViewById(R.id.img_background_blur);
        seekBar = view.findViewById(R.id.seekBar);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        ImageView btnPlayPause = view.findViewById(R.id.btn_play_pause);

        song = PlayerManager.getCurrentSong();
        if (song == null) {
            Toast.makeText(requireContext(), "No song is playing", Toast.LENGTH_SHORT).show();
            dismiss();
            return null;
        }

        tvTitle.setText(song.getTitle());
        Glide.with(requireContext())
            .load(song.getImageUrl())
            .into(imgSong);

        RequestOptions requestOptions = new RequestOptions().transform(new BlurTransformation(50));
        Glide.with(this).load(song.getImageUrl()).apply(requestOptions).into(imgBackgroundBlur);

        btnCollapse.setOnClickListener(v -> {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            if (dialog != null) {
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        btnPlayPause.setImageResource(PlayerManager.isPlaying() ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24);
        btnPlayPause.setOnClickListener(v -> {
            if (PlayerManager.isPlaying()) {
                PlayerManager.pause();
                btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
            } else {
                PlayerManager.resume();
                btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
            }
        });

        exoPlayer = PlayerManager.getPlayer(requireContext());
        // Nếu player đã sẵn sàng khi mở lại từ MiniPlayer
        if (exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
            long duration = exoPlayer.getDuration();
            if (duration > 0) {
                seekBar.setMax((int) (duration / 1000));
                tvTotalTime.setText(formatTime(duration));
                long currentPos = exoPlayer.getCurrentPosition();
                seekBar.setProgress((int) (currentPos / 1000));
                tvCurrentTime.setText(formatTime(currentPos));
            }
        }
        if (exoPlayer == null) {
            Toast.makeText(requireContext(), "Error: Player not initialized", Toast.LENGTH_SHORT).show();
            return view;
        }

        exoPlayer.addListener(new com.google.android.exoplayer2.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == ExoPlayer.STATE_READY && !isSeekBarUpdating) {
                    long duration = exoPlayer.getDuration();
                    if (duration > 0) {
                        seekBar.setMax((int) (duration / 1000));
                        tvTotalTime.setText(formatTime(duration));
                    }

                    updateSeekBarRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (exoPlayer != null && exoPlayer.isPlaying()) {
                                long currentPos = exoPlayer.getCurrentPosition();
                                long dur = exoPlayer.getDuration();
                                if (dur > 0) {
                                    seekBar.setProgress((int) (currentPos / 1000));
                                    tvCurrentTime.setText(formatTime(currentPos));
                                }
                            }
                            handler.postDelayed(this, 500);
                        }
                    };

                    handler.post(updateSeekBarRunnable);
                    isSeekBarUpdating = true;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                btnPlayPause.setImageResource(isPlaying
                    ? R.drawable.baseline_pause_24
                    : R.drawable.baseline_play_arrow_24);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && exoPlayer != null) {
                    exoPlayer.seekTo(progress * 1000L);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    private String formatTime(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateSeekBarRunnable);
        isSeekBarUpdating = false;
    }
}
