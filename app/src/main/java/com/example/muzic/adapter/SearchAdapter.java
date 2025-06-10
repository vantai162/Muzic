package com.example.muzic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzic.R;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.SearchResult;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TRACK = 0;
    private static final int TYPE_PLAYLIST = 1;
    private static final int TYPE_USER = 2;

    private List<Object> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onTrackClick(Track track);
        void onPlaylistClick(Playlist playlist);
        void onUserClick(User user);
    }

    public SearchAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(SearchResult searchResult) {
        items.clear();
        items.addAll(searchResult.getTracks());
        items.addAll(searchResult.getPlaylists());
        items.addAll(searchResult.getUsers());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof Track) return TYPE_TRACK;
        if (item instanceof Playlist) return TYPE_PLAYLIST;
        if (item instanceof User) return TYPE_USER;
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_TRACK:
                return new TrackViewHolder(inflater.inflate(R.layout.item_track, parent, false));
            case TYPE_PLAYLIST:
                return new PlaylistViewHolder(inflater.inflate(R.layout.item_playlist, parent, false));
            case TYPE_USER:
                return new UserViewHolder(inflater.inflate(R.layout.item_user, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        
        if (holder instanceof TrackViewHolder && item instanceof Track) {
            ((TrackViewHolder) holder).bind((Track) item);
        } else if (holder instanceof PlaylistViewHolder && item instanceof Playlist) {
            ((PlaylistViewHolder) holder).bind((Playlist) item);
        } else if (holder instanceof UserViewHolder && item instanceof User) {
            ((UserViewHolder) holder).bind((User) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView artistView;

        TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            titleView = itemView.findViewById(R.id.title);
            artistView = itemView.findViewById(R.id.artist);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && items.get(position) instanceof Track) {
                    listener.onTrackClick((Track) items.get(position));
                }
            });
        }

        void bind(Track track) {
            titleView.setText(track.title());
            artistView.setText(track.user().name());
            if (track.artwork() != null) {
                Glide.with(itemView.getContext())
                        .load(track.artwork().x480())
                        .placeholder(R.drawable.bolt_24px)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.bolt_24px);
            }
        }
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView userView;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            titleView = itemView.findViewById(R.id.title);
            userView = itemView.findViewById(R.id.user);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && items.get(position) instanceof Playlist) {
                    listener.onPlaylistClick((Playlist) items.get(position));
                }
            });
        }

        void bind(Playlist playlist) {
            titleView.setText(playlist.playlistName());
            userView.setText(playlist.user().name());
            if (playlist.artwork() != null) {
                Glide.with(itemView.getContext())
                        .load(playlist.artwork().x480())
                        .placeholder(R.drawable.bolt_24px)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.bolt_24px);
            }
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView followersView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            nameView = itemView.findViewById(R.id.name);
            followersView = itemView.findViewById(R.id.followers);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && items.get(position) instanceof User) {
                    listener.onUserClick((User) items.get(position));
                }
            });
        }

        void bind(User user) {
            nameView.setText(user.name());
            followersView.setText(user.followerCount() + " followers");
            if (user.profilePicture() != null) {
                Glide.with(itemView.getContext())
                        .load(user.profilePicture().x480())
                        .placeholder(R.drawable.bolt_24px)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.bolt_24px);
            }
        }
    }
} 