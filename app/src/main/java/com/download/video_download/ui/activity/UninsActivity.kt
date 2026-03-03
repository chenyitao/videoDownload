package com.download.video_download.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.download.video_download.base.BaseActivity
import com.download.video_download.databinding.ActivityUninsBinding
import com.download.video_download.ui.viewmodel.UninsViewModel
import kotlin.getValue

class UninsActivity:BaseActivity<UninsViewModel, ActivityUninsBinding>() {
    val viewModel: UninsViewModel by viewModels()
    override fun createViewBinding(): ActivityUninsBinding {
        return ActivityUninsBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): UninsViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
    }

    override fun initListeners() {
    }
}