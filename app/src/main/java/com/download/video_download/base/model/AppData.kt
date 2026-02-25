package com.download.video_download.base.model

import androidx.room.PrimaryKey
import com.download.video_download.base.room.entity.Video
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class LanguageSelectData(
    val language: String,
    var isSelected: Boolean,
    val languageIv: Int,
    var languageCode: String,
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

data class NavigationItem(
    val params: String,
    val from: NavState,
    val route: NavState,
    val video: MutableList<Video>? = mutableListOf(),
)

data class DetectStatus(
    val state: DetectState = DetectState.SUPPORTWEB,
)
@Serializable
data class Rf(
    @SerialName("referrerUrl")
    val referrerUrl: String,
    @SerialName("referrerClickTimestampSeconds")
    val referrerClickTimestampSeconds: Long,
    @SerialName("referrerClickTimestampServerSeconds")
    val referrerClickTimestampServerSeconds: Long,
    @SerialName("installBeginTimestampSeconds")
    val installBeginTimestampSeconds: Long,
    @SerialName("googlePlayInstantParam")
    val googlePlayInstantParam: Boolean,
    @SerialName("installBeginTimestampServerSeconds")
    val installBeginTimestampServerSeconds: Long,
    @SerialName("firstInstallTime")
    val firstInstallTime: Long,
    @SerialName("lastUpdateTime")
    val lastUpdateTime: Long,
    @SerialName("installVersion")
    val installVersion: String?
)