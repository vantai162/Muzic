package com.example.muzic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;

import com.example.muzic.R;
import com.example.muzic.adapter.PopularUserAdapter;
import com.example.muzic.adapter.TrendingPlaylistAdapter;
import com.example.muzic.adapter.TrendingTracksAdapter;
import com.example.muzic.databinding.ActivityMainBinding;
import com.example.muzic.records.AudiusTrackResponse;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AudiusAPI";
    private ExoPlayer exoPlayer;
    private TrendingTracksAdapter trendingTracksAdapter;
    private PopularUserAdapter popularUserAdapter;
    private TrendingPlaylistAdapter trendingPlaylistAdapter;
    private ActivityMainBinding binding;
    private SlidingRootNav slidingRootNavBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();



        //Cau hinh thanh 3 dau gach ngang ben trai goc tren
        slidingRootNavBuilder = new SlidingRootNavBuilder(this)
                .withMenuLayout(R.layout.main_drawer_layout)
                .withContentClickableWhenMenuOpened(false)
                .withDragDistance(250)
                .inject();
        binding.profileIcon.setOnClickListener(view -> slidingRootNavBuilder.openMenu(true));
        onDrawerItemsClicked();


        // Cấu hình RecyclerView theo chiều ngang
        binding.popularSongsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        trendingTracksAdapter = new TrendingTracksAdapter(this, track -> {
            String streamUrl = "https://api.audius.co/v1/tracks/" + track.id() + "/stream";
            MediaItem mediaItem = MediaItem.fromUri(streamUrl);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
            //xu ly phat nhac
            Log.d(TAG, "Playing: " + track.title() + " - By: " + track.user().name());
        });
        binding.popularSongsRecyclerView.setAdapter(trendingTracksAdapter);


        // RecyclerView: Popular Users
        binding.popularArtistsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        popularUserAdapter = new PopularUserAdapter(this, new ArrayList<>(), user -> {
            Log.d(TAG, "User clicked: " + user.name());
            // TODO: Mở trang UserDetailActivity nếu bạn có
        });
        binding.popularArtistsRecyclerView.setAdapter(popularUserAdapter);


        // RecyclerView: Popular Playlists
        binding.popularPlaylistRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        trendingPlaylistAdapter = new TrendingPlaylistAdapter(this, new ArrayList<>());
        binding.popularPlaylistRecyclerView.setAdapter(trendingPlaylistAdapter);

        // Gọi API
        AudiusApiService apiService = AudiusApiClient.getInstance();
        Call<AudiusTrackResponse> call = apiService.getTrendingTracks(10);


        call.enqueue(new Callback<AudiusTrackResponse>() {
            @Override
            public void onResponse(Call<AudiusTrackResponse> call, Response<AudiusTrackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = response.body().data();
                    trendingTracksAdapter.setTracks(tracks); // ⬅️ Cập nhật dữ liệu adapter
                    // Lấy user duy nhất từ track
                    Set<String> seenIds = new HashSet<>();
                    List<User> uniqueUsers = new ArrayList<>();
                    for (Track track : tracks) {
                        User user = track.user();
                        if (user != null && seenIds.add(user.id())) {
                            uniqueUsers.add(user);
                        }
                    }
                    popularUserAdapter.setUsers(uniqueUsers); // Cap nhat du lieu adapter user
                } else {
                    Log.e(TAG, "API Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AudiusTrackResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
            }
        });

        // Gọi API lấy danh sách trending playlists
        Call<PlaylistResponse> playlistCall = apiService.getTrendingPlaylists(10);
        playlistCall.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    trendingPlaylistAdapter.setPlaylists(response.body().data());
                } else {
                    Log.e(TAG, "Playlist API Error: " + response.message());
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
        if (exoPlayer != null) {
            exoPlayer.release();
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
}
