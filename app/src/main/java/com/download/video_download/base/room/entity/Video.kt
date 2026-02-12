package com.download.video_download.base.room.entity

import androidx.media3.common.MimeTypes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_table")
data class Video(
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0,
    var url: String = "",
    var thumb: String = "",
    var speed: String = "",
    var totalSize: Int = 0,
    var process: Float = 0f,
    var mimeTypes: String = "",
    var path: String = "",
    var fileName: String = "",
    var downloadStatus: Int = 0,
    var audioUrl: String = "",
    var duration: Long =  0L
)