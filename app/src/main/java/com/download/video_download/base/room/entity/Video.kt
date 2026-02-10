package com.download.video_download.base.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_table")
data class Video(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
)