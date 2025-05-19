package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

public record TrackResponse(
        @SerializedName("data") Track data
) {}