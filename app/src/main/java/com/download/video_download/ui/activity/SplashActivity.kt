package com.download.video_download.ui.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.ext.startActivity
import com.download.video_download.databinding.ActivitySplashBinding
import com.download.video_download.ui.viewmodel.SplashViewModel

class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {
    
    override fun createViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): SplashViewModel {
        return SplashViewModel()
    }

    override fun initViews(savedInstanceState: Bundle?) {
        updateProgressText(0)
    }

    override fun initListeners() {
        mViewModel.progress.observe(this, Observer { progress ->
            mBind.progress.progress = progress
            updateProgressText(progress)
        })
        mViewModel.isLoadingComplete.observe(this, Observer { isComplete ->
            if (isComplete) {
                 startActivity<MainActivity>()
                 finish()
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        mViewModel.startLoading()
    }

    private fun updateProgressText(progress: Int) {
        val loadingText = App.getAppContext().resources.getString(
            R.string.loading_progress,
            progress
        )
        mBind.loadingProgress.text = loadingText
    }

    override fun handleBackPressed(): Boolean {
        return true
    }
}