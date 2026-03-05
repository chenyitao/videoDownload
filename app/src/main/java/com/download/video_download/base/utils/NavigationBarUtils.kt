package com.download.video_download.base.utils

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat

object NavigationBarUtils {
    fun hasNavigationBar(activity: Activity): Boolean {
        val resources = activity.resources
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (id > 0) {
            resources.getBoolean(id)
        } else {
            false
        }
    }

    fun getNavigationBarHeight(activity: Activity): Int {
        var result = 0
        val resources = activity.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun getNavigationBarWidth(activity: Activity): Int {
        var result = 0
        val resources = activity.resources
        val resourceId = resources.getIdentifier("navigation_bar_width", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun setImmersiveNavigationBar(activity: Activity) {
        val window = activity.window
        val decorView = window.decorView

        // 1. 设置导航栏为透明（保留你的核心需求）
        window.navigationBarColor = Color.TRANSPARENT

        // 2. 关键：获取当前已有的标志位，追加布局相关标志（而非直接赋值）
        var uiFlags = decorView.systemUiVisibility
        // 追加布局稳定标志
        uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        // 追加布局延伸到导航栏标志
        uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        // 追加布局延伸到状态栏标志
        uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // 3. 重新设置（保留所有原有标志，仅新增布局相关）
        decorView.systemUiVisibility = uiFlags
    }

    fun setNavigationBarTransparent(activity: Activity) {
        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    fun setNavigationBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.navigationBarColor = color
        }
    }

    fun setNavigationBarLightMode(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
            activity.window.decorView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            )
    }

    fun setNavigationBarDarkMode(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
            activity.window.decorView.systemUiVisibility
                and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            )
    }

    fun hideNavigationBar(activity: Activity) {
        val window = activity.window
        val decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = decorView.windowInsetsController
            controller?.let {
                it.hide(WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            window.setDecorFitsSystemWindows(false)
        } else {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
    private fun handleSamsungNavigationBar(window: Window) {
        try {
            // 三星设备特有的导航栏隐藏参数
            val params = window.attributes
            val field = params.javaClass.getField("navigationBarColor")
            field.set(params, 0x00000000) // 设置透明
            window.attributes = params

            // 强制刷新窗口
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        } catch (e: Exception) {
            // 忽略反射异常，兼容非三星设备
            e.printStackTrace()
        }
    }
    fun showNavigationBar(activity: Activity) {
        val window = activity.window
        val decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.windowInsetsController?.show(WindowInsets.Type.navigationBars())
            window.setDecorFitsSystemWindows(true)
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    fun isNavigationBarVisible(activity: Activity): Boolean {
        return activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0
    }

    fun getSafeAreaRect(activity: Activity): Rect {
        val rect = Rect()
        val decorView = activity.window.decorView
        decorView.getWindowVisibleDisplayFrame(rect)
        return rect
    }
}