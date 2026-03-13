package com.download.video_download.ui.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
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
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.model.RWeb
import com.download.video_download.base.model.WebsiteData
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LogUtils
import kotlinx.coroutines.launch
import kotlin.collections.sort

class HomeViewModel: BaseViewModel() {
    private val _videoList = MutableLiveData<MutableList<WebsiteData>>()
    val videoList: LiveData<MutableList<WebsiteData>> get() = _videoList
    private val _isAdLoaded = MutableLiveData<Boolean>(false)

    val isAdLoaded: MutableLiveData<Boolean> = _isAdLoaded
    init {

    }
    private val COLORS = listOf(
        R.drawable.shape_site_fd9900_round,
        R.drawable.shape_site_04b4e4_round,
        R.drawable.shape_site_fe2727_round,
        R.drawable.shape_site_03b226_round,
        R.drawable.shape_site_1414c1_round,
    )
    fun initWebSiteData(){
        runCatching {
            val webList = mutableListOf<WebsiteData>()
            AppCache.rWeb.takeIf { it.isNotEmpty() }?.let {
                val sIndex = AppCache.hWebColorIndex
                val web: RWeb? = App.getAppContext().jsonParser().decodeFromString<RWeb>(it)
                web?.let { cg->
                    if (cg.show.isNotEmpty() && cg.show == "y" && cg.content.isNotEmpty()){
                        cg.content.forEachIndexed { index, websiteData ->
                            val colorIndex = (sIndex + index) % 5
                            webList.add(WebsiteData(image = 0, title = websiteData.title, url =websiteData.url, color = COLORS[colorIndex], sort = websiteData.sort))
                        }
                        webList.sortBy { it.sort }
                        val newIndex = (AppCache.hWebColorIndex + 1) % 5
                        AppCache.hWebColorIndex = newIndex
                    }
                }
            }
            val fb = WebsiteData(
                title = "Facebook",
                image = R.mipmap.ic_fb,
                url = "https://www.facebook.com",
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
            webList.add(fb)
            webList.add(x)
            webList.add(video)
            webList.add(mixkit)
            webList.add(imdb)
            _videoList.value = webList
        }.onFailure(
            {
                LogUtils.e("initWebSiteData: ", it)
            }
        )
    }
    fun preloadAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.HOME, AdType.INTERSTITIAL, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("ad:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadSNAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.SEARCH, AdType.NATIVE, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("ad:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadBkAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.BACK, AdType.INTERSTITIAL, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("ad:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadTabAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.TAB, AdType.INTERSTITIAL, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("ad:  ${error?.message}${error?.domain}")
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
                    LogUtils.d("ad:  ${error?.message}${error?.domain}")
                })
        }
    }
}