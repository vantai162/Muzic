package com.example.muzic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzic.R;
import com.example.muzic.records.Track;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class TrendingTracksAdapter extends RecyclerView.Adapter<TrendingTracksAdapter.TrackViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }

    private final Context context;
    private final OnTrackClickListener listener;
    private final List<Track> trackList = new ArrayList<>();

    public TrendingTracksAdapter(Context context, OnTrackClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTracks(List<Track> tracks) {
        trackList.clear();
        trackList.addAll(tracks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = View.inflate(parent.getContext(), viewType == 0 ? R.layout.activity_main_songs_item : R.layout.songs_item_shimmer, null);
        _v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new TrackViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }
        Track track = trackList.get(position);
        ((TextView) holder.itemView.findViewById(R.id.albumTitle)).setText(track.title());
        ((TextView) holder.itemView.findViewById(R.id.albumSubTitle)).setText(track.user().name());


        Glide.with(context)
                .load(track.artwork().x480()) // Ensure artwork has .x480() method
                .placeholder(R.drawable.bolt_24px)
                .into(holder.imageCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackClick(track);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCover;
        TextView songTitle;
        TextView user;
        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCover = itemView.findViewById(R.id.coverImage);
            songTitle = itemView.findViewById(R.id.albumTitle);
            user = itemView.findViewById(R.id.albumSubTitle);
        }
    }

}
