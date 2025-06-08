package com.example.muzic.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.activities.MusicOverviewActivity;
import com.example.muzic.databinding.ActivityListSongItemBinding;
import com.example.muzic.model.TrackData;
import com.example.muzic.records.Track;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserCreatedSongsListAdapter extends RecyclerView.Adapter<UserCreatedSongsListAdapter.ViewHolder> {

    private final List<Track> data;

    public UserCreatedSongsListAdapter(List<Track> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = View.inflate(parent.getContext(), R.layout.activity_list_song_item,null);
        _v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(_v);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = data.get(position);
        holder.binding.title.setText(track.title());
        holder.binding.artist.setText(track.user().name());
        if(track.artwork() != null && !track.artwork().x480().isBlank())
            Picasso.get().load(Uri.parse(track.artwork().x480())).into(holder.binding.coverImage);

        holder.itemView.setOnClickListener(view -> {
            // Convert all tracks to TrackData
            ArrayList<TrackData> playlist = new ArrayList<>();
            for (Track t : data) {
                playlist.add(convertToTrackData(t));
            }
            
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
        if (track.user() != null) {
            userData.name = track.user().name();
            userData.id = track.user().id();
        }
        trackData.user = userData;
        
        // Create and set Artwork
        com.example.muzic.model.Artwork artworkData = new com.example.muzic.model.Artwork();
        if (track.artwork() != null) {
            artworkData._480x480 = track.artwork().x480();
            artworkData._150x150 = track.artwork().x150();
            artworkData._1000x1000 = track.artwork().x1000();
        }
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ActivityListSongItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ActivityListSongItemBinding.bind(itemView);
        }
    }
}
