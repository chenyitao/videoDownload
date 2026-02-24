package com.download.video_download.base.ad.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdCount(
    @SerialName("todayShowCount")
    var todayShowCount: Int,
    @SerialName("todayClickCount")
    var todayClickCount: Int,
    @SerialName( "today")
    var today: Long,
)