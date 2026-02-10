package com.download.video_download.ui.activity

import android.os.Bundle
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
import com.download.video_download.ui.fragment.DownloadFragment
import com.download.video_download.ui.fragment.HomeFragment
import com.download.video_download.ui.fragment.PlayerFragment
import com.download.video_download.ui.fragment.WebFragment

class MainActivity : BaseActivity< MainViewModel, ActivityMainBinding>() {
    val viewModel: MainViewModel by viewModels()
    override fun createViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): MainViewModel  = viewModel

    override fun initViews(savedInstanceState: Bundle?) {
        mBind.navBottom.selectedItemId = R.id.nav_home
        mBind.navBottom.itemIconTintList = null
        setStatusBarMode(true)
    }

    override fun initListeners() {
        mBind.navBottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(mBind.container.id, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_search -> {
                    supportFragmentManager.beginTransaction()
                        .replace(mBind.container.id, WebFragment())
                        .commit()
                    true
                }
                R.id.nav_download -> {
                    supportFragmentManager.beginTransaction()
                        .replace(mBind.container.id, DownloadFragment())
                        .commit()
                    true
                }
                R.id.nav_player -> {
                    supportFragmentManager.beginTransaction()
                        .replace(mBind.container.id, PlayerFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}