package com.download.video_download.base.ad

import android.app.Activity
import android.content.Context
import com.appsflyer.AFAdRevenueData
import com.appsflyer.MediationNetwork
import com.download.video_download.App
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.download.video_download.base.ad.block.AdInteractionCallback
import com.download.video_download.base.ad.block.AdLoadCallback
import com.download.video_download.base.ad.block.AdShowCallback
import com.download.video_download.base.ad.block.emptyClickCallback
import com.download.video_download.base.ad.block.emptyDismissCallback
import com.download.video_download.base.ad.block.emptyImpressionCallback
import com.download.video_download.base.ad.block.emptyLoadCallback
import com.download.video_download.base.ad.block.emptyShowCallback
import com.download.video_download.base.ad.model.Ac
import com.download.video_download.base.ad.model.AdCacheMeta
import com.download.video_download.base.ad.model.AdCount
import com.download.video_download.base.ad.model.AdData
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdManageData
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ad.model.At
import com.download.video_download.base.ad.model.Config
import com.download.video_download.base.ad.model.LoadAdError
import com.download.video_download.base.ad.strategy.AdLoadStrategyFactory
import com.download.video_download.base.ad.strategy.AdShowStrategyFactory
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.config.sensor.TrackParamBuilder
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.TimeFormateUtils
import com.facebook.appevents.AppEventsConstants
import com.google.android.gms.ads.AdValue
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import java.util.Currency
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.getOrPut
import kotlin.collections.set
import kotlin.let
import kotlin.run
import kotlin.takeIf
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty

class AdMgr private constructor() {
    companion object {
        val INSTANCE: AdMgr by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AdMgr()
        }
    }
    private var adManageData: AdManageData? = null
    private var advert: At? = null
    private var adConfig: Ac? = null
    private val loadedAdCache = mutableMapOf<Pair<AdPosition, AdType>, AdCacheMeta>()
    private val loadStateCache = ConcurrentHashMap<Pair<AdPosition, AdType>, AdLoadState>()
    private val loadLocks = ConcurrentHashMap<Pair<AdPosition, AdType>, Mutex>()
    private var isPrivacyConsentGiven = true
    private var adCount: AdCount? =  null
    private val MIN_REPORT_VALUE = BigDecimal("0.01")
    private var acRevenue: BigDecimal = BigDecimal.ZERO
    private val decimalPlaces: Int = 6
    fun initAdData() {
        val remoteAdConfig = AppCache.adcf
        remoteAdConfig.takeIf { it.isNotEmpty() }?.let { config ->
            val adModel = App.getAppContext().jsonParser().decodeFromString<Config>(config)
            adModel.adcg.let { ad ->
                advert = ad.at
                adConfig = ad.ac
            }
        }
        initAdLimitTime()
        val advertMap = mutableMapOf<AdPosition, List<AdData>>()
        advertMap[AdPosition.LOADING] =  advert?.ld ?: emptyList()
        advertMap[AdPosition.LANGUAGE] = advert?.lg ?: emptyList()
        advertMap[AdPosition.GUIDE] = advert?.gd ?: emptyList()
        advertMap[AdPosition.HOME] = advert?.h ?: emptyList()
        advertMap[AdPosition.SEARCH] = advert?.sh ?: emptyList()
        advertMap[AdPosition.START_DOWNLOAD] = advert?.sd ?: emptyList()
        advertMap[AdPosition.BACK] = advert?.bk ?: emptyList()
        advertMap[AdPosition.TAB] = advert?.tb ?: emptyList()
        advertMap[AdPosition.DOWNLOAD_TASK_DIALOG] = advert?.dtd ?: emptyList()
        adManageData = AdManageData(advertMap, adConfig)
    }
    fun initAdLimitTime(){
        val adCountStr = AppCache.adLimitC
        if (adCountStr.isEmpty()){
            adCount = AdCount( 0, 0, System.currentTimeMillis())
        }else{
            adCount = App.getAppContext().jsonParser().decodeFromString(adCountStr)
            val isSameDay = TimeFormateUtils.isSameDayApi26(adCount?.today ?: 0L, System.currentTimeMillis())
            if (!isSameDay){
                adCount?.todayShowCount = 0
                adCount?.todayClickCount = 0
                adCount?.today = System.currentTimeMillis()
            }
        }
    }
    fun getAdManageData(): AdManageData? {
        return adManageData
    }
    fun setPrivacyConsent(consentGiven: Boolean) {
        isPrivacyConsentGiven = consentGiven
    }
    fun getAdLoadState(position: AdPosition, adType: AdType): AdLoadState {
        return loadStateCache[Pair(position, adType)] ?: AdLoadState.UNLOADED
    }
    fun getNativeAd(position: AdPosition): NativeAd? {
        return loadedAdCache[Pair(position, AdType.NATIVE)]?.adInstance as? NativeAd
    }
    fun hasCachedAd(position: AdPosition, adType: AdType): Boolean {
        return loadedAdCache.containsKey(Pair(position, adType))
    }
    fun isClickLimitReached(): Boolean {
        val config = adConfig ?: return true
        val curTodayClickCount = adCount?.todayClickCount ?:0
        return curTodayClickCount >= config.tcc
    }
    fun isShowLimitReached(): Boolean {
        val config = adConfig ?: return true
        val curTodayShowCount = adCount?.todayShowCount ?:0
        return  curTodayShowCount >= config.tsc
    }
    private fun getLoadLock(position: AdPosition, adType: AdType): Mutex {
        val key = Pair(position, adType)
        return loadLocks.getOrPut(key) { Mutex() }
    }
    fun isDailyLimitReached(): Boolean {
        val config = adConfig ?: return true
        val curTodayShowCount = adCount?.todayShowCount ?:0
        val curTodayClickCount = adCount?.todayClickCount ?:0
        val limit = curTodayShowCount >= config.tsc || curTodayClickCount >= config.tcc
        return limit
    }
    fun isAdExpired(cacheMeta: AdCacheMeta?): Boolean {
        if (cacheMeta?.cacheTime == 0L) return true
        val timeElapsed = Date().time - (cacheMeta?.cacheTime?:0)
        val expiryTime =(cacheMeta?.expireTime?:0) * 60000L
        return timeElapsed >= expiryTime
    }
    private val showingAdCache = mutableSetOf<Pair<AdPosition, AdType>>()
    private val showingAdLock = Mutex()

    private suspend fun isAdShowing(position: AdPosition, adType: AdType): Boolean {
        return showingAdLock.withLock {
            showingAdCache.contains(Pair(position, adType))
        }
    }

    private suspend fun markAdShowing(position: AdPosition, adType: AdType) {
        showingAdLock.withLock {
            showingAdCache.add(Pair(position, adType))
        }
    }

    private suspend fun unmarkAdShowing(position: AdPosition, adType: AdType) {
        showingAdLock.withLock {
            showingAdCache.remove(Pair(position, adType))
        }
    }

    suspend fun preloadAd(
        position: AdPosition,
        adType: AdType,
        context: Context,
        onLoadStateChanged: AdLoadCallback = emptyLoadCallback
    ) {
        if (!isPrivacyConsentGiven) {
            val error = LoadAdError(-1, "PRIVACY NOT AGREE", "UMP NO PERMIT，NOT LOADING AD")
            onLoadStateChanged(position, adType, AdLoadState.FAILED, error)
            return
        }
        if (isDailyLimitReached()) {
            val error = LoadAdError(-2, "Daily Limit", "The daily ad impressions/clicks limit has been reached")
            onLoadStateChanged(position, adType, AdLoadState.FAILED, error)
            return
        }
        val adUnitList = adManageData?.advert?.get(position)?.filter { it.at == adType.type } ?: emptyList()
        if (adUnitList.isEmpty()) {
            val error = LoadAdError(-3, "No advertising data", "no found ${position.name}-${adType.name} ad unit")
            onLoadStateChanged(position, adType, AdLoadState.FAILED, error)
            return
        }

        val key = Pair(position, adType)
        getLoadLock(position, adType).withLock {
            val cachedMeta = loadedAdCache[key]
            if (isAdExpired(cachedMeta)) {
                loadedAdCache.remove(key)
                loadStateCache.remove(key)
            }
        }
        val currentState = loadStateCache[key] ?: AdLoadState.UNLOADED
        if (currentState == AdLoadState.LOADED || currentState == AdLoadState.LOADING) {
            val key = Pair(position, adType)
            val cachedMeta = loadedAdCache[key]
            val message = if (currentState == AdLoadState.LOADED) "Advertising data already exists" else "The advertisement is currently cached"
            val domain = if (currentState == AdLoadState.LOADED) "found ${position.name}-${adType.name} ad unit ,ad data :${cachedMeta?.adInstance}" else "loading ${position.name}-${adType.name} ad unit"
            val error = LoadAdError(-4,  message,domain)
            onLoadStateChanged(position, adType, currentState, error)
            return
        }

        getLoadLock(position, adType).withLock {
            val latestState = loadStateCache[key] ?: AdLoadState.UNLOADED
            if (latestState == AdLoadState.LOADED || latestState == AdLoadState.LOADING) {
                onLoadStateChanged(position, adType, latestState, null)
                return@withLock
            }
            TrackMgr.instance.trackAdEvent(position,adType, TrackEventType.safedddd_bf)

            loadStateCache[key] = AdLoadState.LOADING
            val message = "The advertisement is currently cached"
            val domain = "Loading ${position.name}-${adType.name}ad unit"
            val error = LoadAdError(-4,  message,domain)
            onLoadStateChanged(position, adType, AdLoadState.LOADING, error)
            var loadSuccess = false
            var loadedMeta: AdCacheMeta? = null
            var adError: LoadAdError? = null
            for (adUnit in adUnitList) {
                val loadStrategy = AdLoadStrategyFactory.getStrategy(adType)
                val (success, adInstance) = if (adType.type == AdType.NATIVE.type) loadStrategy.load(context, adUnit.uid?:"",adCount,position,adType) else loadStrategy.load(context, adUnit.uid?:"")
                if (success && adInstance != null) {
                    loadedMeta = AdCacheMeta(
                        adInstance = adInstance,
                        expireTime = adUnit.et,
                        cacheTime = System.currentTimeMillis(),
                        adId = adUnit.uid?:""
                    )
                    loadedAdCache[key] = loadedMeta
                    loadSuccess = true
                    TrackMgr.instance.trackAdEvent(position,adType, TrackEventType.safedddd_cgg)
                    break
                }else{
                    val error = adInstance as com.google.android.gms.ads.LoadAdError
                    adError = LoadAdError(error.code, error.message, error.domain)
                    TrackMgr.instance.trackAdEvent(position,adType, TrackEventType.safedddd_sbb,adError)
                }
            }

            if (loadSuccess) {
                loadStateCache[key] = AdLoadState.LOADED
                val message ="Advertising data loaded successfully"
                val domain = "${position.name}-${adType.name} ad unit ,advertising data:${loadedMeta?.adInstance}"
                val error = LoadAdError(-4,  message,domain)
                onLoadStateChanged(position, adType, AdLoadState.LOADED, error)
            } else {
                loadStateCache[key] = AdLoadState.FAILED
                val error = LoadAdError(adError?.code?:-1, adError?.message?:"", "${position.name}-${adType.name} Ad unit loading failed")
                onLoadStateChanged(position, adType, AdLoadState.FAILED, error)
            }
        }
    }
    suspend fun batchPreloadAds(
        adPairs: List<Pair<AdPosition, AdType>>,
        context: Context,
        onLoadStateChanged: AdLoadCallback = emptyLoadCallback
    ) {
        coroutineScope {
            adPairs.forEach { (position, adType) ->
                launch {
                    preloadAd(position, adType, context, onLoadStateChanged)
                }
            }
        }
    }
    suspend fun showAd(
        position: AdPosition,
        adType: AdType,
        activity: Activity,
        keepCache: Boolean = false,
        onShowResult: AdShowCallback = emptyShowCallback,
        onAdClicked: AdInteractionCallback = emptyClickCallback,
        onAdImpression: AdInteractionCallback = emptyImpressionCallback,
        onAdDismissed: AdInteractionCallback = emptyDismissCallback
    ) {
        if (isAdShowing(position, adType)) {
            val error = AdError(-7, "Advertising Display", "${position.name}-${adType.name}The advertisement is currently being displayed and cannot be called repeatedly")
            onShowResult(position, adType, false, error)
            return
        }
        // 前置检查
        if (!isPrivacyConsentGiven) {
            val error = AdError(-1, "PRIVACY NOT AGREE", "UMP NO PERMIT，NOT LOADING AD")
            onShowResult(position, adType, false, error)
            return
        }
        if (isDailyLimitReached()) {
            val error = AdError(-2, "Daily Limit", "The daily ad impressions/clicks limit has been reached")
            onShowResult(position, adType, false, error)
            return
        }

        val key = Pair(position, adType)
        getLoadLock(position, adType).withLock {
            val cachedMeta = loadedAdCache[key]
            if (isAdExpired(cachedMeta)) {
                loadedAdCache.remove(key)
                loadStateCache.remove(key)
            }
        }
        var currentState = getAdLoadState(position, adType)

        if (currentState == AdLoadState.UNLOADED || currentState == AdLoadState.FAILED) {
            val loadSuccess = awaitAdLoaded(position, adType, activity, timeoutMs = 10000)
            if (!loadSuccess) {
                val error = AdError(-3, "Advertisement not ready", "${position.name}-${adType.name}Ad loading incomplete/timeout")
                onShowResult(position, adType, false, error)
                return
            }
            currentState = getAdLoadState(position, adType)
        }

        if (currentState == AdLoadState.LOADING) {
            val loadSuccess = awaitAdLoaded(position, adType, activity, timeoutMs = 10000)
            if (!loadSuccess) {
                val error = AdError(-4, "Loading", "${position.name}-${adType.name}Ad loading timeout")
                onShowResult(position, adType, false, error)
                return
            }
            currentState = getAdLoadState(position, adType)
        }
        val adMeta = loadedAdCache[key] ?: run {
            val error = AdError(-5, "No cache", "${position.name}-${adType.name}Ad instance cache does not exist")
            onShowResult(position, adType, false, error)
            return
        }
        if (isAdExpired(adMeta)) {
            val error = AdError(-6, "Advertisement expires", "${position.name}-${adType.name}The advertisement has expired, please reload")
            onShowResult(position, adType, false, error)
            return
        }
        when(adType.type){
            AdType.NATIVE.type->{
                val nativeAd =  adMeta.adInstance as NativeAd
                nativeAd.setOnPaidEventListener {
                    val params = TrackParamBuilder.createAdImpressionParams(it,nativeAd.responseInfo,adMeta.adId?:"", position.desc, adType.type).build()
                    TrackMgr.instance.trackEvent(TrackEventType.AD_IMPRESSION,params )
                    val adRevenueData = AFAdRevenueData(
                        "admob",
                        MediationNetwork.GOOGLE_ADMOB,
                        it.currencyCode,
                        (it.valueMicros/1000000.0)
                    )
                    val afParams = TrackParamBuilder.createAfAdRevenueParams(it,adMeta.adId?:"", position.desc, adType.type).build()
                    TrackMgr.instance.trackAppflyEvent(adRevenueData,afParams )

                    val firebaseParams = mutableMapOf<String, Any>()
                    firebaseParams[FirebaseAnalytics.Param.VALUE] = it.valueMicros/(1000000.0)
                    firebaseParams[FirebaseAnalytics.Param.CURRENCY] = it.currencyCode
                    firebaseParams[FirebaseAnalytics.Param.AD_FORMAT] = position.desc
                    firebaseParams["precisionType"] = it.precisionType
                    firebaseParams["adNetwork"] = "admob"
                    TrackMgr.instance.trackFireBaseEvent(TrackEventType.Ad_Impression_revenue.tn, firebaseParams)
                    revenueAccumulator(it)
                }
            }
            AdType.INTERSTITIAL.type->{
                val inAd =  adMeta.adInstance as InterstitialAd
                inAd.setOnPaidEventListener {
                    val params = TrackParamBuilder.createAdImpressionParams(it,inAd.responseInfo,inAd.adUnitId, position.desc, adType.type).build()
                    TrackMgr.instance.trackEvent(TrackEventType.AD_IMPRESSION,params )
                    val adRevenueData = AFAdRevenueData(
                        "admob",
                        MediationNetwork.GOOGLE_ADMOB,
                        it.currencyCode,
                        (it.valueMicros/1000000.0)
                    )
                    val afParams = TrackParamBuilder.createAfAdRevenueParams(it,inAd.adUnitId, position.desc, adType.type).build()
                    TrackMgr.instance.trackAppflyEvent(adRevenueData,afParams )
                    val firebaseParams = mutableMapOf<String, Any>()
                    firebaseParams[FirebaseAnalytics.Param.VALUE] = it.valueMicros/(1000000.0)
                    firebaseParams[FirebaseAnalytics.Param.CURRENCY] = it.currencyCode
                    firebaseParams[FirebaseAnalytics.Param.AD_FORMAT] = position.desc
                    firebaseParams["precisionType"] = it.precisionType
                    firebaseParams["adNetwork"] = "admob"
                    TrackMgr.instance.trackFireBaseEvent(TrackEventType.Ad_Impression_revenue.tn, firebaseParams)
                    revenueAccumulator(it)
                }
            }
            AdType.APP_OPEN.type->{
                val openAd =  adMeta.adInstance as AppOpenAd
                openAd.setOnPaidEventListener {
                    val params = TrackParamBuilder.createAdImpressionParams(it,openAd.responseInfo,openAd.adUnitId, position.desc, adType.type).build()
                    TrackMgr.instance.trackEvent(TrackEventType.AD_IMPRESSION,params )
                    val adRevenueData = AFAdRevenueData(
                        "admob",
                        MediationNetwork.GOOGLE_ADMOB,
                        it.currencyCode,
                        (it.valueMicros/1000000.0)
                    )
                    val afParams = TrackParamBuilder.createAfAdRevenueParams(it,openAd.adUnitId, position.desc, adType.type).build()
                    TrackMgr.instance.trackAppflyEvent(adRevenueData,afParams )
                    val firebaseParams = mutableMapOf<String, Any>()
                    firebaseParams[FirebaseAnalytics.Param.VALUE] = it.valueMicros/(1000000.0)
                    firebaseParams[FirebaseAnalytics.Param.CURRENCY] = it.currencyCode
                    firebaseParams[FirebaseAnalytics.Param.AD_FORMAT] = position.desc
                    firebaseParams["precisionType"] = it.precisionType
                    firebaseParams["adNetwork"] = "admob"
                    TrackMgr.instance.trackFireBaseEvent(TrackEventType.Ad_Impression_revenue.tn, firebaseParams)
                    revenueAccumulator(it)
                }
            }
        }
        try {
            markAdShowing(position, adType)

            val wrappedDismissCallback = { pos: AdPosition, type: AdType ->
                onAdDismissed(pos, type)
                CoroutineScope(Dispatchers.IO).launch {
                    unmarkAdShowing(pos, type)
                }
                Unit
            }
            val onWrappedShowResult = { pos: AdPosition, type: AdType, success: Boolean, error: AdError? ->
                onShowResult(pos, type, success, error)
                if (!success){
                    CoroutineScope(Dispatchers.IO).launch {
                        unmarkAdShowing(pos, type)
                    }
                }
                Unit
            }
            TrackMgr.instance.trackAdEvent(position, adType, TrackEventType.safedddd_bh)
            val showStrategy = AdShowStrategyFactory.getStrategy(adType)
            showStrategy.show(
                adInstance = adMeta.adInstance,
                position = position,
                adType = adType,
                activity = activity,
                keepCache = keepCache,
                showCallback = onWrappedShowResult,
                clickCallback = onAdClicked,
                impressionCallback = onAdImpression,
                dismissCallback = wrappedDismissCallback,
                adCount = adCount
            )
        } catch (e: Exception) {
            val error = AdError(-8, "Display anomalies", "${position.name}-${adType.name}Abnormal advertising display：${e.message}")
            onShowResult(position, adType, false, error)
            unmarkAdShowing(position, adType)
        }

        if (!keepCache) {
            loadedAdCache.remove(key)
            loadStateCache[key] = AdLoadState.UNLOADED
            if (adType.type == AdType.NATIVE.type){
                unmarkAdShowing(position, adType)
            }
        }
    }
    private suspend fun awaitAdLoaded(
        position: AdPosition,
        adType: AdType,
        context: Context,
        timeoutMs: Long = 10000
    ): Boolean {
        val startTime = System.currentTimeMillis()
        coroutineScope {
            launch {
                preloadAd(position, adType, context)
            }
        }
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val currentState = getAdLoadState(position, adType)
            when (currentState) {
                AdLoadState.LOADED -> return true
                AdLoadState.FAILED -> return false
                else -> delay(200)
            }
        }
        return false
    }
    fun destroyAd(position: AdPosition, adType: AdType) {
        val key = Pair(position, adType)
        loadedAdCache[key]?.let { AdShowStrategyFactory.getStrategy(adType).destroy(it) }
        loadedAdCache.remove(key)
        loadStateCache[key] = AdLoadState.UNLOADED
        loadLocks.remove(key)
    }

    fun clearAllAdCache() {
        loadedAdCache.forEach { (key, adInstance) ->
            AdShowStrategyFactory.getStrategy(key.second).destroy(adInstance)
        }
        loadedAdCache.clear()
        loadStateCache.clear()
        loadLocks.clear()
    }
    private fun revenueAccumulator(it: AdValue){
        val singleRevenue = BigDecimal.valueOf(3000)
            .divide(BigDecimal.valueOf(1_000_000), decimalPlaces, BigDecimal.ROUND_DOWN)
        val saveDecimal = AppCache.totalRv
        if (saveDecimal.isNotEmpty()){
            acRevenue = BigDecimal.valueOf(saveDecimal.toDouble())
        }
        acRevenue = acRevenue.add(singleRevenue)
        acRevenue = acRevenue.setScale(decimalPlaces, BigDecimal.ROUND_DOWN)
        val appinsTime = App.getAppContext().packageManager.getPackageInfo(App.getAppContext().packageName, 0).firstInstallTime
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - appinsTime
        val currencyCode = it.currencyCode
        if (acRevenue > MIN_REPORT_VALUE){
            val currencyCode = it.currencyCode
            val params = mutableMapOf<String, Any>()
            params[FirebaseAnalytics.Param.VALUE] = acRevenue
            params[FirebaseAnalytics.Param.CURRENCY] = currencyCode
            TrackMgr.instance.trackFireBaseEvent(TrackEventType.Total_ADS_REVENUE_001.tn, params)
            TrackMgr.instance.trackFbPurchaseEvent(TrackEventType.FB_PURCHASE, Currency.getInstance(currencyCode), acRevenue)
            if (timeDifference <= (adConfig?.installHour?:0)*(60*60*1000)) {
                TrackMgr.instance.trackFireBaseEvent(TrackEventType.Total_ADS_REVENUEXIN_001.tn, params)
            }
            acRevenue = BigDecimal.ZERO
        }
        AppCache.totalRv = acRevenue.toString()
        val fbParams = mutableMapOf<String, Any>()
        fbParams[FirebaseAnalytics.Param.VALUE ] = acRevenue
        fbParams[AppEventsConstants.EVENT_PARAM_CURRENCY] = currencyCode
        TrackMgr.instance.trackFbPurchaseEvent(TrackEventType.FB_AD_IMPRESSION, Currency.getInstance(currencyCode), BigDecimal.valueOf(it.valueMicros),fbParams)

    }
}