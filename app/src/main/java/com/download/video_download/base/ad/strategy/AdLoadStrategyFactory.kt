package com.download.video_download.base.ad.strategy

import com.download.video_download.base.ad.model.AdType

object AdLoadStrategyFactory {
    fun getStrategy(adType: AdType): AdLoadStrategy = when (adType) {
        AdType.APP_OPEN -> AppOpenAdLoadStrategy()
        AdType.INTERSTITIAL -> InterstitialAdLoadStrategy()
        AdType.NATIVE -> NativeAdLoadStrategy()
    }
}