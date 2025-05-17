package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record PlaylistSearch(
        @SerializedName("success") boolean success,
        @SerializedName("data") Data data

) {
    public record Data(
            @SerializedName("id") String id,
            @SerializedName("name") String name,
            @SerializedName("url") String url,
            @SerializedName("description") String description,
            @SerializedName("type") String type,
            @SerializedName("year") int year,
            @SerializedName("playCount") int playCount,
            @SerializedName("songCount") int songCount,
            @SerializedName("language") String language,
            @SerializedName("explicitContent") boolean explicitContent,
            @SerializedName("artists") List<Artist> artists,
            @SerializedName("image") List<GlobalSearch.Image> image,
            @SerializedName("songs") List<SongResponse.Song> songs
    ) {

        public record Artist(
                @SerializedName("id") String id,
                @SerializedName("name") String name,
                @SerializedName("url") String url,
                @SerializedName("role") String role,
                @SerializedName("type") String type,
                @SerializedName("image") List<GlobalSearch.Image> image

        ) {
        }

    }
}