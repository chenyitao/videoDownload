package com.download.video_download.base.ad.model

data class AdCacheMeta(
    val adInstance: Any,
    val expireTime: Long,
    val cacheTime : Long,
    val adId: String? = null,
)