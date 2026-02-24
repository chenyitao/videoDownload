package com.download.video_download.base.ad.model

enum class AdType(val type: Int) {
    APP_OPEN(1),
    INTERSTITIAL(3),
    NATIVE(5);
    companion object {
        fun fromType(type: Int): AdType? = entries.find { it.type == type }
    }
}