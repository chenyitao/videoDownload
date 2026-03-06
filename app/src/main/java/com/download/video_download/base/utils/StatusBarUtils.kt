package com.download.video_download.base.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

object StatusBarUtils {

    fun setTransparentStatusBar(activity: Activity, isLightText: Boolean = true) {
        val window = activity.window
        val decorView = window.decorView
        val contentView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val rootView = contentView.getChildAt(0) // Activity的根布局

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                window.statusBarColor = Color.TRANSPARENT
                var uiFlags = decorView.systemUiVisibility
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                if (isLightText) {
                    uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    uiFlags = uiFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                decorView.systemUiVisibility = uiFlags

            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                window.statusBarColor = Color.TRANSPARENT
                var uiFlags = decorView.systemUiVisibility
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                decorView.systemUiVisibility = uiFlags

            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }

        if (rootView != null) {
            rootView.fitsSystemWindows = false
            rootView.setPadding(0, 0, 0, rootView.paddingBottom)
        }
    }

    fun setStatusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = color
        }
    }

    fun setStatusBarLightMode(activity: Activity, isLightMode: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = activity.window
            val decorView = window.decorView
            window.statusBarColor = Color.TRANSPARENT
            var uiFlags = decorView.systemUiVisibility
            uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            if (isLightMode) {
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                uiFlags = uiFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }

            decorView.systemUiVisibility = uiFlags
            val contentView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
            val rootView = contentView.getChildAt(0)
            rootView?.let {
                it.fitsSystemWindows = false
            }
        }
    }

    fun setStatusBarDarkMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }

    fun hideStatusBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }

    fun showStatusBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    fun getStatusBarHeight(activity: Activity): Int {
        var result = 0
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = activity.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}