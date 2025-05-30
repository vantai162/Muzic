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
import com.example.muzic.R;
import com.example.muzic.activities.ListActivity;
import com.example.muzic.records.Playlist;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import java.util.List;

public class TrendingPlaylistAdapter extends RecyclerView.Adapter<TrendingPlaylistAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlists;
    private final Context context;

    public TrendingPlaylistAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists.clear();
        this.playlists.addAll(playlists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = LayoutInflater.from(parent.getContext()).inflate(viewType == 0 ? R.layout.activity_main_playlist_item : R.layout.main_playlist_item_shimmer, null, false);
        _v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new PlaylistViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        holder.tvPlaylistName.setText(playlist.playlistName());

        String imageUrl = playlist.artwork() != null ? playlist.artwork().x480() : null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bolt_24px)
                    .into(holder.ivArtwork);
        } else {
            holder.ivArtwork.setImageResource(R.drawable.bolt_24px);
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
            intent.putExtra("data", new Gson().toJson(customData));
            intent.putExtra("isTrendingPlaylist", true);  // Add flag to identify this is a trending playlist
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArtwork;
        TextView tvPlaylistName;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArtwork = itemView.findViewById(R.id.imageView);
            tvPlaylistName = itemView.findViewById(R.id.title);
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
