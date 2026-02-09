package com.download.video_download.base.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * 软键盘工具类
 */
object KeyboardUtils {
    
    /**
     * 显示软键盘
     */
    fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    
    /**
     * 隐藏软键盘
     */
    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus ?: activity.findViewById(android.R.id.content)
        hideKeyboard(view)
    }
    
    /**
     * 隐藏软键盘（针对特定View）
     */
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    
    /**
     * 切换软键盘显示状态
     */
    fun toggleKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
    
    /**
     * 检查软键盘是否显示
     */
    fun isKeyboardShowing(activity: Activity): Boolean {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.isActive
    }
}