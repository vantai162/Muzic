package com.example.muzic.records;


import com.google.gson.annotations.SerializedName;

public record CoverPhoto(
        @SerializedName("640x") String x640,
        @SerializedName("2000x") String x2000
) {}
