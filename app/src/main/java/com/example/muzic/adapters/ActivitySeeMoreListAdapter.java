package com.example.muzic.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.R;
import com.example.muzic.activities.MusicOverviewActivity;
import com.example.muzic.records.SongResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ActivitySeeMoreListAdapter extends RecyclerView.Adapter<ActivitySeeMoreListAdapter.ViewHolder> {

    private final List<SongResponse.Song> data;
    private static final int LOADING = 0;
    private static final int ITEM = 1;
    private boolean isLoadingAdded = false;

    public ActivitySeeMoreListAdapter(List<SongResponse.Song> data) {
        this.data = data;
    }

    public ActivitySeeMoreListAdapter(){
        this.data = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = View.inflate(parent.getContext(), viewType == 1 ? R.layout.activity_artist_profile_view_top_songs_item : R.layout.progress_bar_layout, null);
        _v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            //((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        ImageView coverImage = holder.itemView.findViewById(R.id.coverImage);
        TextView coverTitle = holder.itemView.findViewById(R.id.coverTitle);
        TextView coverPlayed = holder.itemView.findViewById(R.id.coverPlayed);
        TextView positionTextView = holder.itemView.findViewById(R.id.position);
        ImageView moreImage = holder.itemView.findViewById(R.id.more);

        positionTextView.setText(String.valueOf(position + 1));
        coverTitle.setText(data.get(position).name());
        coverPlayed.setText(
                String.format("%s | %s", data.get(position).year(), data.get(position).label())
        );
        Picasso.get().load(Uri.parse(data.get(position).image().get(data.get(position).image().size() - 1).url())).into(coverImage);

        holder.itemView.setOnClickListener(view -> {
            view.getContext().startActivity(new Intent(view.getContext(), MusicOverviewActivity.class).putExtra("id", data.get(position).id()));
        });
    }

    @Override
    public int getItemCount() {
        return data == null?0:data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void add(SongResponse.Song da) {
        data.add(da);
        notifyItemInserted(data.size() - 1);
    }

    public void addAll(List<SongResponse.Song> moveResults) {
        for (SongResponse.Song result : moveResults) {
            add(result);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public enum Mode{
        TOP_SONGS,
        TOP_ALBUMS,
        TOP_SINGLES
    }
}
