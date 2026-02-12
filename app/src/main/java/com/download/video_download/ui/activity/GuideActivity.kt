package com.download.video_download.ui.activity

import android.R.attr.startX
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.model.GuideData
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.DpUtils.dp2px
import com.download.video_download.base.utils.StringUtils.boldTargetSubStr
import com.download.video_download.databinding.ActivityGuideBinding
import com.download.video_download.ui.viewmodel.GuideViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

class GuideActivity : BaseActivity< GuideViewModel, ActivityGuideBinding>() {
    val viewModel: GuideViewModel by viewModels()
    var guideList: MutableList<GuideData> = mutableListOf()
    private var currentPage = 0
    private var isForbidRightScroll = true
    override fun createViewBinding(): ActivityGuideBinding {
        return ActivityGuideBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): GuideViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        val from = intent.getStringExtra("from")
        guideList = viewModel.getGuideList(from?:"")
        mBind.viewPager.adapter = ViewPager2Adapter(guideList)
        TabLayoutMediator(mBind.tabLayout, mBind.viewPager) { tab, position ->
        }.attach()
        currentPage = mBind.viewPager.currentItem
        mBind.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2){
                    mBind.skip.visibility = View.INVISIBLE
                }else{
                    mBind.skip.visibility = View.VISIBLE
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (from != "language" && from != "splash"){
                    if (position < currentPage){
                        mBind.viewPager.setCurrentItem(position+1,false)
                        currentPage = position+1
                    }else{
                        currentPage = position
                    }
                }
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }

    override fun initListeners() {
        mBind.skip.setOnClickListener {
            startActivity<MainActivity>()
            AppCache.guideShow = true
            finish()
        }
        mBind.next.setOnClickListener {
            if (mBind.viewPager.currentItem == guideList.size - 1){
                startActivity<MainActivity>()
                AppCache.guideShow = true
                finish()
                return@setOnClickListener
            }
            mBind.viewPager.currentItem += 1
        }
    }
    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tv_title)
        val tvIntro: AppCompatTextView = itemView.findViewById(R.id.tv_intro)
        val tvImage: AppCompatImageView = itemView.findViewById(R.id.vp_image)
    }

    class ViewPager2Adapter(private val pageTitles: List<GuideData>) : RecyclerView.Adapter<PageViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_guide, parent, false)
            return PageViewHolder(view)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            holder.tvIntro.text = pageTitles[position].title
            holder.tvTitle.text = pageTitles[position].description
            holder.tvTitle.visibility = if (pageTitles[position].description.isNotEmpty()) View.VISIBLE else View.GONE
            holder.tvImage.setImageResource(pageTitles[position].image)
            if (position == 2){
                val boldText = boldTargetSubStr( pageTitles[position].title, App.getAppContext().getString(R.string.guide3_bold))
                holder.tvIntro.text = boldText
                holder.tvTitle.setPadding(dp2px(holder.tvIntro.context, 36f), 0, dp2px(holder.tvIntro.context, 36f), 0)
                holder.tvIntro.setPadding(dp2px(holder.tvIntro.context, 36f), 0, dp2px(holder.tvIntro.context, 36f), 0)
                holder.tvIntro.gravity = Gravity.START
                holder.tvIntro.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                holder.tvIntro.setTextColor(App.getAppContext().getColor(R.color.white))
                holder.tvIntro.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            }else{
                if (position == 0){
                    holder.tvIntro.setPadding(dp2px(holder.tvIntro.context, 52f), 0, dp2px(holder.tvIntro.context, 52f), 0)
                }else{
                    holder.tvIntro.setPadding(dp2px(holder.tvIntro.context, 40f), 0, dp2px(holder.tvIntro.context, 40f), 0)
                }
                holder.tvIntro.gravity = Gravity.CENTER
                holder.tvIntro.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                holder.tvIntro.setTextColor(App.getAppContext().getColor(R.color.white))
                holder.tvIntro.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
        }

        override fun getItemCount(): Int = pageTitles.size
    }

    override fun handleBackPressed(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}