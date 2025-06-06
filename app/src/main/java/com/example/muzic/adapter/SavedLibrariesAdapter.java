package com.example.muzic.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.example.muzic.R;
import com.example.muzic.activities.ListActivity;
import com.example.muzic.records.Playlist;
import com.example.muzic.databinding.ActivityListSongItemBinding;

import java.util.List;

public class SavedLibrariesAdapter extends RecyclerView.Adapter<SavedLibrariesAdapter.ViewHolder> {

    private final List<Playlist> playlists;
    private final Gson gson = new Gson();

    public SavedLibrariesAdapter(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityListSongItemBinding binding = ActivityListSongItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        
        holder.binding.title.setText(playlist.playlistName());
        holder.binding.artist.setText(playlist.description());

        // Load artwork if available
        String artworkUrl = playlist.artwork() != null ? playlist.artwork().x480() : null;
        if (artworkUrl != null && !artworkUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(Uri.parse(artworkUrl))
                .placeholder(R.drawable.bolt_24px)
                .error(R.drawable.bolt_24px)
                .into(holder.binding.coverImage);
        } else {
            holder.binding.coverImage.setImageResource(R.drawable.bolt_24px);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ListActivity.class)
                .putExtra("id", playlist.id())
                .putExtra("data", gson.toJson(playlist))
                .putExtra("type", playlist.isAlbum() ? "album" : "playlist")
                .putExtra("createdByUser", playlist.id().startsWith("local_"));

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ActivityListSongItemBinding binding;

        public ViewHolder(@NonNull ActivityListSongItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}