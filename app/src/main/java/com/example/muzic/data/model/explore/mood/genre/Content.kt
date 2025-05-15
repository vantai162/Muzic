package com.example.muzic.data.model.explore.mood.genre

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.example.muzic.data.model.searchResult.songs.Thumbnail
import com.example.muzic.data.type.HomeContentType

@Immutable
data class Content(
    @SerializedName("playlistBrowseId")
    val playlistBrowseId: String,
    @SerializedName("thumbnail")
    val thumbnail: List<Thumbnail>?,
    @SerializedName("title")
    val title: Title,
) : HomeContentType