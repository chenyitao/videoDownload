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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.download.video_download.App
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.SearchState
import com.download.video_download.ui.fragment.DownloadFragment
import com.download.video_download.ui.fragment.HomeFragment
import com.download.video_download.ui.fragment.PlayerFragment
import com.download.video_download.ui.fragment.WebChromeFragment
import com.download.video_download.ui.fragment.WebFragment
import com.download.video_download.ui.fragment.WebGuideFragment
import com.download.video_download.ui.fragment.WebHistoryFragment

class MainActivity : BaseActivity< MainViewModel, ActivityMainBinding>()  {
    val viewModel: MainViewModel by viewModels()
    private val fragmentCache = HashMap<Int, Fragment>()
    private var currentFragment: Fragment? = null
    override fun createViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): MainViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        mBind.navBottom.selectedItemId = R.id.nav_home
        loadFragment(R.id.nav_home)
        mBind.navBottom.itemIconTintList = null
        setStatusBarMode(true)
    }

    override fun initListeners() {
        mBind.navBottom.setOnItemSelectedListener { item ->
            loadFragment(item.itemId)
            true
        }
        viewModel.nav.observe(this) { item ->
            if (item == null){
                return@observe
            }
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
    }
}