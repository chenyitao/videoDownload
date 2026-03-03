package com.download.video_download.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.utils.ActivityManager
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
        mBind.ivBack.setOnClickListener {
            ActivityManager.finishAllActivity()
            startActivity<MainActivity>()
        }
        mBind.d.setOnClickListener {
            startActivity<GuideActivity>()
            finish()
        }
        mBind.v.setOnClickListener {
            ActivityManager.finishAllActivity()
            startActivity<MainActivity>(){
                putExtra("param", "vp")
            }
        }
        mBind.keep.setOnClickListener {
            startActivity<MainActivity>(){
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            finish()
        }
        mBind.still.setOnClickListener {
            startActivity<FeedbackActivity>()
            finish()
        }
    }
    override fun handleBackPressed(): Boolean {
        startActivity<MainActivity>{
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return super.handleBackPressed()
    }
}