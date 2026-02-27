package com.download.video_download.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.utils.LogUtils
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {
    private val _nav = MutableLiveData<NavigationItem?>()
    val nav: LiveData<NavigationItem?> get() = _nav

    var isFromPermissionBack = false
    fun navigate(item: NavigationItem) {
        _nav.value = item
    }
    fun clearNavigation() {
        _nav.value = null
    }
    fun preloadTabAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.TAB, AdType.INTERSTITIAL, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
}