package com.download.video_download.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.databinding.ActivityLanguageBinding
import com.download.video_download.ui.viewmodel.LanguageViewModel

class LanguageActivity : BaseActivity<LanguageViewModel, ActivityLanguageBinding>() {
    val viewModel: LanguageViewModel by viewModels()
    override fun createViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): LanguageViewModel =  viewModel

    override fun initViews(savedInstanceState: Bundle?) {
    }

    override fun initListeners() {
    }
}