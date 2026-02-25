package com.download.video_download.base.config.sensor

import android.content.Context
import com.download.video_download.App
import com.download.video_download.base.config.utils.CfUtils
import com.download.video_download.base.utils.AppCache
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

    fun init(context: Context) {
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

        serverStrategy.reportEvent(eventType.tn, finalParams)

        if (shouldReportToFirebase(eventType)) {
            firebaseStrategy.reportEvent(eventType.tn, finalParams)
        }
    }

    /**
     * 上报Facebook购买事件
     */
    fun trackFbPurchaseEvent(currency: Currency, amount: BigDecimal, params: Map<String, Any> = emptyMap()) {
        facebookStrategy.reportPurchaseEvent(currency, amount)
        facebookStrategy.reportEvent(TrackEventType.AD_FB_PURCHASE.name, params)
    }

    /**
     * 构建最终的上报参数
     */
    private fun buildFinalParams(
        eventType: TrackEventType,
        customParams: Map<String, Any>
    ): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        if (shouldReportToFirebase(eventType)) {
            val firebaseMap = mutableMapOf<String, Any>()
            customParams.forEach { (key, value) ->
                firebaseMap[key] = value
            }
            return firebaseMap
        }
        map.putAll(tbaCP)
        map["ambulant"] = UUID.randomUUID().toString()
        map["mcdowell"] = System.currentTimeMillis()
        when (eventType) {
            TrackEventType.SESSION -> {
                map[TrackEventType.SESSION.tn] = TrackParamBuilder.createSessionParams().build()
            }
            TrackEventType.INSTALL ->{
                map[TrackEventType.INSTALL.tn] = customParams
            }
            TrackEventType.AD_IMPRESSION ->{
                map[TrackEventType.AD_IMPRESSION.tn] = customParams
            }
            else -> {
                map["lust"] = eventType.tn
                map["lust"] = customParams
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