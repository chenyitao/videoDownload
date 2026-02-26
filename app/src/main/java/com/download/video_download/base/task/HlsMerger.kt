package com.download.video_download.base.task
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.HandlerThread
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DefaultDownloaderFactory
import androidx.media3.exoplayer.hls.offline.HlsDownloader
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.download.video_download.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@UnstableApi
object HlsMerger {
    private const val TAG = "HlsMerger"
    private const val PROGRESS_POLL_INTERVAL = 500L

    private var transformProgressJob: Job? = null
    private var progressPollJob: Job? = null

    private val hlsCacheDir by lazy { File(App.getAppContext().getExternalFilesDir(null), "hls_cache") }
    private val mergeHlsOutputDir by lazy {
        File(App.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "")
            .apply { mkdirs() }
    }
    private lateinit var cache: Cache
    private var transformer: Transformer? = null
    private lateinit var downloadManager: DownloadManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressHandlerThread by lazy {
        HandlerThread("TransformProgressThread").apply { start() }
    }
    private val databaseProvider by lazy { StandaloneDatabaseProvider(App.getAppContext()) }

    fun init() {
        initCache()
        initDownloadManager()
    }

    private fun initCache() {
        cache = SimpleCache(hlsCacheDir, NoOpCacheEvictor(), databaseProvider)
    }

    private fun initDownloadManager() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Media3-HLS-Downloader")
            .setAllowCrossProtocolRedirects(true)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val downloadExecutor = java.util.concurrent.Executors.newFixedThreadPool(3)
        val downloaderFactory = createDownloaderFactory(cacheDataSourceFactory, downloadExecutor)
        val downloadIndex = DefaultDownloadIndex(databaseProvider)

        downloadManager = DownloadManager(
            App.getAppContext(),
            downloadIndex,
            downloaderFactory
        ).apply {
            maxParallelDownloads = DownloadManager.DEFAULT_MAX_PARALLEL_DOWNLOADS
            minRetryCount = DownloadManager.DEFAULT_MIN_RETRY_COUNT

            addListener(createDownloadManagerListener())
            resumeDownloads()
        }
    }
    private fun createDownloaderFactory(
        cacheDataSourceFactory: CacheDataSource.Factory,
        downloadExecutor: java.util.concurrent.Executor
    ): DefaultDownloaderFactory {
        return object : DefaultDownloaderFactory(cacheDataSourceFactory, downloadExecutor) {
            override fun createDownloader(request: DownloadRequest): androidx.media3.exoplayer.offline.Downloader {
                val mediaItem = MediaItem.Builder()
                    .setUri(request.uri)
                    .setMediaId(request.id)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()

                return HlsDownloader.Factory(cacheDataSourceFactory)
                    .setExecutor(downloadExecutor)
                    .create(mediaItem)
            }
        }
    }
    private fun createDownloadManagerListener(): DownloadManager.Listener {
        return object : DownloadManager.Listener {
            override fun onDownloadChanged(
                manager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                when (download.state) {
                    Download.STATE_DOWNLOADING -> {
                    }
                    Download.STATE_COMPLETED -> {
                        convertHlsToMp4(download.request.id, download.request.uri)
                    }
                    Download.STATE_FAILED -> {
                    }
                    Download.STATE_QUEUED -> {
                    }
                    else -> {}
                }
            }
        }
    }
    fun startDownload(m3u8Url: String, mediaId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = DownloadRequest.Builder(mediaId, Uri.parse(m3u8Url))
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()

                downloadManager.addDownload(request)
                startProgressPolling(mediaId)
            } catch (e: Exception) {
                Log.e(TAG, "Start download failed: ${e.message}", e)
            }
        }
    }

    fun cancelDownload(mediaId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            downloadManager.removeDownload(mediaId)
            transformProgressJob?.cancel()
            releaseTransformer()
        }
    }

    private fun startProgressPolling(mediaId: String) {
        stopProgressPolling()
        progressPollJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(PROGRESS_POLL_INTERVAL)
                val download = downloadManager.downloadIndex.getDownload(mediaId)
                if (download != null && download.state == Download.STATE_DOWNLOADING) {
                    calculateDownloadProgress(download) // 保留计算逻辑，移除回调
                }
            }
        }
    }


    private fun stopProgressPolling() {
        progressPollJob?.cancel()
        progressPollJob = null
    }

    private fun calculateDownloadProgress(download: Download): Int {
        return if (download.contentLength == C.LENGTH_UNSET.toLong() || download.contentLength <= 0L) {
            0
        } else {
            val progress = (download.bytesDownloaded * 100 / download.contentLength).toInt()
            progress.coerceIn(0, 100)
        }
    }

    private fun convertHlsToMp4(mediaId: String, hlsUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val outputFile = File(mergeHlsOutputDir, "$mediaId.mp4")
                val outputPath = outputFile.absolutePath

                val mediaItem = MediaItem.Builder()
                    .setUri(hlsUri)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()

                val transformerBuilder = Transformer.Builder(App.getAppContext())
                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .setLooper(mainHandler.looper)
                    .setPortraitEncodingEnabled(false)
                    .setEnsureFileStartsOnVideoFrameEnabled(false)

                transformer = transformerBuilder.build().apply {
                    this@apply.addListener(createTransformerListener(mediaId, outputPath))
                    start(mediaItem, outputPath)
                    startTransformProgressListening(mediaId)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Convert HLS to MP4 failed: ${e.message}", e)
                releaseTransformer()
            }
        }
    }
    private fun createTransformerListener(mediaId: String, outputPath: String): Transformer.Listener {
        return object : Transformer.Listener {
            override fun onCompleted(
                composition: androidx.media3.transformer.Composition,
                exportResult: ExportResult
            ) {
                transformProgressJob?.cancel()
                releaseTransformer()
            }

            override fun onError(
                composition: androidx.media3.transformer.Composition,
                exportResult: ExportResult,
                exportException: ExportException
            ) {
                transformProgressJob?.cancel()
                Log.e(TAG, "Transformer error: ${exportException.message}", exportException)
                releaseTransformer()
            }

            override fun onFallbackApplied(
                composition: androidx.media3.transformer.Composition,
                originalTransformationRequest: androidx.media3.transformer.TransformationRequest,
                fallbackTransformationRequest: androidx.media3.transformer.TransformationRequest
            ) {
            }
        }
    }
    private fun startTransformProgressListening(mediaId: String) {
        transformProgressJob = GlobalScope.launch(Dispatchers.IO) {
            val progressHolder = ProgressHolder()
            while (true) {
                delay(PROGRESS_POLL_INTERVAL)
                val transformer = transformer ?: break

                try {
                    val progressState = transformer.getProgress(progressHolder)
                    when (progressState) {
                        Transformer.PROGRESS_STATE_AVAILABLE -> progressHolder.progress / 100f
                        Transformer.PROGRESS_STATE_WAITING_FOR_AVAILABILITY -> 0f
                        Transformer.PROGRESS_STATE_UNAVAILABLE -> -1f
                        Transformer.PROGRESS_STATE_NOT_STARTED -> 0f
                        else -> 0f
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun releaseTransformer() {
        transformer?.let {
            try {
                it.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Release transformer failed", e)
            }
            it.removeAllListeners()
            transformer = null
        }
    }
    fun release() {
        progressHandlerThread.quitSafely()
        releaseTransformer()
        if (::downloadManager.isInitialized) {
            downloadManager.release()
        }

        transformProgressJob?.cancel()
        stopProgressPolling()
    }
}