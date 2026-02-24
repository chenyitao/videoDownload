package com.download.video_download.base.config.sensor

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BTrackMgr  private constructor() {
    private var isReporting = false
    private var isStarted = false
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val apiService = TrackApiService.instance

    /**
     * 启动定时批量上报
     */
    fun startTimedReport(context: Context) {
        if (isStarted) {
            Timber.d("定时上报已启动，无需重复启动")
            return
        }
        isStarted = true

        coroutineScope.launch {
            while (isStarted) {
                try {
                    performBatchReport(context.applicationContext)
                    delay(TrackConfig.REPORT_INTERVAL_MS)
                } catch (e: Exception) {
                    Timber.e(e, "定时批量上报异常")
                    delay(TrackConfig.REPORT_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * 停止定时上报
     */
    fun stopTimedReport() {
        isStarted = false
        isReporting = false
        coroutineScope.cancel()
        Timber.d("定时上报已停止")
    }

    /**
     * 执行批量上报
     */
    private suspend fun performBatchReport(context: Context) {
        if (isReporting) {
            Timber.d("正在执行批量上报，跳过本次")
            return
        }
        isReporting = true

        try {
            val memoryCache = AppLruCache.instance.getMemoryCache()
            if (memoryCache.size() == 0) {
                Timber.d("缓存中无埋点数据，无需上报")
                return
            }

            // 分批获取要上报的数据
            val batchKeys = memoryCache.snapshot().keys.take(TrackConfig.MAX_BATCH_COUNT)
            val batchValues = batchKeys.mapNotNull { key -> memoryCache.get(key) as? Map<String, Any> }

            if (batchValues.isNotEmpty()) {
                val reportSuccess = apiService.batchReportEvents(batchValues)
                if (reportSuccess) {
                    // 上报成功，移除缓存
                    batchKeys.forEach { key -> memoryCache.remove(key) }
                    Timber.d("批量上报成功，移除缓存数据: ${batchKeys.size}条")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "批量上报执行异常")
        } finally {
            isReporting = false
        }
    }

    /**
     * 手动触发批量上报
     */
    fun triggerManualReport(context: Context) {
        coroutineScope.launch {
            performBatchReport(context.applicationContext)
        }
    }

    companion object {
        val instance: BTrackMgr by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BTrackMgr()
        }
    }
}