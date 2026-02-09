package com.download.video_download.base.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

/**
 * 状态栏工具类
 */
object StatusBarUtils {
    
    /**
     * 设置状态栏透明
     */
    fun setTransparentStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = Color.TRANSPARENT
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }
    
    /**
     * 设置状态栏颜色
     */
    fun setStatusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = color
        }
    }
    
    /**
     * 设置状态栏文字颜色为深色（适用于浅色背景）
     */
    fun setStatusBarLightMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                )
        }
    }
    
    /**
     * 设置状态栏文字颜色为浅色（适用于深色背景）
     */
    fun setStatusBarDarkMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }
    
    /**
     * 隐藏状态栏
     */
    fun hideStatusBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }
    
    /**
     * 显示状态栏
     */
    fun showStatusBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
    
    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(activity: Activity): Int {
        var result = 0
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = activity.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}