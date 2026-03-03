package com.download.video_download.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.utils.DpUtils.dp2px
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.databinding.ActivityFeedbackBinding
import com.download.video_download.ui.adapter.FlexTagAdapter
import com.download.video_download.ui.viewmodel.FDViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlin.getValue

class FeedbackActivity:BaseActivity<FDViewModel, ActivityFeedbackBinding>() {
    val viewModel: FDViewModel by viewModels()
    override fun createViewBinding(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): FDViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        addFeedbackTag()
    }

    override fun initListeners() {
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
}