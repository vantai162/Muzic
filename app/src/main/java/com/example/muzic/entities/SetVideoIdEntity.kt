package com.example.muzic.data.db.entities

import androidx.room.Entity

@Entity(tableName = "set_video_id", primaryKeys = ["videoId", "youtubePlaylistId"])
data class SetVideoIdEntity(
    val videoId: String = "",
    val setVideoId: String? = null,
    val youtubePlaylistId: String = "",
)