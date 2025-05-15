package com.example.muzic.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.muzic.data.model.metadata.Line

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey(autoGenerate = false) val videoId: String,
    val error: Boolean,
    val lines: List<Line>?,
    val syncType: String?,
)