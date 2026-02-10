package com.download.video_download

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.core.os.LocaleListCompat
import com.download.video_download.base.utils.ActivityManager
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LanguageUtils
import java.lang.ref.WeakReference
import java.util.Locale

class App : Application() {
    interface AppStatusChangeListener {
        fun onAppEnterForeground()
        fun onAppEnterBackground()
    }
    private var foregroundActivityCount = 0
    private val statusChangeListeners = mutableListOf<AppStatusChangeListener>()
    override fun onCreate() {
        super.onCreate()
        weakApp = WeakReference(this)
        AppCache.init(this)
        LanguageUtils.initLocale(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                ActivityManager.addActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                foregroundActivityCount++
                if (foregroundActivityCount == 1 && !isAppInForeground) {
                    isAppInForeground = true
                    statusChangeListeners.forEach { it.onAppEnterForeground() }
                }
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                foregroundActivityCount--
                if (foregroundActivityCount == 0 && isAppInForeground) {
                    isAppInForeground = false
                    statusChangeListeners.forEach { it.onAppEnterBackground() }
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                ActivityManager.removeActivity(activity)
            }
        })
    }
    companion object {
        private var weakApp: WeakReference<Application>? = null
        var isAppInForeground = false
        fun getAppContext(): Context {
            return weakApp?.get()?.applicationContext!!
        }
    }
    fun addAppStatusChangeListener(listener: AppStatusChangeListener) {
        if (!statusChangeListeners.contains(listener)) {
            statusChangeListeners.add(listener)
        }
    }

    fun removeAppStatusChangeListener(listener:AppStatusChangeListener) {
        statusChangeListeners.remove(listener)
    }
    override fun getResources(): android.content.res.Resources {
        val resources = super.getResources()
        if (AppCache.isInit()){
            val sLanguage = AppCache.switchLanguage
            val savedLocale =if (sLanguage == "default") {
                LocaleListCompat.getAdjustedDefault()[0] ?: Locale.getDefault()
            }else{
                Locale(sLanguage)
            }
            val configuration = Configuration(resources.configuration)
            configuration.setLocales(LocaleList(savedLocale))
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return resources
    }
}