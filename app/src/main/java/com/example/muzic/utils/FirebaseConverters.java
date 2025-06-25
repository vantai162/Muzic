package com.example.muzic.utils;

import com.example.muzic.model.Library;
import com.example.muzic.model.SavedLibrary;
import com.example.muzic.model.TrackData;
import com.example.muzic.records.Artwork;
import com.example.muzic.records.CoverPhoto;
import com.example.muzic.records.ProfilePicture;
import com.example.muzic.records.Track;
import com.example.muzic.records.User;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FirebaseConverters {

    // --- Artwork ---
    public static Artwork toArtworkRecord(com.example.muzic.model.Artwork artwork) {
        if (artwork == null) return null;
        return new Artwork(artwork._150x150, artwork._480x480, artwork._1000x1000);
    }

    public static com.example.muzic.model.Artwork toArtworkData(Artwork artwork) {
        if (artwork == null) return null;
        com.example.muzic.model.Artwork data = new com.example.muzic.model.Artwork();
        data._150x150 = artwork.x150();
        data._480x480 = artwork.x480();
        data._1000x1000 = artwork.x1000();
        return data;
    }

    // --- CoverPhoto ---
    public static CoverPhoto toCoverPhotoRecord(com.example.muzic.model.CoverPhoto photo) {
        if (photo == null) return null;
        return new CoverPhoto(photo._640x, photo._2000x);
    }

    public static com.example.muzic.model.CoverPhoto toCoverPhotoData(CoverPhoto photo) {
        if (photo == null) return null;
        com.example.muzic.model.CoverPhoto data = new com.example.muzic.model.CoverPhoto();
        data._640x = photo.x640();
        data._2000x = photo.x2000();
        return data;
    }

    // --- ProfilePicture ---
    public static ProfilePicture toProfilePictureRecord(com.example.muzic.model.ProfilePicture pic) {
        if (pic == null) return null;
        return new ProfilePicture(pic._150x150, pic._480x480, pic._1000x1000);
    }

    public static com.example.muzic.model.ProfilePicture toProfilePictureData(ProfilePicture pic) {
        if (pic == null) return null;
        com.example.muzic.model.ProfilePicture data = new com.example.muzic.model.ProfilePicture();
        data._150x150 = pic.x150();
        data._480x480 = pic.x480();
        data._1000x1000 = pic.x1000();
        return data;
    }

    // --- User ---
    public static User toUserRecord(com.example.muzic.model.User user) {
        if (user == null) return null;
        return new User(
                user.album_count,
                user.artist_pick_track_id,
                user.bio,
                toCoverPhotoRecord(user.cover_photo),
                user.followee_count,
                user.follower_count,
                user.does_follow_current_user,
                user.handle,
                user.id,
                user.is_verified,
                user.location,
                user.name,
                user.playlist_count,
                toProfilePictureRecord(user.profile_picture),
                user.repost_count,
                user.track_count,
                user.is_deactivated,
                user.is_available,
                user.erc_wallet,
                user.spl_wallet,
                user.supporter_count,
                user.supporting_count,
                user.total_audio_balance
        );
    }

    public static com.example.muzic.model.User toUserData(User user) {
        if (user == null) return null;
        com.example.muzic.model.User data = new com.example.muzic.model.User();
        data.album_count = user.albumCount();
        data.artist_pick_track_id = user.artistPickTrackId();
        data.bio = user.bio();
        data.cover_photo = toCoverPhotoData(user.coverPhoto());
        data.followee_count = user.followeeCount();
        data.follower_count = user.followerCount();
        data.does_follow_current_user = user.doesFollowCurrentUser();
        data.handle = user.handle();
        data.id = user.id();
        data.is_verified = user.isVerified();
        data.location = user.location();
        data.name = user.name();
        data.playlist_count = user.playlistCount();
        data.profile_picture = toProfilePictureData(user.profilePicture());
        data.repost_count = user.repostCount();
        data.track_count = user.trackCount();
        data.is_deactivated = user.isDeactivated();
        data.is_available = user.isAvailable();
        data.erc_wallet = user.ercWallet();
        data.spl_wallet = user.splWallet();
        data.supporter_count = user.supporterCount();
        data.supporting_count = user.supportingCount();
        data.total_audio_balance = user.totalAudioBalance();
        return data;
    }

    // --- Track ---
    public static Track toTrackRecord(TrackData data) {
        if (data == null) return null;
        return new Track(
                toArtworkRecord(data.artwork),
                data.description,
                data.genre,
                data.id,
                data.track_cid,
                data.mood,
                data.release_date,
                data.repost_count,
                data.favorite_count,
                data.tags,
                data.title,
                toUserRecord(data.user),
                data.duration,
                data.downloadable,
                data.play_count,
                data.permalink,
                data.is_streamable
        );
    }

    public static TrackData toTrackData(Track track) {
        if (track == null) return null;
        TrackData data = new TrackData();
        data.artwork = toArtworkData(track.artwork());
        data.description = track.description();
        data.genre = track.genre();
        data.id = track.id();
        data.track_cid = track.trackCid();
        data.mood = track.mood();
        data.release_date = track.releaseDate();
        data.repost_count = track.repostCount();
        data.favorite_count = track.favoriteCount();
        data.tags = track.tags();
        data.title = track.title();
        data.user = toUserData(track.user());
        data.duration = track.duration();
        data.downloadable = track.downloadable();
        data.play_count = track.playCount();
        data.permalink = track.permalink();
        data.is_streamable = track.isStreamable();
        return data;
    }

    public static List<TrackData> toTrackDataList(List<Track> tracks) {
        if (tracks == null) return null;
        List<TrackData> result = new ArrayList<>();
        for (Track track : tracks) {
            result.add(toTrackData(track));
        }
        return result;
    }

    // --- SavedLibrary
    public static SavedLibrariesAudius toSavedLibrariesAudius(SavedLibrary data) {
        if (data == null || data.getLists() == null) return new SavedLibrariesAudius(List.of());
        return new SavedLibrariesAudius(
                data.getLists().stream().map(FirebaseConverters::toLibraryRecord).collect(Collectors.toList())
        );
    }

    public static SavedLibrary toSavedLibrariesData(SavedLibrariesAudius record) {
        if (record == null || record.lists() == null) return new SavedLibrary();
        List<Library> libraryList = record.lists().stream()
                .map(FirebaseConverters::toLibraryData)
                .collect(Collectors.toList());
        return new SavedLibrary(libraryList);
    }

    // ----- Library
    public static SavedLibrariesAudius.Library toLibraryRecord(Library data) {
        return new SavedLibrariesAudius.Library(
                data.id,
                data.isCreatedByUser,
                data.isAlbum,
                data.name,
                data.artwork,
                data.description,
                data.tracks != null
                        ? data.tracks.stream().map(FirebaseConverters::toTrackRecord).collect(Collectors.toList())
                        : new ArrayList<>()
        );
    }

    public static Library toLibraryData(SavedLibrariesAudius.Library record) {
        return new Library(
                record.id(),
                record.isCreatedByUser(),
                record.isAlbum(),
                record.name(),
                record.image(),
                record.description(),
                record.tracks() != null
                        ? record.tracks().stream().map(FirebaseConverters::toTrackData).collect(Collectors.toList())
                        : new ArrayList<>()
        );
    }
}


