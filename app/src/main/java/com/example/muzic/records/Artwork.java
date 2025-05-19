package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

public record Artwork(
        @SerializedName("150x150") String x150,
        @SerializedName("480x480") String x480,
        @SerializedName("1000x1000") String x1000
) {}
