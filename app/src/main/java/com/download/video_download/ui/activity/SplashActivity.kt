package com.download.video_download.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.utils.ActivityManager
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.databinding.ActivitySplashBinding
import com.download.video_download.ui.viewmodel.SplashViewModel
import com.google.android.gms.ads.AdActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {

    override fun createViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): SplashViewModel {
        return SplashViewModel()
    }

    override fun initViews(savedInstanceState: Bundle?) {
        updateProgressText(0)
        TrackMgr.instance.trackEvent(TrackEventType.SESSION_START)
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_ad)
        if (intent?.extras?.getString("from") != "Background"){
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_ae)
        }else{
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_af)
        }
    }

    override fun initListeners() {
        mViewModel.progress.observe(this, Observer { progress ->
            mBind.progress.progress = progress
            updateProgressText(progress)
        })

        mViewModel.isLoadingComplete.observe(this, Observer { isComplete ->
            if (!isComplete) return@Observer
            mBind.loadingProgress.visibility = View.INVISIBLE
            mBind.progress.visibility = View.INVISIBLE
            TrackMgr.instance.trackAdEvent(AdPosition.LOADING, AdType.APP_OPEN, TrackEventType.safedddd_bg)
            if (mViewModel.isAdLoaded.value == true) {
                lifecycleScope.launch {
                    AdMgr.INSTANCE.showAd(
                        AdPosition.LOADING,
                        AdType.APP_OPEN,
                        this@SplashActivity,
                        onShowResult={ position, adType, success, error->
                            val suc = if (success) "成功" else "失败"
                            LogUtils.d("广告: 展示$suc ${error?.message}${error?.domain}")
                            if (success){
                                mViewModel.preloadAd(this@SplashActivity)
                            }
                            mViewModel.preloadBatchAds(this@SplashActivity)
                        },
                        onAdDismissed = { position, adType ->
                            route()
                        })
                }
                return@Observer
            }
            route()
        })
    }
    private fun route(){
        if (intent?.extras?.getString("from") != "Background") {
            if (!AppCache.isSelectLng) {
                startActivity<LanguageActivity>()
            } else if (!AppCache.guideShow) {
                startActivity<GuideActivity> {
                    putExtra("from", "splash")
                }
            } else {
                startActivity<MainActivity>()
            }
        }
        finish()
    }
    override fun onResume() {
        super.onResume()
        mViewModel.preloadBatchAds(this)
        mViewModel.preloadAd(this)
        mViewModel.startLoading {
            AdMgr.INSTANCE.getAdLoadState(AdPosition.LOADING, AdType.APP_OPEN) == AdLoadState.LOADED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.resetAfterAdClosed()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onAppEnterForeground() {
        super.onAppEnterForeground()
    }

    override fun onAppEnterBackground() {
        super.onAppEnterBackground()
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                delay(200)

                val topActivity = ActivityManager.currentActivity()
                if (topActivity is AdActivity) {
                    topActivity.finish()
                }
            }
        }
    }

    private fun updateProgressText(progress: Int) {
        val progressText =
            String.format(App.getAppContext().getString(R.string.loading_progress), progress)
        if (!mBind.progress.isVisible){
            mBind.progress.visibility = View.VISIBLE
        }
        if (!mBind.loadingProgress.isVisible){
            mBind.loadingProgress.visibility = View.VISIBLE
        }
        mBind.loadingProgress.text = progressText
    }

    override fun handleBackPressed(): Boolean {
        return true // 拦截返回事件
    }
}