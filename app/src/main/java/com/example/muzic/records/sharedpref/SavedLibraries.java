package com.example.muzic.records.sharedpref;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record SavedLibraries(
        @SerializedName("lists") List<Library> lists
) {
    public record Library(
            @SerializedName("id") String id,
            @SerializedName("isCreatedByUser") boolean isCreatedByUser,
            @SerializedName("isAlbum") boolean isAlbum,
            @SerializedName("name") String name,
            @SerializedName("image") String image,
            @SerializedName("description") String description,
            @SerializedName("songs") List<Songs> songs
    ){
        public record Songs(
                @SerializedName("id") String id,
                @SerializedName("title") String title,
                @SerializedName("description") String description,
                @SerializedName("image") String image
        ){}
    }
}
