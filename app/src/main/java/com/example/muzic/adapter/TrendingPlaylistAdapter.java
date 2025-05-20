package com.example.muzic.adapter;

import android.content.Context;
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
import com.example.muzic.records.Playlist;
import com.facebook.shimmer.ShimmerFrameLayout;

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

        ((TextView) holder.itemView.findViewById(R.id.title)).setText(playlist.playlistName());

        String imageUrl = playlist.artwork() != null ? playlist.artwork().x480() : null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bolt_24px)
                    .into(holder.ivArtwork);
        } else {
            holder.ivArtwork.setImageResource(R.drawable.bolt_24px);
        }
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
}
