package com.download.video_download.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.model.WebsiteData
import com.download.video_download.base.utils.LogUtils
import kotlinx.coroutines.launch

class HomeViewModel: BaseViewModel() {
    private val _videoList = MutableLiveData<MutableList<WebsiteData>>()
    val videoList: LiveData<MutableList<WebsiteData>> get() = _videoList
    private val _isAdLoaded = MutableLiveData<Boolean>(false)

    val isAdLoaded: MutableLiveData<Boolean> = _isAdLoaded
    init {
        initWebSiteData()
    }

    fun initWebSiteData(){
        val fb = WebsiteData(
            title = "Facebook",
            image = R.mipmap.ic_fb,
            url = "https://www.facebook.com"
        )
        val x = WebsiteData(
            title = "X",
            image = R.mipmap.ic_x,
            url = "https://www.x.com"
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
        val imdb = WebsiteData(
            title = App.getAppContext().getString(R.string.imdb),
            image = R.mipmap.ic_imdb,
            url = "www.imdb.com"
        )
        val newData = mutableListOf(fb,x,video,mixkit,imdb)
        _videoList.value = newData
    }
    fun preloadAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.HOME, AdType.INTERSTITIAL, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadSNAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.SEARCH, AdType.NATIVE, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadBkAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.BACK, AdType.INTERSTITIAL, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadTabAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.TAB, AdType.INTERSTITIAL, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadNAd(context: Context) {
        val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.HOME, AdType.NATIVE) == AdLoadState.LOADED
        if (hasCache){
            _isAdLoaded.postValue(true)
            return
        }
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.HOME, AdType.NATIVE, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    _isAdLoaded.postValue(loadState == AdLoadState.LOADED)
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
}