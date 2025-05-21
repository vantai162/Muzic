package com.example.muzic.records.sharedpref;

import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public record SavedLibrariesAudius(
        @SerializedName("lists") List<Library> lists
) {
    public record Library(
            @SerializedName("id") String id,
            @SerializedName("isCreatedByUser") boolean isCreatedByUser,
            @SerializedName("isAlbum") boolean isAlbum,
            @SerializedName("name") String name,
            @SerializedName("image") String image,
            @SerializedName("description") String description,
            @SerializedName("tracks") List<Track> tracks // ← Dùng model có sẵn
    ) {}
}
