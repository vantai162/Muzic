package com.example.muzic.data.model.searchResult.songs

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String,
)