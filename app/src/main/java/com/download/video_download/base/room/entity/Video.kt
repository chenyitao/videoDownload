package com.download.video_download.base.room.entity

import androidx.media3.common.MimeTypes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_table")
data class Video(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val url: String = "",
    val thumb: String = "",
    val speed: String = "",
    val totalSize: Int = 0,
    val process: Float = 0f,
    val mimeTypes: String = "",
    val path: String = "",
    val fileName: String = "",
    val downloadStatus: Int = 0,
)