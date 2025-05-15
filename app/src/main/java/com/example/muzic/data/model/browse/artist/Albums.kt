package com.example.muzic.data.model.browse.artist

import com.google.gson.annotations.SerializedName

data class Albums(
    @SerializedName("browseId")
    val browseId: Any,
    @SerializedName("results")
    val results: List<ResultAlbum>,
    @SerializedName("params")
    val params: String,
)