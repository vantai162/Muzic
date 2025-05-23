package com.example.muzic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.databinding.ItemTrendingTrackBinding;
import com.example.muzic.records.Track;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TrendingTracksAdapter extends RecyclerView.Adapter<TrendingTracksAdapter.ViewHolder> {
    private final Context context;
    private final List<Track> tracks;
    private final OnTrackClickListener listener;
    private int currentTrackIndex = -1;

    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }

    public TrendingTracksAdapter(Context context, OnTrackClickListener listener) {
        this.context = context;
        this.tracks = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTrendingTrackBinding binding = ItemTrendingTrackBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.binding.trackTitle.setText(track.title());
        holder.binding.artistName.setText(track.user().name());
        
        if (track.artwork() != null && track.artwork().x480() != null) {
            Picasso.get().load(track.artwork().x480()).into(holder.binding.trackImage);
        }

        holder.itemView.setOnClickListener(v -> {
            currentTrackIndex = position;
            listener.onTrackClick(track);
        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void setTracks(List<Track> tracks) {
        this.tracks.clear();
        this.tracks.addAll(tracks);
        notifyDataSetChanged();
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int index) {
        this.currentTrackIndex = index;
        notifyDataSetChanged();
    }

    public Track getTrack(int position) {
        if (position >= 0 && position < tracks.size()) {
            return tracks.get(position);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTrendingTrackBinding binding;

        ViewHolder(ItemTrendingTrackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
