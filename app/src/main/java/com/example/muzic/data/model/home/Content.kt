package com.example.muzic.data.model.home

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.example.muzic.data.model.searchResult.songs.Album
import com.example.muzic.data.model.searchResult.songs.Artist
import com.example.muzic.data.model.searchResult.songs.Thumbnail
import com.example.muzic.data.type.HomeContentType

@Immutable
data class Content(
    @SerializedName("album")
    val album: Album?,
    @SerializedName("artists")
    val artists: List<Artist>?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("isExplicit")
    val isExplicit: Boolean?,
    @SerializedName("playlistId")
    val playlistId: String?,
    @SerializedName("browseId")
    val browseId: String?,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>,
    @SerializedName("title")
    val title: String,
    @SerializedName("videoId")
    val videoId: String?,
    @SerializedName("views")
    val views: String?,
    @SerializedName("durationSeconds")
    val durationSeconds: Int? = null,
    val radio: String? = null,
) : HomeContentType