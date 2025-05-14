package com.example.muzic.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muzic.MainActivity;
import com.example.muzic.object.Playlist;
import com.example.muzic.adapter.PlaylistAdapter;
import com.example.muzic.R;
import com.example.muzic.adapter.RecentSongAdapter;
import com.example.muzic.object.Song;
import com.example.muzic.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        List<Playlist> playlistList = new ArrayList<>();
        List<Song> sadSongs = new ArrayList<>();
        List<Song> happySongs = new ArrayList<>();
        sadSongs.add(new Song(1, "Buồn ơi là buồn", "https://youtube.com/song1", "https://picsum.photos/200"));
        sadSongs.add(new Song(2, "Lạc trôi", "https://youtube.com/song2", "https://picsum.photos/200"));
        happySongs.add(new Song(1, "See You Again", "https://youtube.com/song1", "https://nld.mediacdn.vn/2017/2-153359-1501912799729.jpg"));
        happySongs.add(new Song(2, "Save Your Tears", "https://youtube.com/song2",  "https://media-cdn-v2.laodong.vn/storage/newsportal/2021/4/16/899292/Ca-Si-The-Weeknd.jpg?w=800&h=496&crop=auto&scale=both"));
        happySongs.add(new Song(2, "Bang Bang Bang", "https://youtube.com/song2",  "https://i.ytimg.com/vi/2ips2mM7Zqw/maxresdefault.jpg"));
        happySongs.add(new Song(2, "Bông Hoa Đẹp Nhất", "https://youtube.com/song2",  "https://photo-resize-zmp3.zadn.vn/w600_r1x1_jpeg/cover/f/8/1/e/f81efd92fa9a3d52eb37f3b867ab9d32.jpg"));



        playlistList.add(new Playlist("Favorite Songs", sadSongs, R.drawable.img_sad));
        playlistList.add(new Playlist("Happy Songs", happySongs, R.drawable.awkward_seal_ft));

        // Gắn adapter
        PlaylistAdapter adapter = new PlaylistAdapter(requireContext(), playlistList);
        binding.rvPlaylistGrid.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPlaylistGrid.setAdapter(adapter);

        RecentSongAdapter recentAdapter = new RecentSongAdapter(requireContext(), happySongs, song -> {
            // Gọi showMiniPlayer từ MainActivity
            ((MainActivity) requireActivity()).showMiniPlayer(
                song.getTitle(),
                "Unknown Artist", // Hoặc song.getArtist() nếu bạn có dữ liệu artist
                song.getImageUrl(), // Lấy ảnh từ URL bài hát,
                song
            );
        });
        binding.rvRecentlyPlayed.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvRecentlyPlayed.setAdapter(recentAdapter);
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}