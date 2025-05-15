package com.example.muzic.data.model.browse.artist

import com.example.muzic.data.model.searchResult.songs.Thumbnail
import com.example.muzic.data.type.HomeContentType

data class ResultPlaylist(
    val id: String,
    val author: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
) : HomeContentType