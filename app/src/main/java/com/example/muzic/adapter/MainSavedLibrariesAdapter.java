package com.example.muzic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.example.muzic.R;
import com.example.muzic.activities.ListActivity;
import com.example.muzic.databinding.ActivityMainSongsItemBinding;
import com.example.muzic.records.Playlist;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

public class MainSavedLibrariesAdapter extends RecyclerView.Adapter<MainSavedLibrariesAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlists;
    private final Context context;
    private final Gson gson = new Gson();

    public MainSavedLibrariesAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    public void updatePlaylists(List<Playlist> newPlaylists) {
        this.playlists.clear();
        this.playlists.addAll(newPlaylists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityMainSongsItemBinding binding = ActivityMainSongsItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new PlaylistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);

        holder.binding.albumTitle.setText(playlist.playlistName());
        /*if (playlist.description() != null) {
            holder.binding.artist.setText(playlist.description());
            holder.binding.artist.setVisibility(View.VISIBLE);
        } else {
            holder.binding.artist.setVisibility(View.GONE);
        }*/

        String imageUrl = playlist.artwork() != null ? playlist.artwork().x480() : null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.bolt_24px)
                .into(holder.binding.coverImage);
        } else {
            holder.binding.coverImage.setImageResource(R.drawable.bolt_24px);
        }

        holder.itemView.setOnClickListener(v -> {
            // Create a custom data object that includes all necessary playlist information
            CustomPlaylistData customData = new CustomPlaylistData(
                playlist.artwork(),
                playlist.description(),
                playlist.permalink(),
                playlist.id(),
                playlist.isAlbum(),
                playlist.playlistName(),
                playlist.repostCount(),
                playlist.favoriteCount(),
                playlist.totalPlayCount(),
                playlist.user(),
                null  // tracks will be fetched in ListActivity
            );

            // Convert to JSON and start ListActivity
            Intent intent = new Intent(v.getContext(), ListActivity.class);
            intent.putExtra("data", gson.toJson(customData));
            intent.putExtra("id", playlist.id());
            intent.putExtra("type", playlist.isAlbum() ? "album" : "playlist");
            intent.putExtra("createdByUser", playlist.id().startsWith("local_"));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ActivityMainSongsItemBinding binding;

        public PlaylistViewHolder(@NonNull ActivityMainSongsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // Custom class to include all playlist data
    private static class CustomPlaylistData {
        private final com.example.muzic.records.Artwork artwork;
        private final String description;
        private final String permalink;
        private final String id;
        private final boolean isAlbum;
        private final String playlistName;
        private final int repostCount;
        private final int favoriteCount;
        private final int totalPlayCount;
        private final com.example.muzic.records.User user;
        private final List<com.example.muzic.records.Track> tracks;

        public CustomPlaylistData(com.example.muzic.records.Artwork artwork, String description,
                                String permalink, String id, boolean isAlbum, String playlistName,
                                int repostCount, int favoriteCount, int totalPlayCount,
                                com.example.muzic.records.User user,
                                List<com.example.muzic.records.Track> tracks) {
            this.artwork = artwork;
            this.description = description;
            this.permalink = permalink;
            this.id = id;
            this.isAlbum = isAlbum;
            this.playlistName = playlistName;
            this.repostCount = repostCount;
            this.favoriteCount = favoriteCount;
            this.totalPlayCount = totalPlayCount;
            this.user = user;
            this.tracks = tracks;
        }
    }
} 