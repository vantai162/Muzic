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
import com.example.muzic.model.BasicDataRecord;
import com.example.muzic.records.ArtistsSearch;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ActivityMainArtistsItemAdapter extends RecyclerView.Adapter<ActivityMainArtistsItemAdapter.ActivityMainArtistsItemAdapterViewHolder> {

    private final List<ArtistsSearch.Data.Results> data;

    public ActivityMainArtistsItemAdapter(List<ArtistsSearch.Data.Results> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ActivityMainArtistsItemAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = View.inflate(parent.getContext(), viewType == 0 ? R.layout.activity_main_artists_item : R.layout.artists_item_shimmer, null);
        _v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ActivityMainArtistsItemAdapterViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityMainArtistsItemAdapterViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        holder.itemView.findViewById(R.id.artist_name).setSelected(true);
        ((TextView) holder.itemView.findViewById(R.id.artist_name)).setText(data.get(position).name());
        ImageView imageView = holder.itemView.findViewById(R.id.artist_img);
        Picasso.get().load(Uri.parse(data.get(position).image().get(data.get(position).image().size() - 1).url())).into(imageView);

        holder.itemView.setOnClickListener(v -> {
            v.getContext().startActivity(new Intent(v.getContext(), ArtistProfileActivity.class)
                    .putExtra("data", new Gson().toJson(new BasicDataRecord(data.get(position).id(), data.get(position).name(), "", data.get(position).image().get(data.get(position).image().size() - 1).url())))
            );
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).id().equals("<shimmer>"))
            return 1;
        else
            return 0;
    }

    public static class ActivityMainArtistsItemAdapterViewHolder extends RecyclerView.ViewHolder {
        public ActivityMainArtistsItemAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}