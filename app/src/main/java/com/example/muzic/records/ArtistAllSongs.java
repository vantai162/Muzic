package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record ArtistAllSongs(
        @SerializedName("success") boolean success,
        @SerializedName("data") Data data
) {
    public record Data(
            @SerializedName("total") int total,
            @SerializedName("songs") List<SongResponse.Song> songs
    ){}
}
