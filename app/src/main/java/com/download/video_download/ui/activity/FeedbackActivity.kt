package com.download.video_download.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
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
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.task.DownloadTaskManager
import com.download.video_download.base.utils.ActivityManager
import com.download.video_download.base.utils.DpUtils.dp2px
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.databinding.ActivityFeedbackBinding
import com.download.video_download.ui.adapter.FlexTagAdapter
import com.download.video_download.ui.dialog.DownloadCancelDialog
import com.download.video_download.ui.dialog.DownloadStatusDialog
import com.download.video_download.ui.dialog.FDDialog
import com.download.video_download.ui.dialog.isFragmentShowing
import com.download.video_download.ui.viewmodel.FDViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.launch
import kotlin.getValue

class FeedbackActivity:BaseActivity<FDViewModel, ActivityFeedbackBinding>() {
    val viewModel: FDViewModel by viewModels()
    var fd: FDDialog? = null
    override fun createViewBinding(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): FDViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_long4)
        addFeedbackTag()
    }

    override fun initListeners() {
        mBind.ivBack.setOnClickListener {
            ActivityManager.finishAllActivityExcept(this.javaClass)
            startActivity<MainActivity>()
            finish()
        }
        mBind.keep.setOnClickListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_long5, mapOf("safeeeee" to "2"))
            ActivityManager.finishAllActivityExcept(this.javaClass)
            startActivity<MainActivity>()
            finish()
        }
        mBind.still.setOnClickListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_long5, mapOf("safeeeee" to "1"))
            if (fd?.isFragmentShowing() == true) {
                return@setOnClickListener
            }
            fd = FDDialog().apply {
                setOnKeep {
                    TrackMgr.instance.trackEvent(TrackEventType.safedddd_long7, mapOf("safeeeee" to "2"))
                    ActivityManager.finishAllActivityExcept(this.javaClass)
                    startActivity<MainActivity>()
                    finish()
                }
                setOnStill {
                    TrackMgr.instance.trackEvent(TrackEventType.safedddd_long7, mapOf("safeeeee" to "1"))
                    App.isJumpingToSystemSetting = true
                    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS").apply {
                        data = "package:${requireContext().packageName}".toUri()
                        addCategory(Intent.CATEGORY_DEFAULT)
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    activityLauncher.launch(intent)
                }
            }
            fd?.show(supportFragmentManager, "fd")
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_long6)
        }
    }
    private fun addFeedbackTag(){
        val tags = viewModel.getFeedbackData()
        val flexLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }
        mBind.rvFlex.layoutManager = flexLayoutManager
        val  adapter = FlexTagAdapter({
            if (it.title == App.getAppContext().getString(R.string.other_problem)){
                mBind.jdInput.visibility = View.VISIBLE
            }else{
                mBind.jdInput.visibility = View.GONE
            }
        })
        mBind.rvFlex.adapter = adapter
        adapter.updateData(tags)
    }

    override fun handleBackPressed(): Boolean {
        ActivityManager.finishAllActivityExcept(this.javaClass)
        startActivity<MainActivity>()
        return super.handleBackPressed()
    }
    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        ActivityManager.finishAllActivityExcept(this.javaClass)
        startActivity<MainActivity>()
        finish()
    }
}