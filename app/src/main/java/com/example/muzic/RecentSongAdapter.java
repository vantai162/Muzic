package com.example.muzic;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

public class RecentSongAdapter extends RecyclerView.Adapter<RecentSongAdapter.ViewHolder> {
    public interface OnSongClickListener {
        void onSongClick(Song song);
    }
    private OnSongClickListener listener;
    private Context context;
    private List<Song> recentSongs;

    public RecentSongAdapter(Context context, List<Song> recentSongs) {
        this.context = context;
        this.recentSongs = recentSongs;
    }
    public RecentSongAdapter(Context context, List<Song> songs, OnSongClickListener listener) {
        this.context = context;
        this.recentSongs = songs;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = recentSongs.get(position);

        holder.title.setText(song.getTitle());
        holder.artist.setText("Artist");
        Log.d("RecentSongAdapter", "Image URL: " + song.getImageUrl());
        Glide.with(holder.itemView.getContext())
                .load(song.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .dontAnimate()
                .skipMemoryCache(true)
                .transform(new RoundedCorners(16))
                .into(holder.imageView);
        holder.itemView.setOnClickListener(v -> {
            //Intent intent = new Intent(context, PlaySongActivity.class);
            //intent.putExtra("song_title", song.getTitle());
            //intent.putExtra("song_image_url", song.getImageUrl());
            //context.startActivity(intent);
            PlaySongBottomSheet bottomSheet = new PlaySongBottomSheet(song);
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            bottomSheet.show(fragmentManager, bottomSheet.getTag());
            if (listener != null) {
                listener.onSongClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recentSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_song);
            title = itemView.findViewById(R.id.tv_song_title);
            artist = itemView.findViewById(R.id.tv_song_artist);
        }
    }
}

