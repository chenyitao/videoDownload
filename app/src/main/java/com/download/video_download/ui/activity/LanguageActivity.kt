package com.download.video_download.ui.activity

import LanguageViewModel
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.ext.startActivityWithExtras
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LanguageUtils
import com.download.video_download.databinding.ActivityLanguageBinding
import com.download.video_download.ui.adapter.LanguageAdapter
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
            if (!AppCache.guideShow) {
                startActivity<GuideActivity>()
            }else{
                startActivity<MainActivity>()
            }
            finish()
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
    }

    override fun initListeners() {
        mBind.nextBtn.setOnClickListener {
            AppCache.isSelectLng = true
            if (!AppCache.guideShow) {
                startActivityWithExtras<GuideActivity>(extras = {
                    putString("from", "language")
                })
            }else{
                startActivity<MainActivity>()
            }
            finish()
        }
    }

    override fun handleBackPressed(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mBind.lottieIcon.cancelAnimation()
    }

    private fun updateText() {
        mBind.nextBtn.text = App.getAppContext().getString(R.string.next)
        mBind.tvLg.text = App.getAppContext().getString(R.string.language)
    }
}