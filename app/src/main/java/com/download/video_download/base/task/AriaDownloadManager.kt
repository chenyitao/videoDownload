package com.download.video_download.base.task

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.task.DownloadTask
import com.download.video_download.App
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.room.entity.getSequentialNumber
import com.download.video_download.base.utils.AppCache
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class AriaDownloadManager() : DownloadTaskListener {
    private var internalDir = ""
    private val _videoItems: MutableLiveData<MutableList<Video>> = MutableLiveData(mutableListOf())
    val videoItems: MutableLiveData<MutableList<Video>> get() = _videoItems
    val m3u8SizeList = mutableMapOf<String, Long>()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val isUpdating = AtomicBoolean(false)
    private val _isCompete: MutableLiveData<Boolean> = MutableLiveData(false)
    val isCompete: MutableLiveData<Boolean> get() = _isCompete
    companion object {
        val INSTANCE: AriaDownloadManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AriaDownloadManager()
        }
    }
    init {
        Aria.download(this).register()
        internalDir = File(App.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "").absolutePath
    }
    fun resetCompete(isCompete: Boolean){
        _isCompete.value = isCompete
    }
    fun startResumeDownloadTask(videoItem: MutableList<Video> = mutableListOf()) {
        m3u8SizeList.clear()
        val playVideos = getPlayList()
        val taskList = AppCache.downloadTask

        val currentList = _videoItems.value ?: mutableListOf()

        taskList.takeIf { it.isNotEmpty() }?.let {
            val cacheTask = try {
                Json.decodeFromString<MutableList<Video>>(taskList)
            } catch (e: Exception) {
                mutableListOf()
            }
            if (cacheTask.isNotEmpty()) {
                currentList.addAll(cacheTask)
            }
        }

        // 3. 添加新任务
        currentList.addAll(videoItem)

        // 4. 去重处理
        val newVideos = deduplicateVideoItems(currentList)

        // 5. 处理重复文件名（序号生成）
        processNewVideos(playVideos, newVideos)
        _isCompete.value = false
        // 6. 核心修复：通过setValue更新LiveData，触发观察者通知
        updateVideoLiveData(newVideos)

        // 7. 创建/添加下载任务
        newVideos.forEach { item ->
            if (videoItem.isEmpty()) add(item) else create(item)
        }

        // 8. 保存最新任务列表到缓存
        saveDownloadTaskCache()
    }

    /**
     * 去重视频列表（按url+fileName，优先保留有id的项）
     */
    private fun deduplicateVideoItems(list: MutableList<Video>): MutableList<Video> {
        val uniqueMap = mutableMapOf<String, Video>()

        list.forEach { item ->
            val key = "${item.url}|${item.fileName}"
            val existingItem = uniqueMap[key]
            when {
                existingItem == null -> uniqueMap[key] = item
                existingItem.id == -1L && item.id != -1L -> uniqueMap[key] = item // 优先保留有任务id的项
            }
        }

        return filterDuplicateTitleList(uniqueMap.values.toMutableList())
    }

    /**
     * 过滤重复标题（优先保留带序号括号的项）
     */
    fun filterDuplicateTitleList(list: List<Video>): MutableList<Video> {
        val groupMap = mutableMapOf<String, MutableList<Video>>()

        list.forEach { item ->
            val baseTitle = getBaseTitle(item.fileName.trim())
            groupMap.getOrPut(baseTitle) { mutableListOf() }.add(item)
        }

        val resultList = mutableListOf<Video>()
        groupMap.forEach { (_, itemList) ->
            val hasBracketItems = itemList.filter { hasNumberBracket(it.fileName) }
            resultList.addAll(if (hasBracketItems.isNotEmpty()) hasBracketItems else itemList)
        }

        return resultList
    }

    /**
     * 创建下载任务（首次创建/状态更新）
     */
    fun create(videoItem: Video) {
        if (videoItem.id < 0) {
            // 新建任务
            val fileName = getFileExtension(videoItem)
            val taskId = if (isHlsMediaUrl(videoItem.url)) {
                createM3u8DownloadTask(videoItem.url, fileName)
            } else {
                createNormalDownloadTask(videoItem.url, fileName)
            }
            // 更新videoItem的id，并同步到LiveData
            updateVideoItemInList(videoItem) { it.copy(id = taskId) }
        } else {
            // 恢复/暂停已有任务
            when (videoItem.downloadStatus) {
                IEntity.STATE_STOP -> stop(videoItem.id)
                IEntity.STATE_RUNNING -> resume(videoItem.id)
            }
            // 更新任务状态到LiveData
            updateVideoItemInList(videoItem) {
                it.copy(
                    path = videoItem.path,
                    downloadStatus = IEntity.STATE_STOP,
                    process = videoItem.process,
                    speed = videoItem.speed,
                    downloadProcess = videoItem.downloadProcess,
                    totalSize = videoItem.totalSize
                )
            }
        }
    }

    /**
     * 添加并暂停下载任务
     */
    fun add(videoItem: Video) {
        if (isHlsMediaUrl(videoItem.url)) {
            Aria.download(this)
                .load(videoItem.id)
                .ignoreCheckPermissions()
                .m3u8VodOption(getDefaultM3u8Option())
                .stop()
        } else {
            Aria.download(this)
                .load(videoItem.id)
                .ignoreCheckPermissions()
                .stop()
        }
        // 更新任务状态到LiveData
        updateVideoItemInList(videoItem) {
            it.copy(
                path = videoItem.path,
                downloadStatus = IEntity.STATE_STOP,
                process = videoItem.process,
                speed = videoItem.speed,
                downloadProcess = videoItem.downloadProcess,
                totalSize = videoItem.totalSize
            )
        }
    }

    // ========== 下载任务操作封装（抽离重复逻辑） ==========
    /**
     * 创建普通文件下载任务
     */
    private fun createNormalDownloadTask(url: String, fileName: String): Long {
        return Aria.download(this)
            .load(url)
            .setFilePath("$internalDir/$fileName")
            .ignoreFilePathOccupy()
            .ignoreCheckPermissions()
            .create()
    }

    /**
     * 创建M3U8下载任务（带默认配置）
     */
    private fun createM3u8DownloadTask(url: String, fileName: String): Long {
        return Aria.download(this)
            .load(url)
            .setFilePath("$internalDir/$fileName")
            .ignoreCheckPermissions()
            .ignoreFilePathOccupy()
            .m3u8VodOption(getDefaultM3u8Option())
            .create()
    }

    /**
     * 获取默认M3U8下载配置（抽离重复配置）
     */
    private fun getDefaultM3u8Option(): M3U8VodOption {
        return M3U8VodOption().apply {
            setVodTsUrlConvert(VodTsDefConverter())
            setBandWidthUrlConverter(BandWidthDefConverter())
            setUseDefConvert(false)
            merge(true)
            generateIndexFile()
            ignoreFailureTs()
        }
    }

    /**
     * 暂停下载任务
     */
    fun stop(taskId: Long) {
        Aria.download(this).load(taskId).ignoreCheckPermissions().stop()
        // 更新状态到LiveData
        updateVideoStatus(taskId, IEntity.STATE_STOP)
    }

    /**
     * 恢复下载任务
     */
    fun resume(taskId: Long) {
        Aria.download(this).load(taskId).ignoreCheckPermissions().resume(true)
        // 更新状态到LiveData
        updateVideoStatus(taskId, IEntity.STATE_RUNNING)
    }

    /**
     * 重试下载任务
     */
    fun retry(taskId: Long) {
        Aria.download(this).load(taskId).ignoreCheckPermissions().reTry()
    }

    /**
     * 取消任务（不删除文件）
     */
    fun cancelNoRemove(taskId: Long) {
        Aria.download(this).load(taskId).ignoreCheckPermissions().cancel()
        removeVideoFromList(taskId)
    }

    /**
     * 取消任务（删除文件）
     */
    fun cancel(taskId: Long) {
        Aria.download(this).load(taskId).ignoreCheckPermissions().cancel(true)
        removeVideoFromList(taskId)
    }

    /**
     * 重新开始下载任务
     */
    fun reStart(taskId: Long) {
        Aria.download(this).load(taskId).ignoreCheckPermissions().reStart()
    }

    // ========== LiveData 核心操作封装（解决通知不生效） ==========
    /**
     * 安全更新Video LiveData（主线程）
     * @param newList 新的视频列表
     */
    private fun updateVideoLiveData(newList: MutableList<Video>) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _videoItems.value = newList // 主线程直接更新
        } else {
            mainHandler.post { _videoItems.value = newList } // 子线程切换主线程
        }
    }

    /**
     * 更新列表中指定Video的属性
     * @param targetItem 目标Video（匹配url+id）
     * @param updateAction 更新操作
     */
    private fun updateVideoItemInList(targetItem: Video, updateAction: (Video) -> Video) {
        val currentList = _videoItems.value ?: return
        val index = currentList.indexOfFirst { it.url == targetItem.url && it.id == targetItem.id }
        if (index != -1) {
            val newList = currentList.toMutableList()
            newList[index] = updateAction(newList[index])
            updateVideoLiveData(newList)
        }
    }

    /**
     * 更新指定任务id的视频状态
     */
    private fun updateVideoStatus(taskId: Long, status: Int) {
        val currentList = _videoItems.value ?: return
        val index = currentList.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val newList = currentList.toMutableList()
            newList[index] = newList[index].copy(downloadStatus = status)
            updateVideoLiveData(newList)
        }
    }

    private fun removeVideoFromList(taskId: Long) {
        if (taskId == -1L) return
        val currentList = _videoItems.value ?: return
        val newList = currentList.filter { it.id != taskId }.toMutableList()
        updateVideoLiveData(newList)
        saveDownloadTaskCache() // 同步缓存
    }

    private fun saveDownloadTaskCache() {
        val currentList = _videoItems.value ?: mutableListOf()
        AppCache.downloadTask = Json.encodeToString(currentList)
    }

    override fun onWait(task: DownloadTask?) {
        task?.let {
            val video = _videoItems.value?.find { v -> v.id == it.entity.id }
            video?.let { v ->
                updateVideoItemInList(v) {
                    it.copy(
                        downloadStatus = task.entity.state,
                        totalSize = task.entity.fileSize,
                        downloadProcess = "${convertBytesToHumanReadable(0)}/${convertBytesToHumanReadable(
                            task.entity.fileSize
                        )}",
                        speed = formatSpeed(task.entity.speed),
                        process = 0
                    )
                }
            }
        }
    }

    override fun onPre(task: DownloadTask?) = updatateVideoItem(task)
    override fun onTaskPre(task: DownloadTask?) = updatateVideoItem(task)
    override fun onTaskResume(task: DownloadTask?) = updatateVideoItem(task)
    override fun onTaskStart(task: DownloadTask?) = updatateVideoItem(task)
    override fun onTaskStop(task: DownloadTask?) = updatateVideoItem(task)
    override fun onTaskCancel(task: DownloadTask?) = updatateVideoItem(task)
    override fun onTaskFail(task: DownloadTask?, e: Exception?) = updatateVideoItem(task)
    override fun onTaskComplete(task: DownloadTask?) = updatateVideoItem(task)
    override fun onTaskRunning(task: DownloadTask?) = updatateVideoItem(task)
    override fun onNoSupportBreakPoint(task: DownloadTask?) = Unit

    /**
     * 统一更新下载任务状态到LiveData
     * 核心修复：所有修改都生成新列表，通过updateVideoLiveData触发通知
     */
    private fun updatateVideoItem(task: DownloadTask?) {
        if (task == null || isUpdating.get()) return
        isUpdating.set(true)

        try {
            val entity = task.downloadEntity
            val currentList = _videoItems.value ?: return
            val index = currentList.indexOfFirst { it.url == entity.url && it.id == entity.id }

            if (index != -1) {
                val newList = currentList.toMutableList()
                val videoItem = newList[index]

                // 处理M3U8文件大小计算
                if (entity.state == IEntity.STATE_RUNNING && entity.m3U8Entity != null) {
                    handleM3u8FileSize(task, entity.id.toString())
                }

                // 构建更新后的Video对象
                val updatedItem = videoItem.copy(
                    path = entity.filePath ?: "",
                    downloadStatus = entity.state ?: -1,
                    process = entity.percent?.toLong() ?: 0,
                    speed = formatSpeed(entity.speed ?: 0),
                    downloadProcess = "${convertBytesToHumanReadable(entity.currentProgress ?: 0)}/${convertBytesToHumanReadable(entity.fileSize ?: 0)}",
                    totalSize = entity.fileSize ?: 0
                )

                // 处理下载完成逻辑
                val finalItem = if (entity.state == IEntity.STATE_COMPLETE) {
                    // 修正苹果格式mimeTypes
                    val mimeType = if (updatedItem.mimeTypes?.contains("vnd.apple") == true) "video/mp4" else updatedItem.mimeTypes
                    // 添加完成时间
                    val completedItem = updatedItem.copy(
                        mimeTypes = mimeType,
                        downloadCompletedTime = System.currentTimeMillis()
                    )
                    // 移到播放列表
                    val playList = getPlayList()
                    playList.add(completedItem)
                    AppCache.playVideos = Json.encodeToString(playList)
                    // 从下载列表移除
                    newList.removeAt(index)
                    _isCompete.value = true
                    completedItem
                } else {
                    newList[index] = updatedItem
                    updatedItem
                }

                // 触发LiveData通知
                updateVideoLiveData(newList)

                // 保存缓存（运行中/完成状态）
                if (entity.state == IEntity.STATE_RUNNING || entity.state == IEntity.STATE_COMPLETE) {
                    saveDownloadTaskCache()
                }
            }
        } finally {
            isUpdating.set(false)
        }
    }

    // ========== 工具方法（保持不变，优化命名/空安全） ==========
    private fun getBaseTitle(title: String): String {
        val regex = Regex("^(.*)\\((\\d+)\\)$")
        return regex.find(title.trim())?.groupValues?.get(1)?.trim() ?: title.trim()
    }

    private fun hasNumberBracket(title: String): Boolean {
        val regex = Regex("^.*\\((\\d+)\\)$")
        return regex.matches(title.trim())
    }

    private fun getFileExtension(videoItem: Video): String {
        val safeTitle = videoItem.fileName.ifEmpty { "${System.currentTimeMillis()}" }
        val fileExtension = when (videoItem.mimeTypes) {
            "video/mp4" -> ".mp4"
            "video/avi" -> ".avi"
            "video/mov" -> ".mov"
            "video/wmv" -> ".wmv"
            "video/flv" -> ".flv"
            "video/webm" -> ".webm"
            "audio/mp3" -> ".mp3"
            "audio/wav" -> ".wav"
            "audio/ogg" -> ".ogg"
            "application/x-mpegURL" -> ".m3u8"
            else -> getExtensionFromUrl(videoItem.url)
        }
        return "$safeTitle$fileExtension"
    }

    private fun getExtensionFromUrl(url: String): String {
        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.endsWith(".mp4") -> ".mp4"
            lowerUrl.endsWith(".avi") -> ".avi"
            lowerUrl.endsWith(".mov") -> ".mov"
            lowerUrl.endsWith(".wmv") -> ".wmv"
            lowerUrl.endsWith(".flv") -> ".flv"
            lowerUrl.endsWith(".webm") -> ".webm"
            lowerUrl.endsWith(".mp3") -> ".mp3"
            lowerUrl.endsWith(".wav") -> ".wav"
            lowerUrl.endsWith(".ogg") -> ".ogg"
            else -> ".mp4"
        }
    }

    fun getPlayList(): MutableList<Video> {
        return try {
            val playListStr = AppCache.playVideos
            if (playListStr.isNotEmpty()) Json.decodeFromString(playListStr) else mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun convertBytesToHumanReadable(bytes: Long): String {
        val kilobyte = 1024L
        val megabyte = kilobyte * 1024L
        val gigabyte = megabyte * 1024L

        return when {
            bytes >= gigabyte -> String.format("%.2f GB", bytes.toDouble() / gigabyte)
            bytes >= megabyte -> String.format("%.2f MB", bytes.toDouble() / megabyte)
            bytes >= kilobyte -> String.format("%.2f KB", bytes.toDouble() / kilobyte)
            bytes > 0 -> String.format("%.2f B", bytes.toDouble())
            else -> "0 B"
        }
    }

    fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "${bytesPerSecond} B/s"
            bytesPerSecond < 1024 * 1024 -> String.format("%.1f KB/s", bytesPerSecond / 1024.0)
            else -> String.format("%.1f MB/s", bytesPerSecond / (1024.0 * 1024.0))
        }
    }

    fun processNewVideos(existingVideos: List<Video>, newVideos: MutableList<Video>) {
        val allExistingNames = existingVideos.map { it.getBaseName() }
        val allExistingFullNames = existingVideos.map {
            it.getBaseName() + (if (it.getSequentialNumber() > 0) "(${it.getSequentialNumber()})" else "")
        }.toMutableSet()
        val processedNewNames = mutableSetOf<String>()

        newVideos.forEach { newVideo ->
            val baseName = newVideo.getBaseName()
            val ext = newVideo.fileName.substringAfterLast('.', "")
            val extSuffix = if (ext.isNotEmpty()) ".$ext" else ""

            val isDuplicate = allExistingFullNames.any { it.startsWith(baseName) } ||
                    allExistingNames.contains(baseName) ||
                    processedNewNames.any { it.startsWith(baseName) }

            if (isDuplicate) {
                val newBaseName = generateSequentialName(baseName, allExistingFullNames + processedNewNames)
                newVideo.fileName = newBaseName + extSuffix
            }

            processedNewNames.add(newVideo.getBaseName())
            allExistingFullNames.add(newVideo.getBaseName())
        }
    }

    fun generateSequentialName(baseName: String, existingNames: Collection<String>): String {
        val sequentialNumbers = mutableListOf<Int>()
        val pattern = Regex("^${Regex.escape(baseName)}\\((\\d+)\\)$")

        existingNames.forEach { name ->
            pattern.find(name)?.groups?.get(1)?.value?.toInt()?.let { sequentialNumbers.add(it) }
        }

        val nextNumber = sequentialNumbers.maxOrNull()?.plus(1) ?: 1
        return "$baseName($nextNumber)"
    }

    private fun isHlsMediaUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.endsWith(".m3u8") ||
                lowerUrl.endsWith(".ts") ||
                lowerUrl.contains(".m3u8?") ||
                lowerUrl.contains(".ts?")
    }

    /**
     * 处理M3U8文件大小计算
     */
    private fun handleM3u8FileSize(task: DownloadTask, taskId: String) {
        val m3u8Entity = task.downloadEntity.m3U8Entity ?: return
        if (m3u8Entity.peerIndex < 6) {
            m3u8SizeList[taskId] = (m3u8SizeList[taskId] ?: 0) + (task.downloadEntity.currentProgress ?: 0)
            task.entity.fileSize = m3u8Entity.peerNum * 300 * 1024L
        } else {
            val avgBytesPerSegment = m3u8SizeList[taskId]?.div(5)
            task.downloadEntity.fileSize = avgBytesPerSegment?.times(m3u8Entity.peerNum) ?: 0
        }
    }

    // 释放资源（建议在Activity/Fragment销毁时调用）
    fun destroy() {
        Aria.download(this).unRegister()
    }
}
// Video类扩展函数（补充getBaseName，避免编译错误）
fun Video.getBaseName(): String {
    return this.fileName.substringBeforeLast('.', this.fileName).trim()
}