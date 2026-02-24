package com.download.video_download.base.ad.block

import com.google.android.gms.ads.AdError
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ad.model.LoadAdError

typealias AdLoadCallback = (position: AdPosition, adType: AdType, state: AdLoadState, error: LoadAdError?) -> Unit

typealias AdShowCallback = (position: AdPosition, adType: AdType, success: Boolean, error: AdError?) -> Unit

typealias AdInteractionCallback = (position: AdPosition, adType: AdType) -> Unit

val emptyLoadCallback: AdLoadCallback = { _, _, _, _ -> }
val emptyShowCallback: AdShowCallback = { _, _, _, _ -> }
val emptyClickCallback: AdInteractionCallback = { _, _ -> }
val emptyImpressionCallback: AdInteractionCallback = { _, _ -> }
val emptyDismissCallback: AdInteractionCallback = { _, _ -> }