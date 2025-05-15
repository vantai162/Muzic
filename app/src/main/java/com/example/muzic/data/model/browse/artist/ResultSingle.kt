package com.example.muzic.data.model.browse.artist

import com.google.gson.annotations.SerializedName
import com.example.muzic.data.model.searchResult.songs.Thumbnail
import com.example.muzic.data.type.HomeContentType

data class ResultSingle(
    @SerializedName("browseId")
    val browseId: String,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>,
    @SerializedName("title")
    val title: String,
    @SerializedName("year")
    val year: String,
) : HomeContentType