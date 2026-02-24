package com.download.video_download.base.ad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Advert(
    @SerialName("loading")
    val loading: MutableList<AdData> = mutableListOf(),
    @SerialName("language")
    val language: MutableList<AdData> = mutableListOf(),
    @SerialName("guide")
    val guide: MutableList<AdData> = mutableListOf(),
    @SerialName("home")
    val home: MutableList<AdData> = mutableListOf(),
    @SerialName("search")
    val search: MutableList<AdData> = mutableListOf(),
    @SerialName("startDownload")
    val startDownload: MutableList<AdData> = mutableListOf(),
    @SerialName("back")
    val back: MutableList<AdData> = mutableListOf(),
    @SerialName("tab")
    val tab: MutableList<AdData> = mutableListOf(),
    @SerialName("downloadTaskDialog")
    val downloadTaskDialog: MutableList<AdData> = mutableListOf(),
)
@Serializable
data class AdData(
    @SerialName("ad_unit_id")
    val ad_unit_id: String?,
    @SerialName("adType")
    val adType: Int = -1,
    @SerialName("priority")
    val priority: Int = 0,
    @SerialName("expired")
    val expired: Long = 0,
)

@Serializable
data class AdConfig(
    @SerialName("todayShowCount")
    val todayShowCount: Int,
    @SerialName("todayClickCount")
    val todayClickCount: Int,
    @SerialName("nativeClickPlace")
    val nativeClickPlace: Int,
    @SerialName("installNewUac001Hour")
    val installNewUac001Hour: Int,
)

@Serializable
data class Config(
    @SerialName("advertConfig")
    val advertConfig:AdModel
)

@Serializable
data class AdModel(
    @SerialName("config")
    val config: AdConfig?,
    @SerialName("advert")
    val advert: Advert?
)
data class AdManageData(
    val advert: Map<AdPosition, List<AdData>>,
    val config: AdConfig?
)

