package com.download.video_download.base.ad.strategy

import android.content.Context
import com.download.video_download.base.ad.model.AdCount
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AppOpenAdLoadStrategy : AdLoadStrategy {
    override suspend fun load(
        context: Context,
        adUnitId: String,
        adCount: AdCount?,
        position: AdPosition?,
        adType: AdType?
    ): Pair<Boolean, Any?> = suspendCoroutine { cont ->
        AppOpenAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    cont.resume(Pair(true, ad))
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    cont.resume(Pair(false, loadAdError))
                }
            }
        )
    }
}