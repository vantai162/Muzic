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
        sadSongs.add(new Song(1, "Buồn ơi là buồn", "https://youtube.com/song1"));
        sadSongs.add(new Song(2, "Lạc trôi", "https://youtube.com/song2"));

        playlistList.add(new Playlist("Sad Songs", sadSongs, R.drawable.img_sad));

        // Gắn adapter
        PlaylistAdapter adapter = new PlaylistAdapter(requireContext(), playlistList);
        binding.rvPlaylistGrid.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPlaylistGrid.setAdapter(adapter);
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}