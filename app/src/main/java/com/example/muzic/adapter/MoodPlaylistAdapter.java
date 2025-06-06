package com.example.muzic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzic.R;
import com.example.muzic.activities.ListActivity;
import com.example.muzic.model.MoodPlaylist;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.Track;
import com.example.muzic.records.Artwork;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

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

        holder.tvMoodTitle.setText(playlist.getMood());

        // Load ảnh đúng theo mood
        final int imageRes;  // Make it final
        
        // Determine image resource based on mood
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
            case "Sentimental":
                imageRes = R.drawable.sentimental_value1;
                break;
            case "Aggressive":
                imageRes = R.drawable.angry;
                break;
            case "Sensual":
                imageRes = R.drawable.romantic;
                break;
            case "Rowdy":
                imageRes = R.drawable.deffi;
                break;
            case "Melancholy":
                imageRes = R.drawable.sad;
                break;
            default:
                imageRes = R.drawable.bolt_24px;
                break;
        }

        // Load bằng Glide
        Glide.with(context)
                .load(imageRes)
                .placeholder(R.drawable.bolt_24px)
                .into(holder.ivArtwork);

        // Add click listener
        holder.itemView.setOnClickListener(view -> {
            // Convert model.Artwork to records.Artwork
            com.example.muzic.model.Artwork modelArtwork = playlist.getArtwork();
            Artwork recordsArtwork = null;
            if (modelArtwork != null) {
                recordsArtwork = new Artwork(
                    modelArtwork._150x150,
                    modelArtwork._480x480,
                    modelArtwork._1000x1000
                );
            }

            // Create a custom JSON object that includes both Playlist and its tracks
            CustomPlaylistData customData = new CustomPlaylistData(
                recordsArtwork,
                playlist.getDescription(),
                "",  // permalink
                playlist.getId(),
                false,  // isAlbum
                playlist.getMood(),  // playlistName
                0,  // repostCount
                0,  // favoriteCount
                0,  // totalPlayCount
                playlist.getUser(),
                playlist.getTracks()  // Include the tracks
            );

            // Convert to JSON
            String playlistJson = new Gson().toJson(customData);

            // Start ListActivity with playlist data
            Intent intent = new Intent(context, ListActivity.class);
            intent.putExtra("data", playlistJson);
            intent.putExtra("isMoodPlaylist", true);
            intent.putExtra("moodImageRes", imageRes);
            context.startActivity(intent);
        });
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

    // Custom class to include tracks with playlist data
    private static class CustomPlaylistData {
        private final Artwork artwork;
        private final String description;
        private final String permalink;
        private final String id;
        private final boolean isAlbum;
        private final String playlistName;
        private final int repostCount;
        private final int favoriteCount;
        private final int totalPlayCount;
        private final com.example.muzic.records.User user;
        private final List<Track> tracks;

        public CustomPlaylistData(Artwork artwork, String description, String permalink,
                                String id, boolean isAlbum, String playlistName,
                                int repostCount, int favoriteCount, int totalPlayCount,
                                com.example.muzic.records.User user, List<Track> tracks) {
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
