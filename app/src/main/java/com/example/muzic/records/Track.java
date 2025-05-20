package com.example.muzic.records;


import com.google.gson.annotations.SerializedName;

public record Track(
        @SerializedName("artwork") Artwork artwork,
        @SerializedName("description") String description,
        @SerializedName("genre") String genre,
        @SerializedName("id") String id,
        @SerializedName("track_cid") String trackCid,
        @SerializedName("mood") String mood,
        @SerializedName("release_date") String releaseDate,
        @SerializedName("repost_count") int repostCount,
        @SerializedName("favorite_count") int favoriteCount,
        @SerializedName("tags") String tags,
        @SerializedName("title") String title,
        @SerializedName("user") User user,
        @SerializedName("duration") int duration,
        @SerializedName("downloadable") boolean downloadable,
        @SerializedName("play_count") int playCount,
        @SerializedName("permalink") String permalink,
        @SerializedName("is_streamable") boolean isStreamable
) {}