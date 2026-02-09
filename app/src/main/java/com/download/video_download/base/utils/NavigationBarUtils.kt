package com.download.video_download.base.utils

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager

/**
 * 虚拟导航栏工具类
 */
object NavigationBarUtils {
    
    /**
     * 检查是否有虚拟导航栏
     */
    fun hasNavigationBar(activity: Activity): Boolean {
        val resources = activity.resources
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (id > 0) {
            resources.getBoolean(id)
        } else {
            false
        }
    }
    
    /**
     * 获取导航栏高度
     */
    fun getNavigationBarHeight(activity: Activity): Int {
        var result = 0
        val resources = activity.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
    
    /**
     * 获取导航栏宽度（横屏时）
     */
    fun getNavigationBarWidth(activity: Activity): Int {
        var result = 0
        val resources = activity.resources
        val resourceId = resources.getIdentifier("navigation_bar_width", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
    
    /**
     * 设置沉浸式导航栏
     */
    fun setImmersiveNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }
    
    /**
     * 设置导航栏颜色
     */
    fun setNavigationBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.navigationBarColor = color
        }
    }
    
    /**
     * 设置导航栏文字颜色为深色（适用于浅色背景）
     */
    fun setNavigationBarLightMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.window.decorView.systemUiVisibility = (
                activity.window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                )
        }
    }
    
    /**
     * 设置导航栏文字颜色为浅色（适用于深色背景）
     */
    fun setNavigationBarDarkMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.window.decorView.systemUiVisibility = (
                activity.window.decorView.systemUiVisibility
                    and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                )
        }
    }
    
    /**
     * 隐藏导航栏
     */
    fun hideNavigationBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
            activity.window.decorView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }
    
    /**
     * 显示导航栏
     */
    fun showNavigationBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
            activity.window.decorView.systemUiVisibility
                and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
                and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
            )
    }
    
    /**
     * 检查导航栏是否可见
     */
    fun isNavigationBarVisible(activity: Activity): Boolean {
        return activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0
    }
    
    /**
     * 获取屏幕安全区域（考虑导航栏）
     */
    fun getSafeAreaRect(activity: Activity): Rect {
        val rect = Rect()
        val decorView = activity.window.decorView
        decorView.getWindowVisibleDisplayFrame(rect)
        return rect
    }
}