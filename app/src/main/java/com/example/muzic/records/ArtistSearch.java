package com.example.muzic.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @param success
 * @param data
 * <p>To be used when called retrieveArtistsByIdOrLink</p>
 */
public record ArtistSearch(
        @SerializedName("success") boolean success,
        @SerializedName("data") Data data
) {
    public record Data(
            @SerializedName("id") String id,
            @SerializedName("name") String name,
            @SerializedName("url") String url,
            @SerializedName("type") String type,
            @SerializedName("followerCount") int followerCount,
            @SerializedName("fanCount") int fanCount,
            @SerializedName("isVerified") boolean isVerified,
            @SerializedName("dominantLanguage") String dominantLanguage,
            @SerializedName("dominantType") String dominantType,
            @SerializedName("bio") List<Bio> bio,
            @SerializedName("dob") String dob,
            @SerializedName("fb") String fb,
            @SerializedName("twitter") String twitter,
            @SerializedName("wiki") String wiki,
            @SerializedName("availableLanguages") List<String> availableLanguages,
            @SerializedName("isRadioPresent") boolean isRadioPresent,
            @SerializedName("image") List<GlobalSearch.Image> image,
            @SerializedName("topSongs") List<SongResponse.Song> topSongs,
            @SerializedName("topAlbums") List<AlbumsSearch.Data.Results> topAlbums,
            @SerializedName("singles") List<AlbumsSearch.Data.Results> singles,
            @SerializedName("similarArtists") List<SimilarArtist> similarArtists

    ) {
        public record Bio(
                @SerializedName("text") String text,
                @SerializedName("title") String title,
                @SerializedName("sequence") int sequence
        ){}

        public record SimilarArtist(
                @SerializedName("id") String id,
                @SerializedName("name") String name
        ){}

    }
}
