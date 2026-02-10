package com.download.video_download.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.download.video_download.App
import com.download.video_download.base.ext.showToast
import com.download.video_download.base.utils.*
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.utils.NavigationBarUtils.setNavigationBarColor
import java.util.Locale

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity() {
    
    protected open val enableImmersiveStatusBar: Boolean = true
    protected open val enableImmersiveNavigationBar: Boolean = true
    protected open val enableAutoHideKeyboard: Boolean = true
    protected open fun onLanguageChanged() {}
    protected open fun onConfigurationChanged() {}
    lateinit var mBind: VB

    lateinit var mViewModel: VM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageUtils.applyLanguageConfiguration(this)
        mBind = createViewBinding()
        setContentView(mBind.root)
        mViewModel = createViewModel()
        initImmersiveUI()
        initViews(savedInstanceState)
        initData()
        initListeners()
        registerOnBackPressedCallback()
    }
    protected abstract fun createViewBinding(): VB

    protected abstract fun createViewModel(): VM
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
    private fun initImmersiveUI() {
        if (enableImmersiveStatusBar) {
            StatusBarUtils.setTransparentStatusBar(this)
            StatusBarUtils.setStatusBarLightMode(this) // 默认浅色文字
        }
        if (enableImmersiveNavigationBar) {
            setNavigationBarColor(this, android.graphics.Color.TRANSPARENT)
            NavigationBarUtils.setImmersiveNavigationBar(this)
            NavigationBarUtils.setNavigationBarLightMode(this) // 默认浅色文字
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            true
        ViewCompat.setOnApplyWindowInsetsListener(mBind.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = 0,
                top = 0,
                right = 0,
                bottom = systemBars.bottom
            )
            insets
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
    protected abstract fun initViews(savedInstanceState: Bundle?)
    protected open fun initData() {}
    protected abstract fun initListeners()
    protected fun showToast(message: String) {
        showToast(message)
    }
    protected open fun handleBackPressed(): Boolean {
        return false
    }
    protected open fun registerOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (handleBackPressed()) {
                    return
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

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