package com.download.video_download.base.config.sensor

import android.content.Context
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.base.utils.MermLruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BTrackMgr  private constructor() {
    private var periodicReportJob: Job? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val apiService = TrackApiService.instance

    fun startTbaBatchReport(context: Context) {
        if (periodicReportJob?.isActive == true) {
            return
        }
        periodicReportJob = coroutineScope.launch {
            while (isActive) {
                try {
                    LogUtils.d("Track","批量上报轮训...")
                    performBatchReport(context.applicationContext)
                    delay(TrackConfig.REPORT_INTERVAL_MS)
                } catch (e: Exception) {
                    delay(TrackConfig.REPORT_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * 停止定时上报
     */
    fun stopTimedReport() {
        periodicReportJob?.cancel()
        periodicReportJob = null
        MermLruCache.instance.clear()
    }

    /**
     * 执行批量上报
     */
    private suspend fun performBatchReport(context: Context) {
        try {
            val memoryCache = MermLruCache.instance.getTrackMemoryCache()
            if (memoryCache.size() == 0) {
                return
            }

            val batchKeys = memoryCache.snapshot().keys.take(TrackConfig.MAX_BATCH_COUNT)
            val batchValues = batchKeys.mapNotNull { key -> memoryCache.get(key) as? Map<String, Any> }
            if (batchValues.isEmpty()) {
                return
            }
            val reportSuccess = apiService.batchReportEvents(batchValues)
            if (reportSuccess) {
                batchKeys.forEach { key -> memoryCache.remove(key) }
            }
        } catch (e: Exception) {
        }
    }


    companion object {
        val instance: BTrackMgr by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BTrackMgr()
        }
    }
}