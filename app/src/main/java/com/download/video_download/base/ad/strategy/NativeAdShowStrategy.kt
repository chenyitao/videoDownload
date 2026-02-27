package com.download.video_download.base.ad.strategy

import android.app.Activity
import com.google.android.gms.ads.nativead.NativeAd
import com.download.video_download.base.ad.block.AdInteractionCallback
import com.download.video_download.base.ad.block.AdShowCallback
import com.download.video_download.base.ad.model.AdCount
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.utils.AppCache
import kotlinx.serialization.encodeToString

class NativeAdShowStrategy : AdShowStrategy {
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
        adCount?.todayShowCount += 1
        AppCache.adLimitC =  activity.jsonParser().encodeToString(adCount)
        showCallback(position, adType, true, null)
//        if (!keepCache) destroy(adInstance)
    }

    override fun destroy(adInstance: Any) {
        (adInstance as NativeAd).destroy()
    }
}