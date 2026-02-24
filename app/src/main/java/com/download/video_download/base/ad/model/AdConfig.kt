package com.download.video_download.base.ad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class At(
    @SerialName("ld")
    val ld: MutableList<AdData> = mutableListOf(),
    @SerialName("lg")
    val lg: MutableList<AdData> = mutableListOf(),
    @SerialName("gd")
    val gd: MutableList<AdData> = mutableListOf(),
    @SerialName("h")
    val h: MutableList<AdData> = mutableListOf(),
    @SerialName("sh")
    val sh: MutableList<AdData> = mutableListOf(),
    @SerialName("sd")
    val sd: MutableList<AdData> = mutableListOf(),
    @SerialName("bk")
    val bk: MutableList<AdData> = mutableListOf(),
    @SerialName("tb")
    val tb: MutableList<AdData> = mutableListOf(),
    @SerialName("dtd")
    val dtd: MutableList<AdData> = mutableListOf(),
)
@Serializable
data class AdData(
    @SerialName("uid")
    val uid: String?,
    @SerialName("at")
    val at: Int = -1,
    @SerialName("p")
    val p: Int = 0,
    @SerialName("et")
    val et: Long = 0,
)

@Serializable
data class Ac(
    @SerialName("tsc")
    val tsc: Int,
    @SerialName("tcc")
    val tcc: Int,
    @SerialName("ncp")
    val ncp: Int,
    @SerialName("installHour")
    val installHour: Int,
)

@Serializable
data class Config(
    @SerialName("adcg")
    val adcg:AdModel
)

@Serializable
data class AdModel(
    @SerialName("ac")
    val ac: Ac?,
    @SerialName("at")
    val at: At?
)
data class AdManageData(
    val advert: Map<AdPosition, List<AdData>>,
    val config: Ac?
)

