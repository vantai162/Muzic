package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record PlaylistResponse(
        @SerializedName("data") List<Playlist> data
) {}