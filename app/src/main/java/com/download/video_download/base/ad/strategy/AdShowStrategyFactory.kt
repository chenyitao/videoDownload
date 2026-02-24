package com.download.video_download.base.ad.strategy

import com.download.video_download.base.ad.model.AdType

object AdShowStrategyFactory {
    fun getStrategy(adType: AdType): AdShowStrategy = when (adType) {
        AdType.APP_OPEN -> AppOpenAdShowStrategy()
        AdType.INTERSTITIAL -> InterstitialStrategy()
        AdType.NATIVE -> NativeAdShowStrategy()
    }
}