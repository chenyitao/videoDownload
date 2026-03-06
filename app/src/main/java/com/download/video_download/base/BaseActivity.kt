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
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.ext.startActivityWithExtras
import com.download.video_download.base.utils.NavigationBarUtils.setNavigationBarColor
import com.download.video_download.ui.activity.MainActivity
import com.download.video_download.ui.activity.SplashActivity
import org.json.JSONObject
import java.util.Locale

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity(), App.AppStatusChangeListener {
    
    protected open val enableImmersiveStatusBar: Boolean = true
    protected open val enableImmersiveNavigationBar: Boolean = true
    protected open val enableAutoHideKeyboard: Boolean = true
    protected open fun onLanguageChanged() {}
    protected open fun onConfigurationChanged() {}
    lateinit var mBind: VB

    lateinit var mViewModel: VM
    var myApp: App? = null
    public var isNavigationBarHidden = false
    lateinit var permissionHelper: PermissionHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageUtils.applyLanguageConfiguration(this)
        mBind = createViewBinding()
        permissionHelper = PermissionHelper.with(this)
        setContentView(mBind.root)
        mViewModel = createViewModel()
        AppCache.fuc.takeIf { it.isNotEmpty() }?.let {fc->
            val fuc = JSONObject(fc)
            val bBarHide = fuc.optString("bBarHide","n")
            ActivityManager.currentActivity()?.let {ac->
                if (bBarHide == "y") {
                    isNavigationBarHidden = true
                    NavigationBarUtils.hideNavigationBar(ac)
                }else{
                    isNavigationBarHidden = false
                    NavigationBarUtils.showNavigationBar(ac)
                }
            }
        }
        initImmersiveUI()
        initViews(savedInstanceState)
        initData()
        initListeners()
        registerOnBackPressedCallback()
        myApp = application as App
        myApp?.addAppStatusChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
    }

    protected abstract fun createViewBinding(): VB

    protected abstract fun createViewModel(): VM
    override fun onResume() {
        super.onResume()
        if (isNavigationBarHidden){
            NavigationBarUtils.hideNavigationBar(this)
        }
        restoreImmersiveState()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        myApp?.removeAppStatusChangeListener(this)
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
            StatusBarUtils.setStatusBarLightMode(this)
        }
        if (enableImmersiveNavigationBar) {
            setNavigationBarColor(this, android.graphics.Color.TRANSPARENT)
            NavigationBarUtils.setImmersiveNavigationBar(this)
            NavigationBarUtils.setNavigationBarLightMode(this)
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            true
    }

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
        if (sLanguage.isNotEmpty()){
            newBase?.let {
                val savedLocale = Locale(sLanguage)
                LanguageUtils.updateContextLocale(newBase, savedLocale)
            }
        }
        super.attachBaseContext(newBase)
    }

    override fun onAppEnterForeground() {
//        if (ActivityManager.currentActivity() is SplashActivity){
//            return
//        }
//        if (App.isJumpingToSystemSetting){
//            App.isJumpingToSystemSetting = false
//            return
//        }
//        LogUtils.d("onAppEnterForeground")
//        startActivity<SplashActivity>{
//            putExtra("from", "Background")
//        }
    }

    override fun onAppEnterBackground() {
    }
}