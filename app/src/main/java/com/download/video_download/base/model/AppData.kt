package com.download.video_download.base.model

import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class LanguageSelectData(
    val language: String,
    var isSelected: Boolean,  // 改为可变属性以支持选中状态更新
    val languageIv: Int,
    val languageCode: String,
)
data class GuideData(
    val title: String,
    val image: Int,
    val description: String
)

@Serializable
data class WebsiteData(
    @SerialName("title")
    val title: String,
    @SerialName("image")
    val image: Int,
    @SerialName("color")
    val color: String = "",
    @SerialName("url")
    val url: String
)
@Serializable
data class History(
    @SerialName("url")
    val url: String = "",
    @SerialName("time")
    val time: Long = 0
)