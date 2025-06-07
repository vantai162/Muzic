package com.example.muzic.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.R;
import com.example.muzic.activities.MusicOverviewActivity;
import com.example.muzic.model.TrackData;
import com.example.muzic.records.Track;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Picasso;
import com.example.muzic.databinding.ActivityArtistProfileViewTopSongsItemBinding;
import com.example.muzic.ApplicationClass;

import java.util.ArrayList;
import java.util.List;

public class ActivityArtistProfileTopSongsAdapter extends RecyclerView.Adapter<ActivityArtistProfileTopSongsAdapter.ViewHolder> {
    private static final String TAG = "ArtistTopSongsAdapter";
    private final List<Track> tracks;

    public ActivityArtistProfileTopSongsAdapter(List<Track> tracks) {
        this.tracks = tracks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
            viewType == 0 ? R.layout.activity_artist_profile_view_top_songs_item 
                        : R.layout.artist_profile_view_top_songs_shimmer, 
            parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (getItemViewType(position) == 1) {
                if (holder.shimmerLayout != null) {
                    holder.shimmerLayout.startShimmer();
                }
                return;
            }

            Track track = tracks.get(position);
            
            // Set position number
            if (holder.positionView != null) {
                holder.positionView.setText(String.valueOf(position + 1));
            }

            // Set title
            if (holder.titleView != null) {
                holder.titleView.setText(track.title());
                holder.titleView.setSelected(true);
            }

            // Set play count
            if (holder.playCountView != null) {
                holder.playCountView.setText(String.valueOf(track.playCount()));
                holder.playCountView.setSelected(true);
            }

            // Load artwork
            if (holder.artworkView != null && track.artwork() != null && track.artwork().x150() != null) {
                Picasso.get()
                    .load(Uri.parse(track.artwork().x150()))
                    .placeholder(R.drawable.bolt_24px)
                    .error(R.drawable.bolt_24px)
                    .into(holder.artworkView);
            } else if (holder.artworkView != null) {
                holder.artworkView.setImageResource(R.drawable.bolt_24px);
            }

            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                try {
                    // Convert all tracks to TrackData
                    ArrayList<TrackData> playlist = new ArrayList<>();
                    for (Track t : tracks) {
                        playlist.add(convertToTrackData(t));
                    }
                    
                    // Get current track data
                    TrackData currentTrack = convertToTrackData(track);
                    
                    // Update playlist in ApplicationClass
                    ApplicationClass app = (ApplicationClass) v.getContext().getApplicationContext();
                    app.updatePlaylist(playlist, holder.getBindingAdapterPosition());
                    app.playCurrentTrack();
                    
                    // Start MusicOverviewActivity
                    Intent intent = new Intent(v.getContext(), MusicOverviewActivity.class);
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching MusicOverviewActivity", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position " + position, e);
        }
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

    @Override
    public int getItemCount() {
        return tracks != null ? tracks.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return tracks != null && position < tracks.size() && "<shimmer>".equals(tracks.get(position).id()) ? 1 : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView positionView;
        TextView titleView;
        TextView playCountView;
        ImageView artworkView;
        ShimmerFrameLayout shimmerLayout;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 0) {
                ActivityArtistProfileViewTopSongsItemBinding binding = ActivityArtistProfileViewTopSongsItemBinding.bind(itemView);
                positionView = binding.position;
                titleView = binding.coverTitle;
                playCountView = binding.coverPlayed;
                artworkView = binding.coverImage;
            } else {
                shimmerLayout = itemView.findViewById(R.id.shimmer);
            }
        }
    }
} 