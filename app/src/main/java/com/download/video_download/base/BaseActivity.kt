package com.download.video_download.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.os.LocaleListCompat
import com.download.video_download.base.ext.showToast
import com.download.video_download.base.utils.*
import java.util.Locale

abstract class BaseActivity : Activity() {
    
    protected open val enableImmersiveStatusBar: Boolean = true
    protected open val enableImmersiveNavigationBar: Boolean = true
    protected open val enableAutoHideKeyboard: Boolean = true
    protected open fun onLanguageChanged() {}
    protected open fun onConfigurationChanged() {}
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageUtils.applyLanguageConfiguration(this)
        initImmersiveUI()
        initViews()
        initData()
        initListeners()
    }
    
    override fun onResume() {
        super.onResume()
        restoreImmersiveState()
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LanguageUtils.applyLanguageConfiguration(this)
        initImmersiveUI()
        onConfigurationChanged()
        onLanguageChanged()
    }
    
    /**
     * 初始化沉浸式UI（状态栏 + 导航栏）
     */
    private fun initImmersiveUI() {
        if (enableImmersiveStatusBar) {
            StatusBarUtils.setTransparentStatusBar(this)
            StatusBarUtils.setStatusBarLightMode(this) // 默认浅色文字
        }
        if (enableImmersiveNavigationBar) {
            NavigationBarUtils.setImmersiveNavigationBar(this)
            NavigationBarUtils.setNavigationBarLightMode(this) // 默认浅色文字
        }
    }
    
    /**
     * 恢复沉浸式状态
     */
    private fun restoreImmersiveState() {
        if (enableImmersiveStatusBar) {
            StatusBarUtils.setTransparentStatusBar(this)
            StatusBarUtils.setStatusBarLightMode(this)
        }
        
        if (enableImmersiveNavigationBar) {
            NavigationBarUtils.setImmersiveNavigationBar(this)
            NavigationBarUtils.setNavigationBarLightMode(this)
        }
    }
    
    /**
     * 抽象方法：初始化视图
     * 子类必须实现此方法来初始化UI组件
     */
    protected abstract fun initViews()
    
    /**
     * 抽象方法：初始化数据
     * 子类可以重写此方法来初始化业务数据
     */
    protected open fun initData() {}
    
    /**
     * 抽象方法：初始化监听器
     * 子类必须实现此方法来设置事件监听
     */
    protected abstract fun initListeners()
    
    /**
     * 显示Toast消息
     */
    protected fun showToast(message: String) {
        showToast(message)
    }
    /**
     * 处理返回键逻辑
     */
    protected open fun handleBackPressed(): Boolean {
        return false // 返回false表示不拦截，交给系统处理
    }
    
    /**
     * 点击空白处自动隐藏键盘
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (enableAutoHideKeyboard && ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = android.graphics.Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    KeyboardUtils.hideKeyboard(this)
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    protected fun getStatusBarHeight(): Int {
        return StatusBarUtils.getStatusBarHeight(this)
    }
    protected fun getNavigationBarHeight(): Int {
        return NavigationBarUtils.getNavigationBarHeight(this)
    }
    protected fun getNavigationBarWidth(): Int {
        return NavigationBarUtils.getNavigationBarWidth(this)
    }
    protected fun hasNavigationBar(): Boolean {
        return NavigationBarUtils.hasNavigationBar(this)
    }

    protected fun switchLanguage(language: String, country: String = ""): Boolean {
        val result = LanguageUtils.switchLanguage(this, language, country)
        if (result) {
            recreate() // 重启Activity以应用新语言
        }
        return result
    }
    protected fun getCurrentLanguage(): String {
        return LanguageUtils.getCurrentLanguage(this)
    }
    protected fun setStatusBarMode(isLight: Boolean) {
        if (isLight) {
            StatusBarUtils.setStatusBarLightMode(this)
        } else {
            StatusBarUtils.setStatusBarDarkMode(this)
        }
    }
    protected fun setNavigationBarMode(isLight: Boolean) {
        if (isLight) {
            NavigationBarUtils.setNavigationBarLightMode(this)
        } else {
            NavigationBarUtils.setNavigationBarDarkMode(this)
        }
    }
    override fun attachBaseContext(newBase: Context?) {
        val sLanguage = AppCache.switchLanguage
        newBase?.let {
            var context =if (sLanguage == "default") {
                val systemLocale = LocaleListCompat.getAdjustedDefault()[0] ?: Locale.getDefault()
                LanguageUtils.updateContextLocale(newBase, systemLocale)
            }else{
                val savedLocale = Locale(sLanguage)
                LanguageUtils.updateContextLocale(newBase, savedLocale)
            }
        }
        super.attachBaseContext(newBase)
    }
}