package com.download.video_download.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.download.video_download.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel() {
    
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress
    
    private val _isLoadingComplete = MutableLiveData<Boolean>()
    val isLoadingComplete: LiveData<Boolean> = _isLoadingComplete
    
    companion object {
        const val TOTAL_DURATION = 2000L
        const val UPDATE_INTERVAL = 20L
        const val MAX_PROGRESS = 100
    }
    fun startLoading() {
        viewModelScope.launch {
            _isLoadingComplete.value = false
            val steps = (TOTAL_DURATION / UPDATE_INTERVAL).toInt()
            val increment = MAX_PROGRESS.toFloat() / steps
            
            var currentProgress = 0f
            
            for (i in 0..steps) {
                _progress.value = currentProgress.toInt()
                currentProgress += increment
                delay(UPDATE_INTERVAL)
            }
            _progress.value = MAX_PROGRESS
            _isLoadingComplete.value = true
        }
    }
}