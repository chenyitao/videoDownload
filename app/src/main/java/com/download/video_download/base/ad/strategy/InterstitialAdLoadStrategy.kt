package com.download.video_download.base.ad.strategy

import android.content.Context
import com.download.video_download.base.ad.model.AdCount
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InterstitialAdLoadStrategy : AdLoadStrategy {
    override suspend fun load(
        context: Context,
        adUnitId: String,
        adCount: AdCount?,
        position: AdPosition?,
        adType: AdType?
    ): Pair<Boolean, Any?>  = suspendCoroutine { cont ->
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    cont.resume(Pair(true, ad))
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    cont.resume(Pair(false, adError))
                }
            }
        )
    }
}