package com.download.video_download.base.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

object StatusBarUtils {

    fun setTransparentStatusBar(activity: Activity) {
        val window = activity.window
        val decorView = window.decorView
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6.0+：透明状态栏 + 浅色状态栏文字（适配浅色背景）
                window.statusBarColor = Color.TRANSPARENT
                // 只添加/保留状态栏相关标志位，不覆盖原有值
                var uiFlags = decorView.systemUiVisibility
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 布局延伸到状态栏
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE   // 布局稳定
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 浅色状态栏文字
                decorView.systemUiVisibility = uiFlags
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                // Android 5.0-5.1：透明状态栏
                window.statusBarColor = Color.TRANSPARENT
                var uiFlags = decorView.systemUiVisibility
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                decorView.systemUiVisibility = uiFlags
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                // Android 4.4-4.4.4：半透明状态栏
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    fun setStatusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = color
        }
    }

    fun setStatusBarLightMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = activity.window.decorView
            // 关键：获取当前已有的标志位，再追加需要的标志，而不是直接赋值
            var uiFlags = decorView.systemUiVisibility
            // 追加全屏布局标志（保持状态栏透明的基础）
            uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // 追加浅色状态栏文字标志（核心需求）
            uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            // 重新设置（保留所有原有标志位，只新增需要的）
            decorView.systemUiVisibility = uiFlags
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