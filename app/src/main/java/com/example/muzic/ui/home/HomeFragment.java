package com.example.muzic.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muzic.Playlist;
import com.example.muzic.PlaylistAdapter;
import com.example.muzic.R;
import com.example.muzic.RecentSongAdapter;
import com.example.muzic.Song;
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
        happySongs.add(new Song(1, "See You Again", "https://youtube.com/song1", "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg"));
        happySongs.add(new Song(2, "Save Your Tears", "https://youtube.com/song2",  "https://media-cdn-v2.laodong.vn/storage/newsportal/2021/4/16/899292/Ca-Si-The-Weeknd.jpg?w=800&h=496&crop=auto&scale=both"));


        playlistList.add(new Playlist("Sad Songs", sadSongs, R.drawable.img_sad));
        playlistList.add(new Playlist("Happy Songs", happySongs, R.drawable.awkward_seal_ft));

        // Gắn adapter
        PlaylistAdapter adapter = new PlaylistAdapter(requireContext(), playlistList);
        binding.rvPlaylistGrid.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPlaylistGrid.setAdapter(adapter);

        RecentSongAdapter recentAdapter = new RecentSongAdapter(requireContext(), happySongs);
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