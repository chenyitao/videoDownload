package com.download.video_download.base.web

import android.util.Log
import androidx.core.net.toUri
import com.download.video_download.base.room.entity.Video
import com.google.common.net.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Collections
import kotlin.collections.set
import kotlin.text.contains

object VideoParse {
    private const val WebXvideo = "1"
    private const val WebXnx = "2"
    private const val WebSpnkBng = "4"
    private const val WebYouJzz = "5"
    private const val WebPrnHub = "6"
    private const val WebRdTube = "7"
    private const val WebYouPrn = "8"
    private const val WebErome = "10"
    private const val WebPornOne = "11"
    private const val WebPrnTrex = "12"
    private const val WebTkTube = "13"
    private const val WebXvdeosPrn = "15"
    private const val WebHdSx = "16"
    private const val WebJwPlayer = "18"
    val tkAPI = listOf(
        "api/recommend/item_list",
        "api/reflow/item/detail",
        "api/hot/item_list",
        "api/for_you/like",
        "api/music/item_list",
        "api/favorite/thumb",
        "api/post/item_list",
        "api/shop/price",
        "api/challenge/item_list",
        "api/down/xx",
        "api/preload/item_list",
    )
    val perWebsite = listOf(
        "tiktok", "freepik","facebook","x",
        "pornhub", "xvideos", "xxnx", "redtube", "pornone", "spankbang",
        "erome", "youjizz", "youporn", "tktube", "hdsex2", "porntrex",
        "xvideosporno", "noodlemagazine", "porntrex", "imdb", "mixkit",
    )
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()
    val detectVideo = Collections.synchronizedMap<String, Video>(mutableMapOf())
    fun videoParseOne(pageUrl: String,url:String,videoData: (videos: MutableList<Video>,isLoading: Boolean) -> Unit):Job{
        val job = scope.launch {
            videoData(mutableListOf(),true)
            mutex.withLock {
                try {
                    val video = detectVideo[url] ?: return@launch
                    val dVideo = parseVideoUrl(url)
                    runCatching {
                        detectVideo[url] = dVideo
                        videoData(detectVideo.values.toMutableList(),false)
                    }.onFailure {
                        detectVideo[url] = dVideo
                        videoData(detectVideo.values.toMutableList(),false)
                    }
                }finally {
                    if (detectVideo.isEmpty()){
                        videoData(mutableListOf(),false)
                    }else{
                        videoData(detectVideo.values.toMutableList(),false)
                    }
                }
            }
        }
        return job
    }
    private suspend  fun parseVideoUrl(url: String): Video = withContext(Dispatchers.IO) {
        var fileName: String? = null
        var fileExtension: String? = null
        var totalSize: Long? = null
        runCatching {
                val headers = NativeHttpUtils.getUrlHeaders(url)
                val contentDisposition = headers["Content-Disposition"]
                if (contentDisposition != null) {
                    contentDisposition.substringAfter("filename=", "").takeUnless { it.isBlank() }
                        ?.let { fileName = it }
                    if (fileName.isNullOrEmpty()) {
                        contentDisposition.substringAfter("filename*=", "")
                            .trimStart(*"UTF-8''".toCharArray())
                            .takeUnless { it.isBlank() }?.let { fileName = it }
                    }
                }
                val contentType = headers["Content-Type"]
                fileExtension = contentType
                totalSize = headers["Content-Length"]?.toLongOrNull()
                if (fileExtension.isNullOrEmpty()) {
                    fileExtension = url.toUri().pathSegments.last().substringBefore("?").substringAfterLast(".")
                }
                if (fileExtension.isEmpty()) {
                    fileExtension = "mp4"
                }
                if (!fileName.isNullOrEmpty() && fileExtension.isEmpty()) {
                    fileExtension = fileName.substringAfterLast(".")
                }

                if (fileName.isNullOrEmpty()) {
                    fileName = url.toUri().pathSegments.last().substringBefore("?")
                }

                fileName = fileName.substringBeforeLast(".")
                if (fileName.isEmpty()) {
                    fileName = "Unknown"
                }
        }.onFailure {

        }
        Video(
            url = url,
            fileName = fileName ?: "Unknown",
            mimeTypes = fileExtension ?: "mp4",
            totalSize = totalSize?.toInt() ?: 0,
        )
    }
    fun videoParse2(pageUrl: String,url:String,videoData: (videos: MutableList<Video>,isLoading: Boolean) -> Unit):Job{
        val job = scope.launch {
            videoData(mutableListOf(),true)
            mutex.withLock {
                try {
                    val urlsArray =
                        Json.decodeFromString<JsonElement>(url).jsonArray
                    val needHandleUrls =
                        urlsArray.map { it.jsonPrimitive.content }
                            .filter { it.isNotEmpty() }
                            .filter { detectVideo[it] == null }
                    if (needHandleUrls.isEmpty()) {
                        return@launch
                    }
                    val methodPairs =
                        needHandleUrls.map { innerUrl ->
                            Pair(
                                innerUrl,
                                parseVideoUrl(innerUrl)
                            )
                        }
                    runCatching {
                        methodPairs.forEach { info ->
                            detectVideo[info.first] =
                                info.second
                        }
                        videoData(detectVideo.values.toMutableList(),false)
                    }.onFailure {
                        methodPairs.forEach { info ->
                            detectVideo[info.first] =
                                info.second
                        }
                        videoData(detectVideo.values.toMutableList(),false)
                    }
                }finally {
                    if (detectVideo.isEmpty()){
                        videoData(mutableListOf(),false)
                    }else{
                        videoData(detectVideo.values.toMutableList(),false)
                    }
                }
            }
        }
        return job
    }
    fun videoParse3(list: String,web:String,videoData: (videos: MutableList<Video>,isLoading: Boolean) -> Unit):Job{
        val job = scope.launch {
            videoData(mutableListOf(),true)
            mutex.withLock {
                try {
                    val result = parseResponse(web, list)
                    runCatching {
                        result.forEach { (url, item) ->
                            detectVideo[url] = item
                        }
                        videoData(detectVideo.values.toMutableList(),false)
                    }.onFailure {
                        result.forEach { (url, item) ->
                            detectVideo[url] = item
                        }
                        videoData(detectVideo.values.toMutableList(),false)
                    }
                }finally {
                    if (detectVideo.isEmpty()){
                        videoData(mutableListOf(),false)
                    }else{
                        videoData(detectVideo.values.toMutableList(),false)
                    }
                }
            }
        }
        return job
    }
    private suspend fun parseResponse(
        web: String,
        list: String
    ): List<Pair<String, Video>> {
        when (web) {
            WebXvideo, WebXnx, WebHdSx -> {
                val obj = Json.decodeFromString<JsonObject>(list)
                val videoTitle = obj["video_title"]?.jsonPrimitive?.content
                val imageUrl = obj["image_url"]?.jsonPrimitive?.content
                val m3u8VideoUrl = obj["m3u8_video_url"]?.jsonPrimitive?.content
                if (m3u8VideoUrl.isNullOrEmpty()) {
                    return emptyList()
                }
                var data = parseVideoUrl(m3u8VideoUrl)
                data.thumb = imageUrl?:""
                data.fileName = videoTitle?:""
                val method = data
                return listOf(Pair(m3u8VideoUrl, method))
            }

            WebSpnkBng -> {
                val obj = Json.decodeFromString<JsonObject>(list)
                val streamDataObj = obj["stream_data"]?.jsonObject ?: return emptyList()
                val videoTitle = obj["video_title"]?.jsonPrimitive?.content
                val coverImage = obj["cover_image"]?.jsonPrimitive?.content
                val thumbnail = obj["thumbnail"]?.jsonPrimitive?.content

                val parseVideoUrls = streamDataObj.filter { entry ->
                    val name = entry.key
                    "480p".equals(name, true) || "720p".equals(name, true) || "1080p".equals(name, true)
                }.filter {
                    val innerArray = it.value.jsonArray
                    innerArray.isNotEmpty()
                }.map {
                    val innerArray = it.value.jsonArray
                    innerArray[0].jsonPrimitive.content
                }
                if (parseVideoUrls.isEmpty()) {
                    return emptyList()
                }
                return parseDownloadMethods(parseVideoUrls).map { pair ->
                    val data = pair.second
                    data.thumb = (coverImage ?: thumbnail).toString()
                    data.fileName = videoTitle?:""
                    Pair(pair.first, second = data)
                }
            }

            WebYouJzz -> {
                val obj = Json.decodeFromString<JsonObject>(list)
                val dataEncodings = obj["data_encodings"]?.jsonArray ?: return emptyList()
                val videoTitle = obj["video_title"]?.jsonPrimitive?.content
                val imageUrl = obj["image_url"]?.jsonPrimitive?.content
                val parseVideoUrls = dataEncodings.map { item ->
                    val obj = item.jsonObject
                    val name = obj["name"]?.jsonPrimitive?.content ?: ""
                    val filename = obj["filename"]?.jsonPrimitive?.content ?: return@map null
                    val finalName = videoTitle + "_" + name
                    val videoUrl = "https:$filename"
                    Pair(finalName, videoUrl)
                }.filterNotNull()
                val resultList = parseDownloadMethods(parseVideoUrls.map { it.second })
                return resultList.mapIndexed { index, pair ->
                    val title = parseVideoUrls[index]
                    val data = pair.second
                    data.fileName = title.first
                    data.thumb = imageUrl?:""
                    Pair(pair.first, second = data)
                }
            }

            WebPrnHub -> {
                val obj = Json.decodeFromString<JsonObject>(list)
                val mediaDefinitions = obj["media_definitions"]?.jsonArray ?: return emptyList()
                val videoTitle = obj["video_title"]?.jsonPrimitive?.content
                val imageUrl = obj["image_url"]?.jsonPrimitive?.content
                val parseVideoUrls = mediaDefinitions.map { item ->
                    val obj = item.jsonObject
                    val videoUrl = obj["videoUrl"]?.jsonPrimitive?.content ?: return@map null
                    val width = obj["width"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    val height = obj["height"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    val lastSuffix = if (width > 0 && height > 0) {
                        "${width}*${height}"
                    } else ""
                    val fileName = videoTitle + "_" + lastSuffix
                    Pair(fileName, videoUrl)
                }.filterNotNull()
                val resultList = parseDownloadMethods(parseVideoUrls.map { it.second })
                return resultList.mapIndexed { index, pair ->
                    val title = parseVideoUrls[index]
                    val data = pair.second
                    data.fileName = title.first
                    data.thumb = imageUrl?:""
                    Pair(pair.first, second = data)
                }
            }

            WebRdTube, WebYouPrn -> {
                val obj = Json.decodeFromString<JsonObject>(list)
                val mediaDefinitions = obj["media_definitions"]?.jsonArray ?: return emptyList()
                val videoTitle = obj["video_title"]?.jsonPrimitive?.content
                val imageUrl = obj["image_url"]?.jsonPrimitive?.content
                val parseVideoUrls = mediaDefinitions.map { item ->
                    val obj = item.jsonObject
                    val videoUrl = obj["videoUrl"]?.jsonPrimitive?.content ?: return@map null
                    val quality = obj["quality"]?.jsonPrimitive?.content
                    val fileName = videoTitle + "_" + quality
                    Pair(fileName, videoUrl)
                }.filterNotNull()
                val resultList = parseDownloadMethods(parseVideoUrls.map { it.second })
                return resultList.mapIndexed { index, pair ->
                    val title = parseVideoUrls[index]
                    val data = pair.second
                    data.fileName = title.first
                    data.thumb = imageUrl?:""
                    Pair(pair.first, second = data)
                }
            }

            WebErome, WebPornOne, WebPrnTrex, WebTkTube, WebXvdeosPrn -> {
                val obj = Json.decodeFromString<JsonObject>(list)
                val sources = obj["sources"]?.jsonArray ?: return emptyList()
                val videoTitle = obj["video_title"]?.jsonPrimitive?.content
                val imageUrl = obj["image_url"]?.jsonPrimitive?.content
                val parseVideoUrls = sources.map { item ->
                    val obj = item.jsonObject
                    val videoUrl = obj["url"]?.jsonPrimitive?.content ?: return@map null
                    val label = obj["label"]?.jsonPrimitive?.content
                    val fileName = videoTitle + "_" + label
                    Pair(fileName, videoUrl)
                }.filterNotNull()
                val resultList = parseDownloadMethods(parseVideoUrls.map { it.second })
                return resultList.mapIndexed { index, pair ->
                    val title = parseVideoUrls[index]
                    val data = pair.second
                    data.fileName = title.first
                    data.thumb = imageUrl?:""
                    Pair(pair.first, second = data)
                }
            }

            WebJwPlayer -> {
                val obj = Json.decodeFromString<JsonObject>(list)
                val sources = obj["allSources"]?.jsonArray ?: return emptyList()
                val imageUrl = obj["image"]?.jsonPrimitive?.content
                val parseVideoUrls = sources.map { item ->
                    val obj = item.jsonObject
                    val videoUrl = obj["file"]?.jsonPrimitive?.content ?: return@map null
                    videoUrl
                }.filterNotNull()
                val resultList = parseDownloadMethods(parseVideoUrls)
                return resultList.mapIndexed { index, pair ->
                    val data = pair.second
                    data.thumb = imageUrl?:""
                    Pair(pair.first, second = data)
                }
            }
        }
        return emptyList()
    }
    private suspend fun parseDownloadMethods(urls: List<String>): List<Pair<String, Video>> {
        return coroutineScope {
            urls.map { url ->
                async {
                    Pair(url, parseVideoUrl(url))
                }
            }.awaitAll()
        }
    }

    fun shouldNowParse(url: String): Boolean {
        val uri2 = url.toUri()
        val host = uri2.host
        val path = uri2.path

        return (if (host != null && path != null && (host.contains("tiktok.com"))) {
            val strArr = tkAPI
            for (i in 0..<strArr.size) {
                if (path.contains(strArr[i])) {
                    true
                }
            }
        } else {
            false
        }) as Boolean
    }
    fun videoParse4(url: String,videoData: (videos: MutableList<Video>,isLoading: Boolean) -> Unit):Job?{
        val uri = url.toUri()
        val path = uri.path ?: return null
        if (!path.contains("item_list")) return  null
        val job = scope.launch {
            try {
                videoData(mutableListOf(),true)
                mutex.withLock {
                    val result = parseTk(url)
                    result.forEach { (url, item) ->
                        detectVideo[url] = item
                        videoData(detectVideo.values.toMutableList(),false)
                    }
                }
            }finally {
                if (detectVideo.isEmpty()){
                    videoData(mutableListOf(),false)
                }else{
                    videoData(detectVideo.values.toMutableList(),false)
                }
            }
        }
        return job
    }
    private suspend fun parseTk(resource: String): List<Pair<String, Video>> {
        val obj = Json.decodeFromString<JsonObject>(resource)
        val responseObj = obj["response"]?.jsonObject ?: return emptyList()
        val contentObj = responseObj["content"]?.jsonObject ?: return emptyList()
        val itemListArray = contentObj["itemList"]?.jsonArray ?: return emptyList()
        val result = mutableListOf<Pair<String, Video>>()
        val filterResult = itemListArray.map { item ->
            val itemObj = item.jsonObject
            val desc = itemObj["desc"]?.jsonPrimitive?.content
            val videoObj = itemObj["video"]?.jsonObject ?: return@map null
            val playAddrStructObj = videoObj["PlayAddrStruct"]?.jsonObject

            val size = videoObj["size"]?.jsonPrimitive?.content?.toLongOrNull()
            val originCover = videoObj["originCover"]?.jsonPrimitive?.content
            var durationSeconds = videoObj["duration"]?.jsonPrimitive?.content?.toLongOrNull()
            if (durationSeconds != null) {
                durationSeconds = durationSeconds * 1000L
            }

            val urlList = playAddrStructObj?.get("UrlList")?.jsonArray ?: return@map null
            val urlItem = urlList.find { urlItem ->
                val urlStr = urlItem.jsonPrimitive.content
                return@find urlStr.contains("/play")
            }
            return@map if (urlItem != null) {
                val url = urlItem.jsonPrimitive.content
                Pair(
                    url, Video(
                        url = url,
                        mimeTypes = "video/mp4",
                        thumb = originCover?:"",
                        duration = durationSeconds?:0,
                        totalSize = size?.toInt()?:0,
                        fileName = desc ?: "Unknown"
                    )
                )
            } else {
                null
            }
        }.filterNotNull()

        result.addAll(filterResult)
        return result
    }
    fun isDouyinVideoUrl(url: String): Boolean {
        val patterns = listOf(
            Regex("^https?://v\\.douyin\\.com/\\w+/?$"),
            Regex("^https?://(www\\.)?douyin\\.com/video/\\d+/?$"),
            Regex("^https?://t\\.zijieimg\\.com/\\w+/?$")
        )
        return patterns.any { it.matches(url) }
    }
}