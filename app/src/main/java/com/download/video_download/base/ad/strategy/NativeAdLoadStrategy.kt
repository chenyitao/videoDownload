package com.download.video_download.base.ad.strategy

import android.content.Context
import com.download.video_download.base.ad.model.AdCount
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.utils.AppCache
import kotlinx.serialization.encodeToString
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NativeAdLoadStrategy : AdLoadStrategy {
    override suspend fun load(
        context: Context,
        adUnitId: String,
        adCount: AdCount?,
        position: AdPosition?,
        adType: AdType?,
    ): Pair<Boolean, Any?>  = suspendCoroutine { cont ->
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                cont.resume(Pair(true, nativeAd))
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    cont.resume(Pair(false, adError))
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCount?.todayClickCount += 1
                    AppCache.adLimitC =  context.jsonParser().encodeToString(adCount)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }
}