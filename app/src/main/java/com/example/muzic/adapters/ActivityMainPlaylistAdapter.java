package com.example.muzic.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.example.muzic.R;
import com.example.muzic.activities.ListActivity;
import com.example.muzic.model.AlbumItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ActivityMainPlaylistAdapter extends RecyclerView.Adapter<ActivityMainPlaylistAdapter.PlaylistAdapterViewHolder> {

    private final List<AlbumItem> data;

    public ActivityMainPlaylistAdapter(List<AlbumItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public PlaylistAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = LayoutInflater.from(parent.getContext()).inflate(viewType == 0 ? R.layout.activity_main_playlist_item : R.layout.main_playlist_item_shimmer, null, false);
        _v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new PlaylistAdapterViewHolder(_v);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapterViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        ((TextView) holder.itemView.findViewById(R.id.title)).setText(data.get(position).albumTitle());
        ImageView imageView = holder.itemView.findViewById(R.id.imageView);
        Picasso.get().load(Uri.parse(data.get(position).albumCover())).into(imageView);

        holder.itemView.setOnClickListener(v -> {
            v.getContext().startActivity(new Intent(v.getContext(), ListActivity.class).putExtra("data", new Gson().toJson(data.get(position))));
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).id().equals("<shimmer>"))
            return 1;
        return 0;
    }

    public static class PlaylistAdapterViewHolder extends RecyclerView.ViewHolder {
        public PlaylistAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}