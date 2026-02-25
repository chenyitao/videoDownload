package com.download.video_download.base.config.sensor

object TrackConfig {
    const val REPORT_INTERVAL_MS = 95_000L
    const val MAX_BATCH_COUNT = 39
    const val MAX_RETRY_COUNT = 1
    const val RETRY_DELAY_MS = 41_000L
    const val TRACK_API_PATH = "/bedroom/fount/plankton"
}
enum class TrackEventType(val tn: String) {
    SESSION("lust"),
    INSTALL("reave"),
    AD_IMPRESSION("escapee"),
    FIRST_OPEN("first_open"),
    SESSION_START("session_start"),
    AD_FB_PURCHASE("Ad_fb_purchase"),
    CUSTOM_EVENT("")
}
interface TrackReportStrategy {
    fun reportEvent(eventName: String, params: Map<String, Any>)
}