package com.example.muzic.records;



import com.google.gson.annotations.SerializedName;

public record User(
        @SerializedName("album_count") int albumCount,
        @SerializedName("artist_pick_track_id") String artistPickTrackId,
        @SerializedName("bio") String bio,
        @SerializedName("cover_photo") CoverPhoto coverPhoto,
        @SerializedName("followee_count") int followeeCount,
        @SerializedName("follower_count") int followerCount,
        @SerializedName("does_follow_current_user") boolean doesFollowCurrentUser,
        @SerializedName("handle") String handle,
        @SerializedName("id") String id,
        @SerializedName("is_verified") boolean isVerified,
        @SerializedName("location") String location,
        @SerializedName("name") String name,
        @SerializedName("playlist_count") int playlistCount,
        @SerializedName("profile_picture") ProfilePicture profilePicture,
        @SerializedName("repost_count") int repostCount,
        @SerializedName("track_count") int trackCount,
        @SerializedName("is_deactivated") boolean isDeactivated,
        @SerializedName("is_available") boolean isAvailable,
        @SerializedName("erc_wallet") String ercWallet,
        @SerializedName("spl_wallet") String splWallet,
        @SerializedName("supporter_count") int supporterCount,
        @SerializedName("supporting_count") int supportingCount,
        @SerializedName("total_audio_balance") int totalAudioBalance
) {}
