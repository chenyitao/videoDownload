package com.download.video_download.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.utils.AppCache
import kotlinx.serialization.encodeToString

class PlayerViewModel : BaseViewModel() {
    private val _videoList = MutableLiveData<MutableList<Video>>()
    val videoList: LiveData<MutableList<Video>> get() = _videoList
    init {
    }
    fun initVideoData() {
        var list = mutableListOf<Video>()
        val videCache = AppCache.playVideos
        if (videCache.isNotEmpty()) {
           runCatching {
               val json = json.decodeFromString<MutableList<Video>>(videCache)
               if (json.isNotEmpty()) {
                   list.addAll(json)
               }
           }.onFailure {
               list.sortByDescending { it.downloadCompletedTime }
               _videoList.value = list
           }.onSuccess {
               list.sortByDescending { it.downloadCompletedTime }
               _videoList.value = list
           }
        }
        _videoList.value = list
    }
    fun removeVideo(video: Video) {
        val list = _videoList.value
        if (list != null) {
            list.remove(video)
            _videoList.value = list
        }
        AppCache.playVideos = json.encodeToString(list)
    }
}