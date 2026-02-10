package com.download.video_download.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.databinding.ActivityGuideBinding
import com.download.video_download.ui.viewmodel.GuideViewModel

class GuideActivity : BaseActivity< GuideViewModel, ActivityGuideBinding>() {
    val viewModel: GuideViewModel by viewModels()
    override fun createViewBinding(): ActivityGuideBinding {
        return ActivityGuideBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): GuideViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
    }

    override fun initListeners() {
    }

}