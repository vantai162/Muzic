package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

public record Playlist(
        @SerializedName("artwork") Artwork artwork,
        @SerializedName("description") String description,
        @SerializedName("permalink") String permalink,
        @SerializedName("id") String id,
        @SerializedName("is_album") boolean isAlbum,
        @SerializedName("playlist_name") String playlistName,
        @SerializedName("repost_count") int repostCount,
        @SerializedName("favorite_count") int favoriteCount,
        @SerializedName("total_play_count") int totalPlayCount,
        @SerializedName("user") User user
) {}