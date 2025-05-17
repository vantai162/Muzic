package com.example.muzic.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.example.muzic.R;
import com.example.muzic.activities.ArtistProfileActivity;
import com.example.muzic.activities.ListActivity;
import com.example.muzic.activities.MusicOverviewActivity;
import com.example.muzic.model.AlbumItem;
import com.example.muzic.model.BasicDataRecord;
import com.example.muzic.model.SearchListItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ActivitySearchListItemAdapter extends RecyclerView.Adapter<ActivitySearchListItemAdapter.ViewHolder> {

    private final List<SearchListItem> data;

    public ActivitySearchListItemAdapter(List<SearchListItem> data) {
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

        holder.itemView.findViewById(R.id.title).setSelected(true);
        holder.itemView.findViewById(R.id.artist).setSelected(true);

        SearchListItem item = data.get(position);

        ((TextView) holder.itemView.findViewById(R.id.title)).setText(item.title());
        ((TextView) holder.itemView.findViewById(R.id.artist)).setText(item.subtitle());

        Picasso.get().load(Uri.parse(item.coverImage())).into(((ImageView) holder.itemView.findViewById(R.id.coverImage)));

        holder.itemView.setOnClickListener(view -> {
            final Intent intent = new Intent();
            intent.putExtra("id", item.id());
            switch (item.type()) {
                case SONG -> {
                    intent.setClass(holder.itemView.getContext(), MusicOverviewActivity.class);
                }
                case ALBUM -> {
                    AlbumItem albumItem = new AlbumItem(item.title(), item.subtitle(), item.coverImage(), item.id());
                    intent.putExtra("data", new Gson().toJson(albumItem));
                    intent.putExtra("type", "album");
                    intent.setClass(holder.itemView.getContext(), ListActivity.class);
                }
                case PLAYLIST -> {
                    AlbumItem albumItem = new AlbumItem(item.title(), item.subtitle(), item.coverImage(), item.id());
                    intent.putExtra("data", new Gson().toJson(albumItem));
                    intent.setClass(holder.itemView.getContext(), ListActivity.class);
                }
                case ARTIST -> {
                    intent.setClass(holder.itemView.getContext(), ArtistProfileActivity.class);
                    intent.putExtra("data", new Gson().toJson(new BasicDataRecord(item.id(), item.title(), "", item.coverImage())));
                }
                default -> {}
            }
            holder.itemView.getContext().startActivity(intent);
        });
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
