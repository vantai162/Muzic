package com.example.muzic.ui;

import android.graphics.Bitmap;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.muzic.R;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlaySongActivity extends AppCompatActivity {
    private ImageView imgSong;
    private TextView tvTitle, tvArtist;
    private String songImageUrl;
    private ImageView imgBackgroundBlur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play_song);

        imgSong = findViewById(R.id.img_song_play);
        tvTitle = findViewById(R.id.tv_song_title_play);
        tvArtist = findViewById(R.id.tv_song_artist_play);
        imgBackgroundBlur = findViewById(R.id.img_background_blur);

        // Nhận dữ liệu từ Intent
        songImageUrl = getIntent().getStringExtra("song_image_url");
        String title = getIntent().getStringExtra("song_title");
        String imageUrl = getIntent().getStringExtra("song_image_url");
        setBlurBackground(songImageUrl);

        tvTitle.setText(title);
        tvArtist.setText("Artist"); // Bạn có thể truyền thêm artist nếu cần

        Glide.with(this)
                .load(imageUrl)
                .into(imgSong);
    }
    private void setBlurBackground(String imageUrl) {
        int blurRadius = 50; // độ mờ

        RequestOptions requestOptions = new RequestOptions()
                .transform(new BlurTransformation(blurRadius));

        Glide.with(this)
                .load(imageUrl)
                .apply(requestOptions)
                .into(imgBackgroundBlur);
    }
}