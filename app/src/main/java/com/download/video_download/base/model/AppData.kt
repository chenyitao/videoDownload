package com.download.video_download.base.model

import android.text.SpannableString
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
data class Nt(
    @SerialName("og")
    val og: String,
    @SerialName("sNTime")
    val sNTime: Int,
    @SerialName("lockN")
    val lockN: String,
    @SerialName("lockNTime")
    val lockNTime: Int,
    @SerialName("lockNInstallTime")
    val lockNInstallTime: Int,
    @SerialName("rhN")
    val rhN: String,
    @SerialName("rhNTime")
    val rhNTime: Int,
    @SerialName("fNTime")
    val fNTime: Int,
    @SerialName("oAdB")
    val oAdB: String,
    @SerialName("hB")
    val hB: String,
    @SerialName("oB")
    val oB: String,
    @SerialName("mediaN")
    val mediaN: String,
    @SerialName("mediaNT")
    val mediaNT: Int,
)
@Serializable
data class Fuc(
    @SerialName("bBarHide")
    val bBarHide: String,
    @SerialName("unInsShow")
    val unInsShow: String,
)
@Serializable
data class RWeb(
    @SerialName("show")
    val show: String,
    @SerialName("content")
    val content: MutableList<WebsiteData> = mutableListOf(),
)
@Serializable
data class WebsiteData(
    @SerialName("title")
    val title: String,
    @SerialName("image")
    val image: Int = 0,
    @SerialName("color")
    val color: Int = -1,
    @SerialName("url")
    val url: String,
    @SerialName("sort")
    val sort: Int = -1,
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
data class TagData(
    val title: String,
    var isSelected: Boolean = false,
)
data class FrontData(
    val title: String,
    val action: String
)
data class NotifyData( val id: Int,
                   var iconSR: Int,
                   var iconBR: Int,
                   val notifyTitle: String,
                   val notifyContent: SpannableString,
                   val action: String,
                   val params: Int)