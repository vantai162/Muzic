package com.example.muzic.model;

import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.example.muzic.utils.FirebaseConverters;

import java.util.ArrayList;
import java.util.List;

public class Library {
    public String id;
    public boolean isAlbum;
    public boolean isCreatedByUser;
    public String name;
    public String artwork;
    public String description;
    public List<TrackData> tracks;

    public Library() {}

    public Library(String id, boolean isAlbum, boolean isCreatedByUser, String name,
                       String artwork, String description, List<TrackData> tracks) {
        this.id = id;
        this.isAlbum = isAlbum;
        this.isCreatedByUser = isCreatedByUser;
        this.name = name;
        this.artwork = artwork;
        this.description = description;
        this.tracks = tracks;
    }

    public static Library convertToLibraryData(SavedLibrariesAudius.Library library) {
        List<TrackData> trackDataList = FirebaseConverters.toTrackDataList(library.tracks());

        return new Library(
                library.id(),
                library.isAlbum(),
                library.isCreatedByUser(),
                library.name(),
                library.image(),
                library.description(),
                trackDataList
        );
    }
}
