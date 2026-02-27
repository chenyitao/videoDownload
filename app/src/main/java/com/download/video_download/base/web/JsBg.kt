package com.download.video_download.base.web

import android.webkit.JavascriptInterface
import androidx.core.net.toUri
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.web.VideoParse.shouldNowParse
import com.download.video_download.base.web.VideoParse.videoParse2
import com.download.video_download.base.web.VideoParse.videoParse3
import com.download.video_download.base.web.VideoParse.videoParse4
import com.download.video_download.base.web.VideoParse.videoParseOne
import kotlinx.coroutines.Job
import kotlin.text.isEmpty

class JsBg(
    private val videoData: (videos: MutableList<Video>,isLoading: Boolean) -> Unit,
    private val jobs:MutableList<Job>,
    private val isInit: () -> Int) {
    @JavascriptInterface
    fun onInit(): Int {
        return isInit()
    }
    @JavascriptInterface
    fun isNowDebug(): Boolean {
        return false
    }
    @JavascriptInterface
    fun onNowShouldInitCurrentPage(pageUrl: String): Boolean {
        return true
    }
    @JavascriptInterface
    fun onShouldCurrentNowPageUrl(pageUrl: String): Boolean {
        return true
    }
    @JavascriptInterface
    fun readyStateChange(readyState: String, location: String) {

    }
    @JavascriptInterface
    fun onReadNowResponse(reqUrl: String, resUrl: String, resource: String) {
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser6, mapOf("safedddd" to resource))
        val job = videoParse4(resource, { videos,isLoading ->
            videoData(videos,isLoading)
        })
        if (job != null){
            jobs.add(job)
        }
    }
    @JavascriptInterface
    fun onVideoNowSrc(pageUrl: String, url: String) {
        if(url.isEmpty()){
            return
        }
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser6, mapOf("safedddd" to url))
        val job = videoParseOne(pageUrl,url, { videos,isLoading ->
            videoData(videos,isLoading)
        })
        jobs.add(job)
    }
    @JavascriptInterface
    fun onNowDownloadResponseUnit(pageUrl: String, list: String, web: String) {
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser6, mapOf("safedddd" to web))
        val job = videoParse3(list,web, { videos,isLoading ->
            videoData(videos,isLoading)
        })
        jobs.add(job)
    }
    @JavascriptInterface
    fun onVideoNowSource(pageUrl: String, url: String) {
        if(url.isEmpty()){
            return
        }
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser6, mapOf("safedddd" to url))
        val job = videoParse2(pageUrl,url, { videos,isLoading ->
            videoData(videos,isLoading)
        })
        jobs.add(job)
    }
    @JavascriptInterface
    fun shouldNowReadResponse(reqUrl: String, resUrl: String, resource: String): Boolean {
        return shouldNowParse(reqUrl)
    }
}