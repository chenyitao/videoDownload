package com.download.video_download.base.config.sensor

object TrackConfig {
    // 批量上报间隔时间(毫秒)
    const val REPORT_INTERVAL_MS = 60_000L
    // 单次最大批量上报数量
    const val MAX_BATCH_COUNT = 20
    // 最大重试次数
    const val MAX_RETRY_COUNT = 1
    // 重试延迟时间(毫秒)
    const val RETRY_DELAY_MS = 41_000L
    // 埋点API路径
    const val TRACK_API_PATH = ""
}
enum class TrackEventType {
    EVENT_SESSION,
    EVENT_INSTALL,
    EVENT_AD_IMPRESSION,
    EVENT_FIRST_OPEN,
    EVENT_SESSION_START,
    AD_FB_PURCHASE,
    AD_IMPRESSION,
    CUSTOM_EVENT
}
interface TrackReportStrategy {
    fun reportEvent(eventName: String, params: Map<String, Any>)
}