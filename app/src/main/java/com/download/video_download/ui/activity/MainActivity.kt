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
import com.download.video_download.App
import com.download.video_download.ui.fragment.DownloadFragment
import com.download.video_download.ui.fragment.HomeFragment
import com.download.video_download.ui.fragment.PlayerFragment
import com.download.video_download.ui.fragment.WebFragment

class MainActivity : BaseActivity< MainViewModel, ActivityMainBinding>()  {
    val viewModel: MainViewModel by viewModels()
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
    }
    private fun loadFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_search -> WebFragment()
            R.id.nav_download -> DownloadFragment()
            R.id.nav_player -> PlayerFragment()
            else -> null
        }
        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(mBind.container.id, it)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}