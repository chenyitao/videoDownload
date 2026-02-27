package com.download.video_download.ui.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.model.GuideData
import com.download.video_download.base.utils.LogUtils
import kotlinx.coroutines.launch

class GuideViewModel : BaseViewModel() {
    private val _isAdLoaded = MutableLiveData<Boolean>(false)
    val isAdLoaded: MutableLiveData<Boolean> = _isAdLoaded
    fun getGuideList(from: String): MutableList<GuideData> {
        val list = mutableListOf<GuideData>()
        val guide3 = GuideData(
            title = App.getAppContext().getString(R.string.guide3_title),
            image = R.mipmap.bg_g_3,
            description = App.getAppContext().getString(R.string.guide3_des)
        )
        if (from != "language" &&  from != "splash"){
            val guide1 = GuideData(
                title = App.getAppContext().getString(R.string.guide1_title),
                image = R.mipmap.bg_g_1,
                description = ""
            )
            val guide2 = GuideData(
                title = App.getAppContext().getString(R.string.guide2_title),
                image = R.mipmap.bg_g_2,
                description = ""
            )
            list.add(guide1)
            list.add(guide2)
            list.add(guide3)
        }else{
            val guide1 = GuideData(
                title = App.getAppContext().getString(R.string.guide1_title_new),
                image = R.mipmap.bg_g_1,
                description = ""
            )
            val guide2 = GuideData(
                title = App.getAppContext().getString(R.string.guide2_title_new),
                image = R.mipmap.bg_g_2,
                description = ""
            )
            list.add(guide1)
            list.add(guide2)
        }
        return list
    }
    fun handleNativeAd(context: Context) {
        viewModelScope.launch {
            val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.GUIDE, AdType.NATIVE) == AdLoadState.LOADED
            if (hasCache){
                isAdLoaded.postValue( true)
                return@launch
            }
            AdMgr.INSTANCE.preloadAd(
                AdPosition.GUIDE,
                AdType.NATIVE,
                context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                    if (loadState == AdLoadState.LOADED){
                        isAdLoaded.postValue( true)
                    }
                }
            )
        }
    }
    fun preNativeAd(context: Context) {
        viewModelScope.launch {
            val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.GUIDE, AdType.NATIVE) == AdLoadState.LOADED
            if (hasCache){
                return@launch
            }
            AdMgr.INSTANCE.preloadAd(
                AdPosition.GUIDE,
                AdType.NATIVE,
                context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                }
            )
        }
    }
    fun handInvestAd(context: Context){
        viewModelScope.launch {
            val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.GUIDE, AdType.INTERSTITIAL) == AdLoadState.LOADED
            if (hasCache){
                return@launch
            }
            AdMgr.INSTANCE.preloadAd(
                AdPosition.GUIDE,
                AdType.INTERSTITIAL,
                context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                }
            )
        }
    }
}