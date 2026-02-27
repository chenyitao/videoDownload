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
    Total_ADS_REVENUE_001("Total_Ads_Revenue_001"),
    FB_PURCHASE("fb_purchase"),
    FB_AD_IMPRESSION("Adimpression"),
    Total_ADS_REVENUEXIN_001("Total_Ads_Revenuexin_001"),
    Ad_Impression_revenue("Ad_Impression_revenue"),
    safedddd_ac("safedddd_ac"),
    safedddd_ad("safedddd_ad"),
    safedddd_ae("safedddd_ae"),
    safedddd_af("safedddd_af"),
    safedddd_yx("safedddd_yx"),
    safedddd_yz("safedddd_yz"),
    safedddd_new1("safedddd_new1"),
    safedddd_new2("safedddd_new2"),
    safedddd_new3("safedddd_new3"),
    safedddd_main1("safedddd_main1"),
    safedddd_main2("safedddd_main2"),
    safedddd_main3("safedddd_main3"),
    safedddd_main4("safedddd_main4"),
    safedddd_home1("safedddd_home1"),
    safedddd_home2("safedddd_home2"),
    safedddd_home3("safedddd_home3"),
    safedddd_search1("safedddd_search1"),
    safedddd_search2("safedddd_search2"),
    safedddd_browser1("safedddd_browser1"),
    safedddd_browser2("safedddd_browser2"),
    safedddd_browser3("safedddd_browser3"),
    safedddd_browser6("safedddd_browser6"),
    safedddd_browser7("safedddd_browser7"),
    safedddd_browser8("safedddd_browser8"),
    safedddd_browser9("safedddd_browser9"),
    safedddd_browser10("safedddd_browser10"),
    safedddd_browser11("safedddd_browser11"),
    safedddd_browser12("safedddd_browser12"),
    safedddd_browser13("safedddd_browser13"),
    safedddd_browser14("safedddd_browser14"),
    safedddd_down1("safedddd_down1"),
    safedddd_down2("safedddd_down2"),
    safedddd_down3("safedddd_down3"),
    safedddd_down4("safedddd_down4"),
    safedddd_down5("safedddd_down5"),
    safedddd_down6("safedddd_down6"),
    safedddd_play1("safedddd_play1"),
    safedddd_play2("safedddd_play2")
}
interface TrackReportStrategy {
    fun reportEvent(eventName: String, params: Map<String, Any>)
}