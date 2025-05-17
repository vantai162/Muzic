package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record AlbumSearch(
        @SerializedName("success") boolean success,
        @SerializedName("data") Data data

) {
    public record Data(
            @SerializedName("id") String id,
            @SerializedName("name") String name,
            @SerializedName("url") String url,
            @SerializedName("description") String description,
            @SerializedName("year") int year,
            @SerializedName("playCount") int playCount,
            @SerializedName("language") String language,
            @SerializedName("explicitContent") boolean explicitContent,
            @SerializedName("artists") SongResponse.Artists artist,
            @SerializedName("image") List<GlobalSearch.Image> image,
            @SerializedName("songs") List<SongResponse.Song> songs
    ) {
    }
}
