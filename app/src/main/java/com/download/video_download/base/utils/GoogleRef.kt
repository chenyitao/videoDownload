package com.download.video_download.base.utils

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.config.sensor.TrackParamBuilder
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.model.Rf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString

class GoogleRef  private constructor(){
    companion object {
        private const val TAG = "GoogleRefer"
        private const val FETCH_TIMEOUT_MS = 9000L
        private const val RETRY_DELAY_MS = 3000L
        private const val MAX_RETRY_COUNT = 2

        fun getInstance(): GoogleRef = SingletonHolder.INSTANCE

        private object SingletonHolder {
            val INSTANCE = GoogleRef()
        }
    }
    private  var referrerClient:InstallReferrerClient? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentRetryCount = 0
    fun init(context: Context,success:()->Unit) {
        if (AppCache.gr.isNotEmpty()) {
            Log.d(TAG, "Referrer已获取成功，无需重复获取")
            return
        }
        currentRetryCount = 0
        fetchReferrer(context,success)
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_ref1)
    }
    private fun fetchReferrer(context: Context,success:()->Unit) {
        coroutineScope.launch {
            try {
                withTimeout(FETCH_TIMEOUT_MS) {
                    doFetchReferrer(context,success)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Referrer获取超时(${FETCH_TIMEOUT_MS}ms)，当前重试次数：$currentRetryCount")
                handleFetchFailure(context,success)
            } catch (e: Exception) {
                Log.e(TAG, "Referrer获取异常，当前重试次数：$currentRetryCount", e)
                handleFetchFailure(context,success)
            }
        }
    }

    private fun saveReferrerData(context: Context,success:()->Unit) {
        runCatching {
            val response: ReferrerDetails? = referrerClient?.installReferrer
            val referrerUrl: String? = response?.installReferrer
            val referrerClickTimestampSeconds: Long = response?.referrerClickTimestampSeconds?:0
            val installBeginTimestampSeconds: Long = response?.installBeginTimestampSeconds?:0
            val referrerClickTimestampServerSeconds: Long = response?.referrerClickTimestampServerSeconds?:0
            val installBeginTimestampServerSeconds: Long = response?.installBeginTimestampServerSeconds?:0
            val firstInstallTime = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            val lastUpdateTime = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            val instantExperienceLaunched: Boolean = response?.googlePlayInstantParam?:false
            val installVersion: String? = response?.installVersion?:""
            val referrer = Rf(
                referrerUrl = referrerUrl?: "",
                referrerClickTimestampSeconds = referrerClickTimestampSeconds,
                referrerClickTimestampServerSeconds = referrerClickTimestampServerSeconds,
                installBeginTimestampSeconds = installBeginTimestampSeconds,
                googlePlayInstantParam = instantExperienceLaunched,
                installBeginTimestampServerSeconds = installBeginTimestampServerSeconds,
                firstInstallTime = firstInstallTime,
                lastUpdateTime = lastUpdateTime,
                installVersion = installVersion
            )
            Log.d(
                "InstallRefer",
                "InstallRefer: InstallRefer request success  ,referdata: ${context.jsonParser().encodeToString(referrer)}"
            )
            AppCache.gr = context.jsonParser().encodeToString(referrer)
        }.onSuccess {
        }.onFailure {
            handleFetchFailure(context,success)
        }
    }
    private suspend fun doFetchReferrer(context: Context,success:()->Unit) = withContext(Dispatchers.Main) {
        referrerClient = InstallReferrerClient.newBuilder(context).build()

        referrerClient?.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        saveReferrerData(context,success)
                        success.invoke()
                        referrerClient?.endConnection()
                    }
                    else -> {
                        Log.d(TAG, "Referrer获取失败，响应码：$responseCode")
                        handleFetchFailure(context,success)
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
            }
        })
    }
    private fun handleFetchFailure(context: Context,success:()->Unit) {
        referrerClient?.endConnection()
        if (currentRetryCount < MAX_RETRY_COUNT) {
            currentRetryCount++
            coroutineScope.launch {
                delay(RETRY_DELAY_MS)
                fetchReferrer(context,success)
            }
        } else {
            currentRetryCount = 0
        }
    }
    fun release() {
        referrerClient?.endConnection()
        coroutineScope.cancel()
    }
}