package com.download.video_download.ui.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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
import com.download.video_download.base.model.GuideData
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.DpUtils
import com.download.video_download.base.utils.DpUtils.dp2px
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.base.utils.StringUtils.boldTargetSubStr
import com.download.video_download.databinding.ActivityGuideBinding
import com.download.video_download.ui.viewmodel.GuideViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuideActivity : BaseActivity< GuideViewModel, ActivityGuideBinding>() {
    val viewModel: GuideViewModel by viewModels()
    var guideList: MutableList<GuideData> = mutableListOf()
    private var param = ""
    private var currentPage = 0
    private var isForbidRightScroll = true
    private var from = ""
    private var Job: kotlinx.coroutines.Job? = null
    private var isNext = false
    override fun createViewBinding(): ActivityGuideBinding {
        return ActivityGuideBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): GuideViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        TrackMgr.instance.trackEvent(TrackEventType.SESSION_START)
        from = intent.getStringExtra("from")?:""
        param = intent?.extras?.getString("param") ?: ""
        guideList = viewModel.getGuideList(from?:"")
        mBind.viewPager.adapter = ViewPager2Adapter(guideList)
        TabLayoutMediator(mBind.tabLayout, mBind.viewPager) { tab, position ->
        }.attach()
        currentPage = mBind.viewPager.currentItem
        mBind.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                var safeddddA = 0
                if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
                    safeddddA = 1
                }else{
                    safeddddA = 2
                }
                TrackMgr.instance.trackEvent(TrackEventType.safedddd_new1, mapOf("safedddd" to position+1,"safeddddA" to safeddddA))
                if (position == 2){
                    mBind.skip.visibility = View.INVISIBLE
                }else{
                    mBind.skip.visibility = View.VISIBLE
                    if (intent.getStringExtra("from") == "language"
                        || intent.getStringExtra("from") == "splash"){
                        if (position == guideList.size-1){
                            if (!isNext){
                                TrackMgr.instance.trackAdEvent(AdPosition.GUIDE, AdType.NATIVE, TrackEventType.safedddd_bg)
                            }else{
                                isNext = false
                            }
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) {
                                    delay(200)
                                    viewModel.handleNativeAd( this@GuideActivity)
                                }
                            }
                        }
                    }
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position < currentPage){
                    mBind.viewPager.setCurrentItem(position+1,false)
                    currentPage = position+1
                }else{
                    currentPage = position
                }
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
        var paramsLayout = mBind.viewPager.layoutParams
        if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
            viewModel.handInvestAd(this)
            paramsLayout.height = DpUtils.dp2px(this,320f)
        }else{
            paramsLayout.height = DpUtils.dp2px(this,360f)
        }
        mBind.viewPager.layoutParams = paramsLayout
        if (hasNavigationBar()){
            if (isNavigationBarHidden){
                mBind.container.setPadding(0,0,0,0)
            }else{
                val h = getNavigationBarHeight()
                mBind.container.setPadding(0,0,0,h)
            }
        }
    }

    override fun initListeners() {
        mBind.skip.setOnClickListener {
            val safeddddA = if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
                1
            }else{
                2
            }
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_new3, mapOf("safedddd" to  mBind.viewPager.currentItem+1,"safeddddA" to safeddddA))
            if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
                TrackMgr.instance.trackAdEvent(AdPosition.GUIDE, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)
                val hascache = AdMgr.INSTANCE.getAdLoadState(AdPosition.GUIDE, AdType.INTERSTITIAL) == AdLoadState.LOADED
                if (hascache){
                    lifecycleScope.launch {
                        AdMgr.INSTANCE.showAd(AdPosition.GUIDE , AdType.INTERSTITIAL,this@GuideActivity,
                            onShowResult = { position, adType, success, error->
                            }, onAdDismissed = { position, adType ->
                                startActivity<MainActivity>(){
                                    putExtra("param", param)
                                }
                                AppCache.guideShow = true
                                finish()
                            })
                    }
                    return@setOnClickListener
                }
            }
            startActivity<MainActivity>(){
                putExtra("param", param)
            }
            AppCache.guideShow = true
            finish()
        }
        mBind.next.setOnClickListener {
            val safeddddA = if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
                1
            }else{
                2
            }
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_new2, mapOf("safedddd" to  mBind.viewPager.currentItem+1,"safeddddA" to safeddddA))
            if (mBind.viewPager.currentItem == guideList.size - 1){
                if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
                    TrackMgr.instance.trackAdEvent(AdPosition.GUIDE, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)
                    val hascache = AdMgr.INSTANCE.getAdLoadState(AdPosition.GUIDE, AdType.INTERSTITIAL) == AdLoadState.LOADED
                    if (hascache){
                        lifecycleScope.launch {
                            AdMgr.INSTANCE.showAd(AdPosition.GUIDE , AdType.INTERSTITIAL,this@GuideActivity,
                                onShowResult = { position, adType, success, error->
                                }, onAdDismissed = { position, adType ->
                                    startActivity<MainActivity>(){
                                        putExtra("param", param)
                                    }
                                    AppCache.guideShow = true
                                    finish()
                                })
                        }
                        return@setOnClickListener
                    }
                }
                startActivity<MainActivity>(){
                    putExtra("param", param)
                }
                AppCache.guideShow = true
                finish()
                return@setOnClickListener
            }
            isNext = true
            mBind.viewPager.currentItem += 1
        }
        viewModel.isAdLoaded.observe(this, Observer { isLoaded ->
            if (!isLoaded) return@Observer
            lifecycleScope.launch {
                AdMgr.INSTANCE.showAd(AdPosition.GUIDE , AdType.NATIVE,this@GuideActivity,
                    onShowResult = { position, adType, success, error->
                        if (success){
                            AdMgr.INSTANCE.getNativeAd( position)?.let {
                                mBind.adFrameLayout.visibility = View.VISIBLE
                                mBind.adFrameLayout.setNativeAd(it,this@GuideActivity)
                            }
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) {
                                    delay(200)
                                    if ( mBind.viewPager.currentItem == 0){
                                        viewModel.preNativeAd( this@GuideActivity)
                                    }
                                }
                            }
                        }
                    })
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
            Job?.cancel()
            Job = lifecycleScope.launch {
                delay(200)
                TrackMgr.instance.trackAdEvent(AdPosition.GUIDE, AdType.NATIVE, TrackEventType.safedddd_bg)
                viewModel.handleNativeAd(this@GuideActivity)
            }
            Job?.start()

        }
    }
    override fun onPause() {
        super.onPause()
        Job?.cancel()
        Job = null
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
            var paramsLayout = holder.tvImage.layoutParams
            if (pageTitles.size ==2){
                paramsLayout.height = dp2px(App.getAppContext(), 270f)
            }else{
                paramsLayout.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            holder.tvImage.layoutParams = paramsLayout
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
        if (intent.getStringExtra("from") == "language" || intent.getStringExtra("from") == "splash"){
            mBind.adFrameLayout.releaseAd()
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent( intent)
        from = intent.getStringExtra("from")?:""
        param = intent?.extras?.getString("param") ?: ""
    }
}