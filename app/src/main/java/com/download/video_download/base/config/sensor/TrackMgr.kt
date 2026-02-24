package com.download.video_download.base.config.sensor

import android.content.Context

class TrackMgr private constructor() {
    private lateinit var commonParams: Map<String, Any>
    private var distinctID: String = ""

    // 各种上报策略
    private val serverStrategy by lazy { ServerTrackStrategy() }
    private val firebaseStrategy by lazy { FirebaseTrackStrategy() }
    private val facebookStrategy by lazy { FacebookTrackStrategy() }
    private val batchManager by lazy { BTrackMgr.instance }

    /**
     * 初始化埋点管理器
     */
    fun init(context: Context) {
        // 构建通用参数
        commonParams = TrackParamBuilder.createCommonParams()
            .apply {
                // 初始化唯一标识
                var rimeId = SpUtils.getString("rime", "")
                if (rimeId.isEmpty()) {
                    rimeId = DeviceUtils.generateRandomString(App.getAppContext())
                    SpUtils.putString("rime", rimeId)
                }
                addParam("rime", rimeId)
                addParam("gresham", "$rimeId" + "a")
            }
            .build()

        distinctID = commonParams["rime"] as String

        // 启动批量上报
        batchManager.startTimedReport(context)

        Timber.d("埋点管理器初始化完成，distinctID=$distinctID")
    }

    /**
     * 上报埋点事件
     */
    fun trackEvent(eventType: TrackEventType, eventName: String, params: Map<String, Any> = emptyMap()) {
        // 构建最终上报参数
        val finalParams = buildFinalParams(eventType, eventName, params)

        // 1. 上报到服务器
        serverStrategy.reportEvent(eventName, finalParams)

        // 2. 根据事件类型决定是否上报到Firebase
        if (shouldReportToFirebase(eventType)) {
            val firebaseParams = filterFirebaseParams(params)
            firebaseStrategy.reportEvent(eventName, firebaseParams)
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
        eventName: String,
        customParams: Map<String, Any>
    ): Map<String, Any> {
        return TrackParamBuilder.createCommonParams()
            .addParams(commonParams) // 添加通用参数
            .addParam("ugh", java.util.UUID.randomUUID().toString()) // 每次上报生成新的uuid
            .addParam("tito", System.currentTimeMillis()) // 上报时间戳
            .apply {
                // 根据事件类型添加特定参数
                when (eventType) {
                    TrackEventType.EVENT_SESSION -> addParam(TrackEventType.EVENT_SESSION.name, TrackParamBuilder.createSessionParams().build())
                    TrackEventType.EVENT_INSTALL -> addParam(TrackEventType.EVENT_INSTALL.name, customParams)
                    TrackEventType.EVENT_AD_IMPRESSION -> addParam(TrackEventType.EVENT_AD_IMPRESSION.name, customParams)
                    else -> {
                        addParam("oldster", eventName)
                        // 处理自定义参数
                        customParams.forEach { (key, value) ->
                            addParam("$key%clinch", value)
                        }
                    }
                }
            }
            .build()
    }

    /**
     * 判断是否需要上报到Firebase
     */
    private fun shouldReportToFirebase(eventType: TrackEventType): Boolean {
        return when (eventType) {
            TrackEventType.EVENT_FIRST_OPEN,
            TrackEventType.EVENT_SESSION_START -> false
            else -> true
        }
    }

    /**
     * 过滤Firebase上报参数
     */
    private fun filterFirebaseParams(params: Map<String, Any>): Map<String, Any> {
        // 可以在这里过滤掉Firebase不支持的参数
        return params.filter { (_, value) ->
            when (value) {
                is String, is Int, is Long, is Float, is Double, is Boolean -> true
                else -> false
            }
        }
    }

    /**
     * 手动触发批量上报
     */
    fun triggerManualBatchReport(context: Context) {
        batchManager.triggerManualReport(context)
    }

    /**
     * 销毁埋点管理器
     */
    fun destroy() {
        batchManager.stopTimedReport()
        TrackApiService.instance.cancelAllRequests()
        Timber.d("埋点管理器已销毁")
    }

    companion object {
        // 单例实现
        val instance: TrackMgr by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TrackMgr()
        }
    }
}