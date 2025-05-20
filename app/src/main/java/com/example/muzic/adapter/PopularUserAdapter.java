package com.example.muzic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzic.R;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

public class PopularUserAdapter extends RecyclerView.Adapter<PopularUserAdapter.UserViewHolder> {

    private final Context context;
    private List<User> userList;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final OnUserClickListener clickListener;

    public PopularUserAdapter(Context context, List<User> userList, OnUserClickListener clickListener) {
        this.context = context;
        this.userList = userList;
        this.clickListener = clickListener;
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _v = View.inflate(parent.getContext(), viewType == 0 ? R.layout.activity_main_artists_item : R.layout.artists_item_shimmer, null);
        _v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new UserViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        if (getItemViewType(position) == 1) {
            ((ShimmerFrameLayout) holder.itemView.findViewById(R.id.shimmer)).startShimmer();
            return;
        }

        ((TextView) holder.itemView.findViewById(R.id.artist_name)).setText(user.name());


        Glide.with(context)
                .load(user.profilePicture().x480()) // hoặc "480x480" nếu muốn lớn hơn
                .placeholder(R.drawable.bolt_24px) // ảnh mặc định nếu chưa tải
                .into(holder.avatar);

        holder.itemView.setOnClickListener(v -> clickListener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.artist_img);
            username = itemView.findViewById(R.id.artist_name);
        }
    }
}
