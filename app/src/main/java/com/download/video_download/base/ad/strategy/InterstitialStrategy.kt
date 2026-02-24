package com.download.video_download.base.ad.strategy

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.download.video_download.base.ad.block.AdInteractionCallback
import com.download.video_download.base.ad.block.AdShowCallback
import com.download.video_download.base.ad.model.AdCount
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.utils.AppCache
import kotlinx.serialization.encodeToString

class InterstitialStrategy : AdShowStrategy {


    override suspend fun show(
        adInstance: Any,
        position: AdPosition,
        adType: AdType,
        activity: Activity,
        keepCache: Boolean,
        showCallback: AdShowCallback,
        clickCallback: AdInteractionCallback,
        impressionCallback: AdInteractionCallback,
        dismissCallback: AdInteractionCallback,
        adCount: AdCount?,
    ) {
        val interstitialAd = adInstance as InterstitialAd
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                dismissCallback(position, adType)
                if (!keepCache) destroy(interstitialAd)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                showCallback(position, adType, false, adError)
                if (!keepCache) destroy(interstitialAd)
            }

            override fun onAdShowedFullScreenContent() {
                adCount?.todayShowCount += 1
                AppCache.adLimitC =  activity.jsonParser().encodeToString(adCount)
                showCallback(position, adType, true, null)
            }

            override fun onAdImpression() {
                impressionCallback(position, adType)
            }

            override fun onAdClicked() {
                adCount?.todayClickCount += 1
                AppCache.adLimitC =  activity.jsonParser().encodeToString(adCount)
                clickCallback(position, adType)
            }
        }
        interstitialAd.show(activity)
    }

    override fun destroy(adInstance: Any) {
    }
}