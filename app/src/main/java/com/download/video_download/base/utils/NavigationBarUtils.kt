package com.download.video_download.base.utils

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager

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
        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
        activity.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
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
        activity.window.decorView.systemUiVisibility = (
            activity.window.decorView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }

    fun showNavigationBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
            activity.window.decorView.systemUiVisibility
                and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
                and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
            )
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