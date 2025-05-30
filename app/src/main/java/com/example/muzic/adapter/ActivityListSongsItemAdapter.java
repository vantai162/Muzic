package com.example.muzic.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.activities.MusicOverviewActivity;
import com.example.muzic.records.Track;
import com.example.muzic.model.TrackData;
import com.example.muzic.model.User;
import com.example.muzic.model.Artwork;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ActivityListSongsItemAdapter extends RecyclerView.Adapter<ActivityListSongsItemAdapter.ViewHolder> {

    private final List<Track> data;

    public ActivityListSongsItemAdapter(List<Track> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = View.inflate(parent.getContext(), viewType == 0 ? R.layout.activity_list_song_item : R.layout.activity_list_shimmer, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        _v.setLayoutParams(layoutParams);
        return new ViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        Track track2 = data.get(position);

        holder.itemView.findViewById(R.id.title).setSelected(true);
        holder.itemView.findViewById(R.id.artist).setSelected(true);

        ((TextView) holder.itemView.findViewById(R.id.title)).setText(track2.title());
        ((TextView) holder.itemView.findViewById(R.id.artist)).setText(track2.user().name());

        Picasso.get().load(Uri.parse(track2.artwork().x480())).into(((ImageView) holder.itemView.findViewById(R.id.coverImage)));

        holder.itemView.setOnClickListener(view -> {
            Track track = data.get(holder.getBindingAdapterPosition());
            
            // Convert all tracks to TrackData
            ArrayList<TrackData> playlist = new ArrayList<>();
            for (Track t : data) {
                playlist.add(convertToTrackData(t));
            }
            
            // Get current track data
            TrackData currentTrack = convertToTrackData(track);
            
            // Update playlist in ApplicationClass
            ApplicationClass app = (ApplicationClass) view.getContext().getApplicationContext();
            app.updatePlaylist(playlist, holder.getBindingAdapterPosition());
            app.playCurrentTrack();
            
            // Start MusicOverviewActivity
            Intent intent = new Intent(view.getContext(), MusicOverviewActivity.class);
            view.getContext().startActivity(intent);
        });
    }

    private TrackData convertToTrackData(Track track) {
        TrackData trackData = new TrackData();
        trackData.id = track.id();
        trackData.title = track.title();
        trackData.track_cid = track.trackCid();
        trackData.duration = track.duration();
        
        // Create and set User
        com.example.muzic.model.User userData = new com.example.muzic.model.User();
        userData.name = track.user().name();
        userData.id = track.user().id();
        trackData.user = userData;
        
        // Create and set Artwork
        com.example.muzic.model.Artwork artworkData = new com.example.muzic.model.Artwork();
        artworkData._480x480 = track.artwork().x480();
        artworkData._150x150 = track.artwork().x150();
        artworkData._1000x1000 = track.artwork().x1000();
        trackData.artwork = artworkData;
        
        // Set other fields
        trackData.description = track.description();
        trackData.genre = track.genre();
        trackData.mood = track.mood();
        trackData.release_date = track.releaseDate();
        trackData.repost_count = track.repostCount();
        trackData.favorite_count = track.favoriteCount();
        trackData.tags = track.tags();
        trackData.downloadable = track.downloadable();
        trackData.play_count = track.playCount();
        trackData.permalink = track.permalink();
        trackData.is_streamable = track.isStreamable();
        
        return trackData;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).id().equals("<shimmer>")) return 1;
        else return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
