package com.download.video_download.base.config.sensor
import kotlinx.coroutines.*


/**
 * 埋点API服务 - 单例模式，统一管理网络请求
 */
class TrackApiService private constructor() {
    // 使用SupervisorJob确保一个请求失败不影响其他请求
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * 上报单个事件
     */
    fun reportEvent(eventName: String, params: Map<String, Any>, retryCount: Int = 0) {
        coroutineScope.launch {
            val result = runCatching {
                Post<String>("${Api.HOST}${TrackConfig.TRACK_API_PATH}") {
                    body = gson.toJson(params).toRequestBody(mediaType)
                }.await()
            }

            result.onSuccess {
                Timber.d("埋点上报成功: event=$eventName, params=$params")
            }.onFailure { e ->
                Timber.e(e, "埋点上报失败: event=$eventName")
                handleReportFailure(eventName, params, retryCount, e)
            }
        }
    }

    /**
     * 批量上报事件
     */
    suspend fun batchReportEvents(events: List<Map<String, Any>>): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = Post<String>("${Api.HOST}${TrackConfig.TRACK_API_PATH}") {
                body = gson.toJson(events).toRequestBody(mediaType)
            }.await()
            Timber.d("批量埋点上报成功，数量: ${events.size}")
            result.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "批量埋点上报失败")
            false
        }
    }

    /**
     * 处理上报失败逻辑
     */
    private suspend fun handleReportFailure(
        eventName: String,
        params: Map<String, Any>,
        retryCount: Int,
        exception: Exception
    ) {
        if (retryCount < TrackConfig.MAX_RETRY_COUNT) {
            Timber.d("埋点上报重试，次数: ${retryCount + 1}")
            delay(TrackConfig.RETRY_DELAY_MS)
            reportEvent(eventName, params, retryCount + 1)
        } else {
            Timber.d("埋点上报达到最大重试次数，缓存到本地: $eventName")
            AppLruCache.instance.put(eventName, params)
        }
    }

    /**
     * 取消所有未完成的请求
     */
    fun cancelAllRequests() {
        coroutineScope.cancel()
    }

    companion object {
        // 双重校验锁单例，保证线程安全
        val instance: TrackApiService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TrackApiService()
        }
    }
}