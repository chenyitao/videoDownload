package com.download.video_download.base.config.sensor
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.utils.AsyncPostRequest
import com.download.video_download.base.utils.MermLruCache
import kotlinx.coroutines.*
import kotlin.coroutines.resume


/**
 * 埋点API服务
 */
class TrackApiService private constructor() {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun reportEvent(eventName: String, params: Map<String, Any>, retryCount: Int = 0) {
        coroutineScope.launch {
            val result = runCatching {
                val host = App.getAppContext().getString(R.string.tba_host_test)
                val url = "${host}${TrackConfig.TRACK_API_PATH}"
                val body = params.toString()
                AsyncPostRequest.sendPost(url,body,35*1000,
                    onSuccess = {

                    },
                    onFailure = {

                    })
            }
            result.onSuccess {
            }.onFailure { e ->
                handleReportFailure(eventName, params, retryCount)
            }
        }
    }

    suspend fun batchReportEvents(events: List<Map<String, Any>>): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val result = runCatching {
                val host = App.getAppContext().getString(R.string.tba_host_test)
                val url = "${host}${TrackConfig.TRACK_API_PATH}"
                val body = events.toString()

                AsyncPostRequest.sendPost(
                    url = url,
                    bodyParams = body,
                    timeoutMs = 35 * 1000,
                    onSuccess = {
                        continuation.resume(true)
                    },
                    onFailure = {
                        continuation.resume(false)
                    }
                )
            }.onFailure { e ->
                continuation.resume(false)
            }
        }
    }

    private suspend fun handleReportFailure(
        eventName: String,
        params: Map<String, Any>,
        retryCount: Int,
    ) {
        if (retryCount < TrackConfig.MAX_RETRY_COUNT) {
            delay(TrackConfig.RETRY_DELAY_MS)
            reportEvent(eventName, params, retryCount + 1)
        } else {
            MermLruCache.instance.put(eventName, params)
        }
    }
    fun cancelAllRequests() {
        coroutineScope.cancel()
    }

    companion object {
        val instance: TrackApiService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TrackApiService()
        }
    }
}