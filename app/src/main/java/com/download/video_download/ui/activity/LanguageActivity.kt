package com.download.video_download.ui.activity

import LanguageViewModel
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.block.AdShowCallback
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.ext.startActivityWithExtras
import com.download.video_download.base.utils.ActivityManager
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LanguageUtils
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.databinding.ActivityLanguageBinding
import com.download.video_download.ui.adapter.LanguageAdapter
import com.google.android.gms.ads.AdActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LanguageActivity : BaseActivity<LanguageViewModel, ActivityLanguageBinding>() {
    lateinit var adapter :LanguageAdapter
    val viewModel: LanguageViewModel by viewModels()
    override fun createViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): LanguageViewModel =  viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        mBind.rvLanguage.layoutManager = LinearLayoutManager(this)
        adapter = LanguageAdapter {
            if (it.language == "Use system language"){
                it.languageCode = viewModel.getSystemLanguageCode()
            }
            AppCache.switchLanguage = it.languageCode
            AppCache.switchLanguageName = it.language
            LanguageUtils.setAppLanguage(this,it.languageCode)
            updateText()
           if (!it.isSelected){
               return@LanguageAdapter
           }
            AppCache.isSelectLng = true
            handleGoNext()
        }
        mBind.rvLanguage.adapter = adapter
        adapter.updateData(viewModel.getLanguageList())
        mBind.lottieIcon.setAnimation("lottie/data.json")
        mBind.lottieIcon.imageAssetsFolder = "lottie/images"
        mBind.lottieIcon.playAnimation()
        updateText()
        if (hasNavigationBar()){
            val h = getNavigationBarHeight()
            mBind.container.setPadding(0,0,0,h)
        }
        viewModel.handInvestAd(this)
    }

    override fun initListeners() {
        mBind.nextBtn.setOnClickListener {
            AppCache.isSelectLng = true
            handleGoNext()
        }
        viewModel.isAdLoaded.observe(this, Observer { isLoaded ->
            if (!isLoaded) return@Observer
            lifecycleScope.launch {
                AdMgr.INSTANCE.showAd(AdPosition.LANGUAGE, AdType.NATIVE,this@LanguageActivity,
                    onShowResult = { position, adType, success, error->
                        if (success){
                            AdMgr.INSTANCE.getNativeAd( position)?.let {
                                mBind.adFrameLayout.visibility = View.VISIBLE
                                mBind.adFrameLayout.setNativeAd(it,this@LanguageActivity)
                            }
                        }
                    })
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.handleNativeAd(this)
    }

    override fun handleBackPressed(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mBind.lottieIcon.cancelAnimation()
        mBind.adFrameLayout.releaseAd()
    }

    private fun updateText() {
        mBind.nextBtn.text = App.getAppContext().getString(R.string.next)
        mBind.tvLg.text = App.getAppContext().getString(R.string.language)
    }

    private fun handleGoNext(){
        lifecycleScope.launch {
            val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.LANGUAGE, AdType.INTERSTITIAL) == AdLoadState.LOADED
            if (hasCache){
                AdMgr.INSTANCE.showAd(AdPosition.LANGUAGE, AdType.INTERSTITIAL,this@LanguageActivity,
                    onShowResult = { position, adType, success, error->
                        LogUtils.d("广告:  ${error?.message}${error?.domain}")
                    },onAdDismissed = { position, adType ->
                        jump()
                    })
                return@launch
            }
            jump()
        }
    }
    private fun jump(){
        if (!AppCache.guideShow) {
            startActivity<GuideActivity>()
        }else{
            startActivity<MainActivity>()
        }
        finish()
    }
}