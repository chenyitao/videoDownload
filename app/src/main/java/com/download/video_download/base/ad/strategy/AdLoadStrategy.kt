package com.download.video_download.base.ad.strategy

import android.content.Context
import com.download.video_download.base.ad.model.AdCount
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType

interface AdLoadStrategy {
    suspend fun load(
        context: Context,
        adUnitId: String,
        adCount: AdCount? = null,
        position: AdPosition? = null,
        adType: AdType?= null,
    ): Pair<Boolean, Any?>
}