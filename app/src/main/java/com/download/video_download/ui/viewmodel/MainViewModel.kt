package com.download.video_download.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.model.NavigationItem

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
}