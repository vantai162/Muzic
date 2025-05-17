package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record SongSearch(
        @SerializedName("success") boolean success,
        @SerializedName("data") Data data

) {
    public record Data(
            @SerializedName("total") int total,
            @SerializedName("start") int start,
            @SerializedName("results") List<SongResponse.Song> results
    ) { }
}
