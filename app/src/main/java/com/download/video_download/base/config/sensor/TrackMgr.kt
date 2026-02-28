package com.download.video_download.base.config.sensor

import android.content.Context
import android.util.Log
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AppsFlyerLib
import com.download.video_download.App
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.config.utils.CfUtils
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LogUtils
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.LoadAdError
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class TrackMgr private constructor() {
    private lateinit var tbaCP: Map<String, Any>
    private var distinctID: String = ""
    private val serverStrategy by lazy { ServerTrackStrategy() }
    private val firebaseStrategy by lazy { FirebaseTrackStrategy() }
    private val facebookStrategy by lazy { FacebookTrackStrategy() }
    private val batchManager by lazy { BTrackMgr.instance }
    private var appsflyer: AppsFlyerLib? = null
    private var TAG = "TrackMgr"
    fun init(context: Context) {
        appsflyer = AppsFlyerLib.getInstance()
        tbaCP = TrackParamBuilder.createCommonParams()
            .apply {
                var sawdustId = AppCache.sawdust
                if (sawdustId.isEmpty()) {
                    sawdustId = CfUtils.generateDistinctId(App.getAppContext())
                    AppCache.sawdust = sawdustId
                }
                addParam("sawdust", sawdustId)
                addParam("megavolt", sawdustId + "c")
            }
            .build()

        distinctID = tbaCP["sawdust"] as String
        batchManager.startTbaBatchReport(context)
    }
    fun getDistinctID(): String {
        return distinctID
    }
    fun trackEvent(eventType: TrackEventType, params: Map<String, Any> = emptyMap()) {
        val finalParams = buildFinalParams(eventType, params)
        LogUtils.d(TAG, "trackEvent: ${eventType.tn} trackParams: $finalParams")
        serverStrategy.reportEvent(eventType.tn, finalParams)

        if (shouldReportToFirebase(eventType)) {
            LogUtils.d(TAG, "trackEvent-firebase: ${eventType.tn} trackParams: $params")
            firebaseStrategy.reportEvent(eventType.tn, params)
        }
    }
    fun trackFireBaseEvent(eventName: String, params: Map<String, Any>) {
        LogUtils.d(TAG, "trackEvent-firebase: $eventName trackParams: $params")
        firebaseStrategy.reportEvent(eventName, params)
    }
    fun trackFbPurchaseEvent(eventType:TrackEventType ,currency: Currency, amount: BigDecimal, params: MutableMap<String, Any> = mutableMapOf()) {
        LogUtils.d(TAG, "trackEvent-facebook: ${eventType.tn} trackParams: amount-$amount,currency-$currency params-$params")
        if (eventType == TrackEventType.FB_PURCHASE){
            facebookStrategy.reportPurchaseEvent(currency, amount)
            return
        }
        facebookStrategy.reportEvent(TrackEventType.FB_AD_IMPRESSION.tn, params)
    }

    fun trackAppflyEvent(adRevenueData: AFAdRevenueData, params: Map<String, Any> = emptyMap()) {
        LogUtils.d(TAG, "trackEvent-appflyer  trackParams: ${adRevenueData.toString()} $params")
        appsflyer?.logAdRevenue(adRevenueData, params)
    }
    fun trackAdEvent(adLoc: AdPosition, type: AdType, event: TrackEventType, error: com.download.video_download.base.ad.model.LoadAdError?= null){
        var safedddd = ""
        when(adLoc){
            AdPosition.LOADING -> {
                safedddd = "1"
            }
            AdPosition.LANGUAGE->{
                if (type == AdType.NATIVE){
                    safedddd = "10"
                }else{
                    safedddd = "11"
                }
            }
            AdPosition.GUIDE->{
                if (type == AdType.NATIVE){
                    safedddd = "5"
                }else{
                    safedddd = "6"
                }
            }
            AdPosition.START_DOWNLOAD->{
                safedddd = "3"
            }
            AdPosition.BACK->{
                safedddd = "2"
            }
            AdPosition.TAB->{
                safedddd = "4"
            }
            AdPosition.DOWNLOAD_TASK_DIALOG->{
                if (type == AdType.NATIVE){
                    safedddd = "9"
                }
            }
            AdPosition.HOME->{
                if (type == AdType.NATIVE){
                    safedddd = "7"
                }else{
                    safedddd = "12"
                }
            }
            AdPosition.SEARCH->{
                if (type == AdType.NATIVE){
                    safedddd = "8"
                }
            }
            else->{

            }
        }
        if (event == TrackEventType.safedddd_sbb){
            val code:String? =  error?.code?.toString()
            val message:String =  error?.message.toString()
            trackEvent(event, mutableMapOf("safedddd" to safedddd,"safedddd4" to code.toString(),"safedddd5" to message))
        }else{
            if (event == TrackEventType.safedddd_bg){
                val safedddd1 = if (AdMgr.INSTANCE.hasCachedAd(adLoc, type)) 1 else 2
                val safedddd2 = if (AdMgr.INSTANCE.isDailyLimitReached()) if (AdMgr.INSTANCE.isClickLimitReached()) 1 else 2 else 3
                trackEvent(event, mutableMapOf("safedddd" to safedddd,"safedddd1" to safedddd1,"safedddd2" to safedddd2))
            }else{
                trackEvent(event, mutableMapOf("safedddd" to safedddd))
            }
        }
    }
    private fun buildFinalParams(
        eventType: TrackEventType,
        customParams: Map<String, Any>
    ): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map.putAll(tbaCP)
        map["ambulant"] = UUID.randomUUID().toString()
        map["mcdowell"] = System.currentTimeMillis()
        when (eventType) {
            TrackEventType.SESSION -> {
                map[TrackEventType.SESSION.tn] = "vine"
            }
            TrackEventType.INSTALL ->{
                map[TrackEventType.INSTALL.tn] = customParams
            }
            TrackEventType.AD_IMPRESSION ->{
                map[TrackEventType.AD_IMPRESSION.tn] = customParams
            }
            else -> {
                map["lust"] = eventType.tn
                map[eventType.tn] = customParams
            }
        }
        return map
    }

    private fun shouldReportToFirebase(eventType: TrackEventType): Boolean {
        return when (eventType) {
            TrackEventType.FIRST_OPEN,
            TrackEventType.SESSION,
            TrackEventType.INSTALL,
            TrackEventType.AD_IMPRESSION,
            TrackEventType.SESSION_START -> false
            else -> true
        }
    }

    /**
     * 销毁埋点管理器
     */
    fun destroy() {
        batchManager.stopTimedReport()
        TrackApiService.instance.cancelAllRequests()
    }

    companion object {
        // 单例实现
        val instance: TrackMgr by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TrackMgr()
        }
    }
}