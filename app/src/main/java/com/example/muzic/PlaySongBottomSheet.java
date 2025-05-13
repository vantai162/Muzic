package com.example.muzic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.muzic.object.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlaySongBottomSheet extends BottomSheetDialogFragment {

    private Song song;

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (dialog != null) {
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Mở ở trạng thái full
        }
    }

    public PlaySongBottomSheet(Song song) {
        this.song = song;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_play_song, container, false);
        ImageView btnCollapse = view.findViewById(R.id.btn_collapse);
        ImageView imgSong = view.findViewById(R.id.img_song_play);
        TextView tvTitle = view.findViewById(R.id.tv_song_title_play);
        ImageView imgBackgroundBlur = view.findViewById(R.id.img_background_blur);

        tvTitle.setText(song.getTitle());
        Glide.with(requireContext())
            .load(song.getImageUrl())
            .into(imgSong);

        int blurRadius = 50; // độ mờ

        RequestOptions requestOptions = new RequestOptions() //lam mo cai anh bai hat
            .transform(new BlurTransformation(blurRadius));

        Glide.with(this)
            .load(song.getImageUrl())
            .apply(requestOptions)
            .into(imgBackgroundBlur);
        btnCollapse.setOnClickListener(v -> {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            if (dialog != null) {
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN); // Ẩn đi
                }
            }
        });
        return view;
    }

}
