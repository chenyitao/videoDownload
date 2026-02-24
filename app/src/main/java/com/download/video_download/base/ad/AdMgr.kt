package com.download.video_download.base.ad

import android.app.Activity
import android.content.Context
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
import com.download.video_download.base.ad.model.AdCacheMeta
import com.download.video_download.base.ad.model.AdConfig
import com.download.video_download.base.ad.model.AdCount
import com.download.video_download.base.ad.model.AdData
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdManageData
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ad.model.Advert
import com.download.video_download.base.ad.model.Config
import com.download.video_download.base.ad.model.LoadAdError
import com.download.video_download.base.ad.strategy.AdLoadStrategyFactory
import com.download.video_download.base.ad.strategy.AdShowStrategyFactory
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.TimeFormateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private var advert: Advert? = null
    private var adConfig: AdConfig? = null
    private val loadedAdCache = mutableMapOf<Pair<AdPosition, AdType>, AdCacheMeta>()
    private val loadStateCache = ConcurrentHashMap<Pair<AdPosition, AdType>, AdLoadState>()
    private val loadLocks = ConcurrentHashMap<Pair<AdPosition, AdType>, Mutex>()
    private var isPrivacyConsentGiven = false
    private var adCount: AdCount? =  null
    fun initAdData() {
        val remoteAdConfig = AppCache.adcf
        remoteAdConfig.takeIf { it.isNotEmpty() }?.let { config ->
            val adModel = App.getAppContext().jsonParser().decodeFromString<Config>(config)
            adModel.advertConfig.let { ad ->
                advert = ad.advert
                adConfig = ad.config
            }
        }
        initAdLimitTime()
        val advertMap = mutableMapOf<AdPosition, List<AdData>>()
        advertMap[AdPosition.LOADING] =  advert?.loading ?: emptyList()
        advertMap[AdPosition.LANGUAGE] = advert?.language ?: emptyList()
        advertMap[AdPosition.GUIDE] = advert?.guide ?: emptyList()
        advertMap[AdPosition.HOME] = advert?.home ?: emptyList()
        advertMap[AdPosition.SEARCH] = advert?.search ?: emptyList()
        advertMap[AdPosition.START_DOWNLOAD] = advert?.startDownload ?: emptyList()
        advertMap[AdPosition.BACK] = advert?.back ?: emptyList()
        advertMap[AdPosition.TAB] = advert?.tab ?: emptyList()
        advertMap[AdPosition.DOWNLOAD_TASK_DIALOG] = advert?.downloadTaskDialog ?: emptyList()
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
        return loadedAdCache[Pair(position, AdType.NATIVE)] as? NativeAd
    }
    private fun getLoadLock(position: AdPosition, adType: AdType): Mutex {
        val key = Pair(position, adType)
        return loadLocks.getOrPut(key) { Mutex() }
    }
    private fun isDailyLimitReached(): Boolean {
        val config = adConfig ?: return true
        val curTodayShowCount = adCount?.todayShowCount ?:0
        val curTodayClickCount = adCount?.todayClickCount ?:0
        return curTodayShowCount >= config.todayShowCount || curTodayClickCount >= config.todayClickCount
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
            val error = LoadAdError(-1, "隐私未同意", "UMP隐私政策未同意，无法加载广告")
            onLoadStateChanged(position, adType, AdLoadState.FAILED, error)
            return
        }
        if (isDailyLimitReached()) {
            val error = LoadAdError(-2, "每日限制", "已达到每日广告展示/点击上限")
            onLoadStateChanged(position, adType, AdLoadState.FAILED, error)
            return
        }
        val adUnitList = adManageData?.advert?.get(position)?.filter { it.adType == adType.type } ?: emptyList()
        if (adUnitList.isEmpty()) {
            val error = LoadAdError(-3, "无广告数据", "未找到${position.name}-${adType.name}类型的广告单元")
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
            onLoadStateChanged(position, adType, currentState, null)
            return
        }

        getLoadLock(position, adType).withLock {
            val latestState = loadStateCache[key] ?: AdLoadState.UNLOADED
            if (latestState == AdLoadState.LOADED || latestState == AdLoadState.LOADING) {
                onLoadStateChanged(position, adType, latestState, null)
                return@withLock
            }

            loadStateCache[key] = AdLoadState.LOADING
            onLoadStateChanged(position, adType, AdLoadState.LOADING, null)
            var loadSuccess = false
            var loadedMeta: AdCacheMeta? = null
            var adError: LoadAdError? = null
            for (adUnit in adUnitList) {
                val loadStrategy = AdLoadStrategyFactory.getStrategy(adType)
                val (success, adInstance) = if (adType.type == AdType.NATIVE.type) loadStrategy.load(context, adUnit.ad_unit_id?:"",adCount,position,adType) else loadStrategy.load(context, adUnit.ad_unit_id?:"")
                if (success && adInstance != null) {
                    loadedMeta = AdCacheMeta(
                        adInstance = adInstance,
                        expireTime = adUnit.expired,
                        cacheTime = System.currentTimeMillis(),
                        adId = adUnit.ad_unit_id?:""
                    )
                    loadedAdCache[key] = loadedMeta
                    loadSuccess = true
                    break
                }else{
                    adError = adInstance as LoadAdError? as LoadAdError?
                }
            }

            if (loadSuccess) {
                loadStateCache[key] = AdLoadState.LOADED
                onLoadStateChanged(position, adType, AdLoadState.LOADED, null)
            } else {
                loadStateCache[key] = AdLoadState.FAILED
                val error = LoadAdError(adError?.code?:-1, adError?.message?:"", "${position.name}-${adType.name}所有广告单元加载失败")
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
            val error = AdError(-7, "广告展示中", "${position.name}-${adType.name}广告正在展示，无法重复调用")
            onShowResult(position, adType, false, error)
            return
        }
        // 前置检查
        if (!isPrivacyConsentGiven) {
            val error = AdError(-1, "隐私未同意", "UMP隐私政策未同意，无法展示广告")
            onShowResult(position, adType, false, error)
            return
        }
        if (isDailyLimitReached()) {
            val error = AdError(-2, "每日限制", "已达到每日广告展示/点击上限")
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
                val error = AdError(-3, "广告未就绪", "${position.name}-${adType.name}广告加载未完成/超时")
                onShowResult(position, adType, false, error)
                return
            }
            currentState = getAdLoadState(position, adType)
        }

        if (currentState == AdLoadState.LOADING) {
            val loadSuccess = awaitAdLoaded(position, adType, activity, timeoutMs = 10000)
            if (!loadSuccess) {
                val error = AdError(-4, "加载中", "${position.name}-${adType.name}广告加载超时")
                onShowResult(position, adType, false, error)
                return
            }
            currentState = getAdLoadState(position, adType)
        }
        val adMeta = loadedAdCache[key] ?: run {
            val error = AdError(-5, "无缓存", "${position.name}-${adType.name}广告实例缓存不存在")
            onShowResult(position, adType, false, error)
            return
        }
        if (isAdExpired(adMeta)) {
            val error = AdError(-6, "广告过期", "${position.name}-${adType.name}广告已失效，请重新加载")
            onShowResult(position, adType, false, error)
            return
        }
        when(adType.type){
            AdType.NATIVE.type->{
                val nativeAd =  adMeta.adInstance as NativeAd
                nativeAd.setOnPaidEventListener {
                }
            }
            AdType.INTERSTITIAL.type->{
                val inAd =  adMeta.adInstance as InterstitialAd
                inAd.setOnPaidEventListener {
                }
            }
            AdType.APP_OPEN.type->{
                val openAd =  adMeta.adInstance as AppOpenAd
                openAd.setOnPaidEventListener {
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
            val error = AdError(-8, "展示异常", "${position.name}-${adType.name}广告展示异常：${e.message}")
            onShowResult(position, adType, false, error)
            unmarkAdShowing(position, adType)
        }

        if (!keepCache) {
            loadedAdCache.remove(key)
            loadStateCache[key] = AdLoadState.UNLOADED
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
}