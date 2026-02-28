package com.download.video_download.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import com.download.video_download.R
import com.download.video_download.base.BaseActivity
import com.download.video_download.databinding.ActivityMainBinding
import com.download.video_download.ui.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.size
import androidx.core.view.get
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.download.video_download.App
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.model.SearchState
import com.download.video_download.ui.fragment.DownloadFragment
import com.download.video_download.ui.fragment.HomeFragment
import com.download.video_download.ui.fragment.PlayerFragment
import com.download.video_download.ui.fragment.WebChromeFragment
import com.download.video_download.ui.fragment.WebFragment
import com.download.video_download.ui.fragment.WebGuideFragment
import com.download.video_download.ui.fragment.WebHistoryFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : BaseActivity< MainViewModel, ActivityMainBinding>()  {
    val viewModel: MainViewModel by viewModels()
    private val fragmentCache = HashMap<Int, Fragment>()
    private var currentFragment: Fragment? = null
    private var trackJob: kotlinx.coroutines.Job? = null
    private var isTabClick = true
    private var sessionTime: Long = 0
    override fun createViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): MainViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        mBind.navBottom.selectedItemId = R.id.nav_home
        loadFragment(R.id.nav_home)
        mBind.navBottom.itemIconTintList = null
        setStatusBarMode(true)
        trackJob?.cancel()
        trackJob = lifecycleScope.launch {
            while (isActive) { // isActive
                delay( 60 * 1000L)
                TrackMgr.instance.trackEvent(
                    TrackEventType.safedddd_ac,
                    mutableMapOf("safedddd" to "2")
                )
            }
        }
        trackJob?.start()
    }

    override fun initListeners() {
        mBind.navBottom.setOnItemSelectedListener { item ->
            if (item.itemId == mBind.navBottom.selectedItemId){
                return@setOnItemSelectedListener  true
            }

            if (!isTabClick){
                isTabClick = true
                loadFragment(item.itemId)
                return@setOnItemSelectedListener  true
            }
            TrackMgr.instance.trackAdEvent(AdPosition.TAB, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)
            val cache = AdMgr.INSTANCE.getAdLoadState(AdPosition.TAB, AdType.INTERSTITIAL) == AdLoadState.LOADED
            if (cache) {
                lifecycleScope.launch {
                    AdMgr.INSTANCE.showAd(AdPosition.TAB, AdType.INTERSTITIAL,this@MainActivity,
                        onShowResult = { position, adType, success, error->
                            if (error?.code == -2){
                                loadFragment(item.itemId)
                            }
                        }, onAdDismissed =  {position, adType->
                            viewModel.preloadTabAd(this@MainActivity)
                            loadFragment(item.itemId)
                        })
                }
                return@setOnItemSelectedListener true
            }
            viewModel.preloadTabAd(this@MainActivity)
            loadFragment(item.itemId)
            true
        }
        viewModel.nav.observe(this) { item ->
            if (item == null){
                return@observe
            }
            isTabClick = false
            when(item.route){
                NavState.HOME -> {
                    mBind.navBottom.selectedItemId = R.id.nav_home
                }
                NavState.SEARCH ->{
                    mBind.navBottom.selectedItemId = R.id.nav_search
                }
                NavState.DOWNLOAD ->{
                    mBind.navBottom.selectedItemId = R.id.nav_download
                }
                NavState.PLAYER ->{
                    mBind.navBottom.selectedItemId = R.id.nav_player
                }
            }
        }
    }
    private fun loadFragment(itemId: Int) {
        val targetFragment = fragmentCache.getOrPut(itemId) {
            when (itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_search -> WebFragment()
                R.id.nav_download -> DownloadFragment()
                R.id.nav_player -> PlayerFragment()
                else -> Fragment()
            }
        }
        if (currentFragment == targetFragment) {
            return
        }
        supportFragmentManager.beginTransaction().apply {
            currentFragment?.let {
                hide(it)
                setMaxLifecycle(it, Lifecycle.State.STARTED)
            }

            if (!targetFragment.isAdded) {
                add(mBind.container.id, targetFragment, itemId.toString())
            } else {
                show(targetFragment)
            }
            setMaxLifecycle(targetFragment, Lifecycle.State.RESUMED)

            commitNowAllowingStateLoss()
        }

        currentFragment = targetFragment
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTrack()
    }

    override fun handleBackPressed(): Boolean {
        return false
    }

    override fun onAppEnterForeground() {
        if (!viewModel.isFromPermissionBack){
            super.onAppEnterForeground()
        }
        viewModel.isFromPermissionBack = false
    }
    private fun stopTrack() {
        trackJob?.cancel()
        trackJob = null
    }

    override fun onResume() {
        super.onResume()
        val curTime = System.currentTimeMillis()
        if (curTime - sessionTime > 30 * 1000) {
            sessionTime = curTime
            TrackMgr.instance.trackEvent(TrackEventType.SESSION)
        }
    }
}