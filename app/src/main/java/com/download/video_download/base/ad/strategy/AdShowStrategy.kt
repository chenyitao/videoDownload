package com.download.video_download.base.ad.strategy

import android.app.Activity
import com.download.video_download.base.ad.block.AdInteractionCallback
import com.download.video_download.base.ad.block.AdShowCallback
import com.download.video_download.base.ad.model.AdCount
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType

interface AdShowStrategy {
    suspend fun show(
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
    )

    fun destroy(adInstance: Any)
}