package com.download.video_download.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
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
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_long2)
    }

    override fun initListeners() {
        mBind.ivBack.setOnClickListener {
            ActivityManager.finishAllActivityExcept(this.javaClass)
            startActivity<MainActivity>()
            finish()
        }
        mBind.d.setOnClickListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_long3, mapOf("safeeeee" to "3"))
            startActivity<GuideActivity>()
            finish()
        }
        mBind.v.setOnClickListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_long3, mapOf("safeeeee" to "4"))
            ActivityManager.finishAllActivityExcept(this.javaClass)
            startActivity<MainActivity>(){
                putExtra("param", "vp_ins")
            }
        }
        mBind.keep.setOnClickListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_long3, mapOf("safeeeee" to "2"))
            ActivityManager.finishAllActivityExcept(this.javaClass)
            startActivity<MainActivity>()
            finish()
        }
        mBind.still.setOnClickListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_long3, mapOf("safeeeee" to "1"))
            startActivity<FeedbackActivity>()
            finish()
        }
    }
    override fun handleBackPressed(): Boolean {
        ActivityManager.finishAllActivityExcept(this.javaClass)
        startActivity<MainActivity>()
        return super.handleBackPressed()
    }
}