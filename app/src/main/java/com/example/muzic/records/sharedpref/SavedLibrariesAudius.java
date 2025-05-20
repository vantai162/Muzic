package com.example.muzic.records.sharedpref;

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
            @SerializedName("tracks") List<Track> tracks
    ) {
    }

    public record Track(
            @SerializedName("id") String id,
            @SerializedName("title") String title,
            @SerializedName("genre") String genre,
            @SerializedName("duration") int duration,
            @SerializedName("artworkUrl") String artworkUrl
    ) {
    }

    public record User(
            @SerializedName("id") String id,
            @SerializedName("handle") String handle,
            @SerializedName("name") String name,
            @SerializedName("profilePicture") String profilePicture,
            @SerializedName("isVerified") boolean isVerified
    ) {
    }
}