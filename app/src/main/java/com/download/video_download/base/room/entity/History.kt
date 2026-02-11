package com.download.video_download.base.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class History(
    val url: String = "",
    @PrimaryKey
    val time: Long = 0
)