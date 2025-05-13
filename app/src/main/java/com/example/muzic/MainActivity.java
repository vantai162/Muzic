package com.example.muzic;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private View miniPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Khởi tạo mini player
        miniPlayer = findViewById(R.id.mini_player_container);
        miniPlayer.setVisibility(View.GONE); // Ẩn lúc đầu

    }
    public void showMiniPlayer(String title, String artist, String imageUrl) {
        miniPlayer.setVisibility(View.VISIBLE);

        TextView tvTitle = miniPlayer.findViewById(R.id.tv_title_mini);
        TextView tvArtist = miniPlayer.findViewById(R.id.tv_artist_mini);
        ImageView imgThumb = miniPlayer.findViewById(R.id.img_thumb_mini);

        tvTitle.setText(title);
        tvArtist.setText(artist);
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.baseline_music_note_24) // ảnh mặc định khi đang load
            .into(imgThumb);
    }
}