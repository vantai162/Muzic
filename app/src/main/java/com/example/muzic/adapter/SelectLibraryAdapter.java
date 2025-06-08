package com.example.muzic.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzic.R;
import com.example.muzic.databinding.ActivityListSongItemBinding;
import com.example.muzic.records.Playlist;

import java.util.List;

public class SelectLibraryAdapter extends RecyclerView.Adapter<SelectLibraryAdapter.ViewHolder> {

    private final List<Playlist> libraries;
    private final OnLibrarySelectedListener listener;

    public interface OnLibrarySelectedListener {
        void onLibrarySelected(Playlist library);
    }

    public SelectLibraryAdapter(List<Playlist> libraries, OnLibrarySelectedListener listener) {
        this.libraries = libraries;
        this.listener = listener;
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
        Playlist library = libraries.get(position);
        
        holder.binding.title.setText(library.playlistName());
        holder.binding.artist.setText(library.description());

        // Load artwork if available
        String artworkUrl = library.artwork() != null ? library.artwork().x480() : null;
        if (artworkUrl != null && !artworkUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(Uri.parse(artworkUrl))
                .placeholder(R.drawable.bolt_24px)
                .error(R.drawable.bolt_24px)
                .into(holder.binding.coverImage);
        } else {
            holder.binding.coverImage.setImageResource(R.drawable.bolt_24px);
        }

        holder.itemView.setOnClickListener(v -> listener.onLibrarySelected(library));
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ActivityListSongItemBinding binding;

        public ViewHolder(@NonNull ActivityListSongItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 