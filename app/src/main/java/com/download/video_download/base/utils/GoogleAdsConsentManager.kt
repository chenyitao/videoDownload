package com.download.video_download.base.utils
import android.app.Activity
import com.download.video_download.App
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Arrays
import kotlin.coroutines.resume

object GoogleAdsConsentManager {
    private val consentInfoClient = UserMessagingPlatform.getConsentInformation(App.getAppContext())
    var hasAdRequestPermission = false
    var isLoadingConsent = false
    suspend fun requestConsentInfo(activity: Activity): Boolean {
        return suspendCancellableCoroutine { result ->
            try {
                val isTest = ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("8C867149F21FBD7E6C1D39E79E55D31C")
                val consentParams = ConsentRequestParameters
                    .Builder()
                    .setConsentDebugSettings(isTest.build())
                    .build()
                consentInfoClient.reset()
                consentInfoClient.requestConsentInfoUpdate(
                    activity,
                    consentParams,
                    {
                        hasAdRequestPermission = false
                        result.resume(true)
                    },
                    { error ->
                        hasAdRequestPermission = true
                        result.resume(false)
                    }
                )
            } catch (_: Throwable) {
                hasAdRequestPermission = true
                result.resume(false)
            }
        }
    }

    fun checkIfNeedShowConsent(): Boolean {
        val curCountry = AppCache.curCountry
        if (AppCache.isShowUmp) {
            hasAdRequestPermission = true
            return false
        }
        if (curCountry !in C.countrys) {
            hasAdRequestPermission = true
            return false
        }
        return true
    }

    suspend fun showConsentForm(activity: Activity): Boolean {
        return suspendCancellableCoroutine { result ->
            try {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    AppCache.isShowUmp= (error == null)
                    hasAdRequestPermission = true
                    isLoadingConsent = false
                    result.resume(true)
                }
            } catch (_: Throwable) {
                hasAdRequestPermission = true
                isLoadingConsent = false
                result.resume(false)
            }
        }
    }

    fun isConsentProcessCompleted(): Boolean {
        val localCountry = AppCache.curCountry
        val isShowUmp = AppCache.isShowUmp

        return isShowUmp
                || consentInfoClient.canRequestAds()
                || localCountry !in C.countrys
                || hasAdRequestPermission
    }
}