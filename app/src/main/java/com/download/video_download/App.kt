package com.download.video_download

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.arialyy.aria.core.Aria
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.config.cg.RemoteConfig
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.utils.ActivityManager
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.DESUtil
import com.download.video_download.base.utils.GoogleRef
import com.download.video_download.base.utils.LanguageUtils
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.Locale

class App : MultiDexApplication() {
    private var foregroundActivityCount = 0
    private val statusChangeListeners = mutableListOf<AppStatusChangeListener>()
    override fun onCreate() {
        super.onCreate()
        weakApp = WeakReference(this)
        AppCache.init(this)
        if (AppCache.gr.isEmpty()){
            GoogleRef.getInstance().init(this, {
                if (AppCache.gr.isNotEmpty()){
//                    RemoteConfig.instance.getConfigOn()
                }
            })
        }
        TrackMgr.instance.init(this)
        RemoteConfig.instance.getConfig()
        if (AppCache.firstOpen){
            TrackMgr.instance.trackEvent(TrackEventType.FIRST_OPEN,mutableMapOf())
            AppCache.firstOpen = false
        }
        val cacheStr = DESUtil.readTxtFileSync(
            context = this,
            fileName = "cache.txt"
        )
        if (cacheStr.isNotEmpty()) {
            AppCache.adcf = DESUtil.decryptCBC(cacheStr)
        }
        LanguageUtils.initLocale(this)
        initSdk()
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

            override fun onActivityResumed(activity: Activity) {
                AdMgr.INSTANCE.initAdLimitTime()
            }

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
        var isJumpingToSystemSetting = false
        fun initFB(fbobj: JSONObject?){
            if(!FacebookSdk.isInitialized()){
                val fbcg = AppCache.fb
                var id = ""
                var token = ""
                fbcg.takeIf { it.isNotEmpty() }?.let {
                    val json = JSONObject(it)
                    id = json.optString("fid","")
                    token = json.optString("ftk","")
                }?:run {
                    fbobj?.let {
                        id = it.optString("fid","")
                        token = it.optString("ftk","")
                    }
                }
                FacebookSdk.setApplicationId(id)
                FacebookSdk.setClientToken(token)
                FacebookSdk.sdkInitialize(getAppContext())
                weakApp?.get()?.let { application -> AppEventsLogger.activateApp(application) }
            }
        }
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
        if (AppCache.isInit()) {
            val sLanguage = AppCache.switchLanguage
            if (sLanguage.isNotEmpty()) {
                val locale = if (sLanguage.contains("-r")) {
                    val parts = sLanguage.split("-r")
                    if (parts.size == 2) {
                        Locale(parts[0], parts[1])
                    } else {
                        Locale(sLanguage)
                    }
                } else {
                    Locale(sLanguage)
                }
                val configuration = Configuration(resources.configuration)
                configuration.setLocales(LocaleList(locale))
                resources.updateConfiguration(configuration, resources.displayMetrics)
                ActivityManager.currentActivity()?.let { LanguageUtils.applyLanguageConfiguration(it) }
            }
        }
        return resources
    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun initSdk(){
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG_MODE)
            }
            CoroutineScope(Dispatchers.IO).launch {
                MobileAds.initialize(this@App) {}
            }
            val conversionListener = object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>?) {
                    conversionData?.let { data ->
                    }
                }

                override fun onConversionDataFail(errorMessage: String?) {
                    errorMessage?.let {
                        Log.d(
                            "appsflyer",
                            "appsflyer: deeplink fail: $errorMessage"
                        )
                    }
                }

                override fun onAppOpenAttribution(attributionData: MutableMap<String, String>?) {
                    attributionData?.let { data ->
                    }
                }

                override fun onAttributionFailure(errorMessage: String?) {
                    errorMessage?.let {
                        Log.d(
                            "appsflyer",
                            "appsflyer: deeplink fail: $errorMessage"
                        )
                    }
                }
            }
            AppsFlyerLib.getInstance().init("", conversionListener, this)
            AppsFlyerLib.getInstance().start(this,"",object :
                AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.d("appfly", "Launch sent successfully, got 200 response code from server")
                }

                override fun onError(p0: Int, p1: String) {
                    Log.d("appfly", "Launch failed to be sent:\n" +
                            "Error code: " + p0 + "\n"
                            + "Error description: " + p1)
                }
            })
            AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG_MODE)
            AppsFlyerLib.getInstance().setCustomerUserId(TrackMgr.instance.getDistinctID())
            Aria.init(applicationContext)
                .downloadConfig
                .setMaxTaskNum(3)
            AdMgr.INSTANCE.initAdData()
        } catch (e: Exception) {
        }
    }
    interface AppStatusChangeListener {
        fun onAppEnterForeground()
        fun onAppEnterBackground()
    }

    override fun onTerminate() {
        super.onTerminate()
        GoogleRef.getInstance().release()
        TrackMgr.instance.destroy()
        RemoteConfig.instance.stopConfigRequest()
    }
}