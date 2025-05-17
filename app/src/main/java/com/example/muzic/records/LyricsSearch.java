package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

public record LyricsSearch (
    @SerializedName("success") boolean success,
    @SerializedName("data") Data data
){
    public record Data(
            @SerializedName("lyrics") String lyrics,
            @SerializedName("copyright") String copyright,
            @SerializedName("snippet") String snippet
    ){}
}
