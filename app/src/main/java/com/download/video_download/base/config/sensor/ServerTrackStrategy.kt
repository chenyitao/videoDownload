package com.download.video_download.base.config.sensor

/**
 * 服务器上报策略
 */
class ServerTrackStrategy : TrackReportStrategy {
    private val apiService by lazy { TrackApiService.instance }

    override fun reportEvent(eventName: String, params: Map<String, Any>) {
        apiService.reportEvent(eventName, params)
    }
}