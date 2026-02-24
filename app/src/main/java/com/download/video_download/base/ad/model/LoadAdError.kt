package com.download.video_download.base.ad.model

data class LoadAdError(
    val code: Int,
    val message: String,
    val domain: String
)