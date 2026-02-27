package com.download.video_download.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.utils.LogUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.coroutineContext

class SplashViewModel : BaseViewModel() {
    
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress
    
    private val _isLoadingComplete = MutableLiveData<Boolean>()
    val isLoadingComplete: LiveData<Boolean> = _isLoadingComplete
    private val _isAdLoaded = MutableLiveData<Boolean>(false)

    val isAdLoaded: MutableLiveData<Boolean> = _isAdLoaded
    private var loadingJob: Job? = null
    companion object {
        private const val STAGE1_DURATION = 1000L
        private const val STAGE1_TARGET = 60
        private const val STAGE1_INTERVAL = 10L

        private const val STAGE2_MAX_DURATION = 9000L
        private const val STAGE2_INTERVAL = 50L
        private const val TOTAL_MAX_DURATION = 10000L

        private const val FILL_PROGRESS_DURATION = 1000L
        private const val FILL_INTERVAL = 10L
        private const val MAX_PROGRESS = 100
    }
    fun startLoading(checkAdLoaded: () -> Boolean) {
        loadingJob?.cancel()
        // 重置所有状态
        _progress.value = 0
        _isLoadingComplete.value = false
        _isAdLoaded.value = false

        loadingJob = viewModelScope.launch {
            val timeoutResult = withTimeoutOrNull(TOTAL_MAX_DURATION) {
                runStage1()

                runStage2(checkAdLoaded)
            }

            if (timeoutResult == null) {
                _progress.value = MAX_PROGRESS
                _isLoadingComplete.value = true
            }
        }
    }

    fun resetAfterAdClosed() {
        loadingJob?.cancel()
        _progress.value = 0
        _isLoadingComplete.value = false
        _isAdLoaded.value = false
    }

    private suspend fun runStage1() {
        val totalSteps = (STAGE1_DURATION / STAGE1_INTERVAL).toInt()
        val progressStep = STAGE1_TARGET.toFloat() / totalSteps
        var currentProgress = 0f

        for (i in 0..totalSteps) {
            if (!currentCoroutineContext().isActive) break
            _progress.value = currentProgress.toInt()
            currentProgress += progressStep
            delay(STAGE1_INTERVAL)
        }
        _progress.value = STAGE1_TARGET
    }


    private suspend fun runStage2(checkAdLoaded: () -> Boolean) {
        val totalSteps = (STAGE2_MAX_DURATION / STAGE2_INTERVAL).toInt()
        val remainingProgress = MAX_PROGRESS - STAGE1_TARGET
        val progressStep = remainingProgress.toFloat() / totalSteps

        var currentProgress = STAGE1_TARGET.toFloat()
        var hasAdLoaded = false

        for (i in 0..totalSteps) {
            if (!currentCoroutineContext().isActive) break

            hasAdLoaded = _isAdLoaded.value == true || checkAdLoaded()
            if (hasAdLoaded) {
                fillProgress(currentProgress.toInt())
                break
            }

            _progress.value = currentProgress.toInt()
            currentProgress += progressStep
            delay(STAGE2_INTERVAL)
        }

        _progress.value = MAX_PROGRESS
        _isLoadingComplete.value = true
    }

    private suspend fun fillProgress(currentProgress: Int) {
        val remaining = MAX_PROGRESS - currentProgress
        val totalSteps = (FILL_PROGRESS_DURATION / FILL_INTERVAL).toInt()
        val progressStep = remaining.toFloat() / totalSteps

        var tempProgress = currentProgress.toFloat()
        for (i in 0..totalSteps) {
            if (!currentCoroutineContext().isActive) break
            _progress.value = tempProgress.toInt()
            tempProgress += progressStep
            delay(FILL_INTERVAL)
        }
        _progress.value = MAX_PROGRESS
    }

    override fun onCleared() {
        super.onCleared()
        loadingJob?.cancel()
    }
    fun preloadAd(context: Context) {
        viewModelScope.launch {
            AdMgr.INSTANCE.preloadAd(AdPosition.LOADING, AdType.APP_OPEN, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    _isAdLoaded.postValue(loadState == AdLoadState.LOADED)
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
            })
        }
    }
    fun preloadLgAd(context: Context) {
        viewModelScope.launch {
            val lgAdPair = listOf(AdPosition.LANGUAGE to AdType.NATIVE, AdPosition.LANGUAGE to AdType.INTERSTITIAL)
            AdMgr.INSTANCE.batchPreloadAds(lgAdPair, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
    fun preloadGuideAd(context: Context) {
        viewModelScope.launch {
            val gdAdPair = listOf(AdPosition.GUIDE to AdType.NATIVE, AdPosition.GUIDE  to AdType.INTERSTITIAL)
            AdMgr.INSTANCE.batchPreloadAds(gdAdPair, context,
                onLoadStateChanged = { position, adType, loadState,error ->
                    LogUtils.d("广告:  ${error?.message}${error?.domain}")
                })
        }
    }
}