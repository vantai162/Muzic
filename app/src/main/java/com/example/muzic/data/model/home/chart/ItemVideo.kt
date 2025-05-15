package com.example.muzic.data.model.home.chart

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.example.muzic.data.model.browse.album.Track
import com.example.muzic.data.model.searchResult.songs.Artist
import com.example.muzic.data.model.searchResult.songs.Thumbnail

@Immutable
data class ItemVideo(
    @SerializedName("artists")
    val artists: List<Artist>?,
    @SerializedName("playlistId")
    val playlistId: String,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>,
    @SerializedName("title")
    val title: String,
    @SerializedName("videoId")
    val videoId: String,
    @SerializedName("views")
    val views: String,
)

fun ItemVideo.toTrack(): Track =
    Track(
        album = null,
        artists = artists,
        duration = "",
        durationSeconds = 0,
        isAvailable = false,
        isExplicit = false,
        likeStatus = "INDIFFERENT",
        thumbnails = thumbnails,
        title = title,
        videoId = videoId,
        videoType = "",
        category = null,
        feedbackTokens = null,
        resultType = null,
        year = "",
    )