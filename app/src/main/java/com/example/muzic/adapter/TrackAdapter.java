package com.example.muzic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzic.R;
import com.example.muzic.activities.MusicOverviewActivity;
import com.example.muzic.model.TrackData;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private final Context context;
    private List<TrackData> tracks;

    public TrackAdapter(Context context, List<TrackData> tracks) {
        this.context = context;
        this.tracks = tracks;
    }

    public void setTracks(List<TrackData> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_list_song_item, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        TrackData track = tracks.get(position);
        
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        holder.title.setText(track.title);
        holder.artist.setText(track.user.name);

        if (track.artwork != null && track.artwork._480x480 != null) {
            Glide.with(context)
                    .load(track.artwork._480x480)
                    .placeholder(R.drawable.bolt_24px)
                    .into(holder.coverImage);
        }

        holder.itemView.setOnClickListener(v -> {
            // Open MusicOverviewActivity first
            Intent intent = new Intent(context, MusicOverviewActivity.class);
            intent.putExtra("track", track);
            context.startActivity(intent);
            
            // Add slide up animation
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tracks != null ? tracks.size() : 0;
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView title;
        TextView artist;

        TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.coverImage);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
        }
    }
} 