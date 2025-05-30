package com.example.muzic.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.activities.MusicOverviewActivity;
import com.example.muzic.databinding.ActivityListSongItemBinding;
import com.example.muzic.records.Track;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.squareup.picasso.Picasso;

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = data.get(position);
        holder.binding.title.setText(track.title());
        holder.binding.artist.setText(track.user().name());
        if(track.artwork() != null && !track.artwork().x480().isBlank())
            Picasso.get().load(Uri.parse(track.artwork().x480())).into(holder.binding.coverImage);

        holder.itemView.setOnClickListener(view -> {
            if(ApplicationClass.trackQueue != null)
                if(ApplicationClass.trackQueue.contains(track.id()))
                    ApplicationClass.track_position = holder.getBindingAdapterPosition();
            holder.itemView.getContext().startActivity(new Intent(view.getContext(), MusicOverviewActivity.class).putExtra("id", track.id()));
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ActivityListSongItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ActivityListSongItemBinding.bind(itemView);
        }
    }
}
