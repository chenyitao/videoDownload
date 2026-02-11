package com.download.video_download.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.model.WebsiteData

class HomeViewModel: BaseViewModel() {
    private val _videoList = MutableLiveData<MutableList<WebsiteData>>()
    val videoList: LiveData<MutableList<WebsiteData>> get() = _videoList
    init {
        initWebSiteData()
    }

    fun initWebSiteData(){
        val imdb = WebsiteData(
            title = App.getAppContext().getString(R.string.imdb),
            image = R.mipmap.ic_imdb,
            url = "www.imdb.com"
        )
        val video = WebsiteData(
            title = App.getAppContext().getString(R.string.video),
            image = R.mipmap.ic_video,
            url = "https://www.freepik.com/videos"
        )
        val mixkit = WebsiteData(
            title = App.getAppContext().getString(R.string.mixkit),
            image = R.mipmap.ic_mixkit,
            url = "https://mixkit.co/"
        )
        val newData = mutableListOf(imdb,video,mixkit)
        _videoList.value = newData
    }
}