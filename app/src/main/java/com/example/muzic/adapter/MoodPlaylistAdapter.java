package com.example.muzic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzic.R;
import com.example.muzic.model.MoodPlaylist;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.Track;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MoodPlaylistAdapter extends RecyclerView.Adapter<MoodPlaylistAdapter.MoodPlaylistViewHolder> {

    private final List<MoodPlaylist> moodPlaylists;
    private final Context context;

    public MoodPlaylistAdapter(Context context, List<MoodPlaylist> moodPlaylists) {
        this.context = context;
        this.moodPlaylists = moodPlaylists;
    }

    public void setMoodPlaylists(List<MoodPlaylist> playlists) {
        this.moodPlaylists.clear();
        this.moodPlaylists.addAll(playlists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoodPlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = LayoutInflater.from(parent.getContext()).inflate(viewType == 0 ? R.layout.activity_main_playlist_item : R.layout.main_playlist_item_shimmer, null, false);
        _v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MoodPlaylistViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodPlaylistViewHolder holder, int position) {

        MoodPlaylist playlist = moodPlaylists.get(position);
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        ((TextView) holder.itemView.findViewById(R.id.title)).setText(playlist.getMood());

        // Load ảnh đúng theo mood
        int imageRes = R.drawable.bolt_24px; // Mặc định

        switch (playlist.getMood()) {
            case "Easygoing":
                imageRes = R.drawable.jaming2;
                break;
            case "Energizing":
                imageRes = R.drawable.party;
                break;
            case "Gritty":
                imageRes = R.drawable.gritty;
                break;
            case "Yearning":
                imageRes = R.drawable.sad;
                break;
            case "Peaceful":
                imageRes = R.drawable.yoga;
                break;
            case "Defiant":
                imageRes = R.drawable.deffi;
                break;
            case "Fiery":
                imageRes = R.drawable.angry;
                break;
            case "Empowering":
                imageRes = R.drawable.em;
                break;
            case "Upbeat":
                imageRes = R.drawable.upbeat;
                break;
            case "Romantic":
                imageRes = R.drawable.romantic;
                break;

        }

        // Load bằng Glide
        Glide.with(context)
                .load(imageRes)
                .placeholder(R.drawable.bolt_24px)
                .into(holder.ivArtwork);
    }

    @Override
    public int getItemCount() {
        return moodPlaylists.size();
    }

    public static class MoodPlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView tvMoodTitle;
        ImageView ivArtwork;

        public MoodPlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodTitle = itemView.findViewById(R.id.title);
            ivArtwork = itemView.findViewById(R.id.imageView);
        }
    }
}
