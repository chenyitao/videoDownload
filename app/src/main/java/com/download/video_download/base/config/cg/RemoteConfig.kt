package com.download.video_download.base.config.cg

import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.config.utils.CfUtils
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.model.Rf
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.AsyncPostRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.max

class RemoteConfig private constructor(){
    private var periodicReportJob: Job? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val requestMutex = Mutex()
    companion object {
        val instance: RemoteConfig by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RemoteConfig()
        }
    }
    fun getConfig(){
        if (periodicReportJob?.isActive == true) {
            return
        }
        periodicReportJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    TrackMgr.instance.trackEvent(TrackEventType.safedddd_user1, mapOf("safedddd" to 2))
                    if (!AppCache.isFirstGetConfig){
                        delay(50*60 * 1000)
                        getAdminConfig()
                    }else{
                        getAdminConfig()
                        delay(50*60 * 1000)
                    }
                } catch (e: Exception) {
                    delay(50*60 * 1000)
                }
            }
        }
    }

    private suspend fun getAdminConfig(currentRetry: Int = 0){
        requestMutex.withLock {
            runCatching {
                val startTime = System.currentTimeMillis()
                val params = JSONObject()
                params.put("ocnz", getConfigParams())
                val host = App.getAppContext().getString(R.string.admin_host_test)

                val body = params.toString()
                val requestSuccess = suspendCancellableCoroutine { continuation ->
                    AsyncPostRequest.sendPost(host, body,
                        onSuccess = {
                            val endTime = System.currentTimeMillis()
                            val timeDiffMs = max(0, endTime - startTime)
                            val seconds = timeDiffMs / 1000
                            val sec = if (seconds.toInt() == 0 && timeDiffMs > 0) 1 else seconds.toInt()
                            TrackMgr.instance.trackEvent(TrackEventType.safedddd_user2, mapOf("safedddd" to 1, "safedddd1" to sec))
                            continuation.resume(true)
                            if (it.isEmpty()){
                                return@sendPost
                            }
                            handleConfigSuccess(it)
                        },
                        onFailure = { errorMsg ->
                            val endTime = System.currentTimeMillis()
                            val timeDiffMs = max(0, endTime - startTime)
                            val seconds = timeDiffMs / 1000
                            val sec = if (seconds.toInt() == 0 && timeDiffMs > 0) 1 else seconds.toInt()
                            TrackMgr.instance.trackEvent(TrackEventType.safedddd_user2, mapOf("safedddd" to errorMsg, "safedddd1" to sec))
                            if (errorMsg.contains("timeout") && currentRetry < 1) {
                                continuation.resume(false)
                            } else {
                                continuation.resume(true)
                            }
                        })
                }
                if (!requestSuccess && currentRetry < 1) {
                    getAdminConfig(currentRetry + 1)
                }
            }
        }
    }
    fun getConfigOn() {
        coroutineScope.launch(Dispatchers.IO) {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_user1, mapOf("safedddd" to 3))
            getAdminConfig()
        }
    }
    private fun handleConfigSuccess(data: String) {
        val config = Crypt.paramsDecrypt (data)
        AppCache.adcf = config.toString()
        AppCache.isFirstGetConfig =  false
        val fc = config.optJSONObject("fc")
        fc?.let {
            App.initFB(fc)
            AppCache.fb = it.toString()
        }
        val adConfig = config.optJSONObject("adcg")
        adConfig?.let {
            AdMgr.INSTANCE.initAdData()
            val advert = it.getJSONObject("at")
            var safedddd = ""
            if (advert.has("lg")){
                safedddd = "1"
            }else {
                safedddd = "2"
            }
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_user4,mutableMapOf("safedddd" to safedddd))
        }
        var safedddd = ""
        var safeddddk1 = ""
        var safeddddk3 = ""
        val k0 = config.optString("kionfz","")
        if (k0.isEmpty()){
            safedddd = "1"
        }else if (getLastChar(k0) == "ou"){
            safedddd = "3"
        }else if (getLastChar(k0) == "ji"){
            safedddd = "2"
        }else if (getLastChar(k0) == "zm"){
            safedddd = "4"
        }
        val k1 = config.optString("huxwlv","")
        if (k1.isNotEmpty()){
            if (getLastChar(k1) == "ji"){
                safeddddk1 = "1"
            }else if (getLastChar(k1) == "zm"){
                safeddddk1 = "2"
            }
        }
        val k3 = config.optString("yptrj","")
        if (k3.isEmpty()){
            safeddddk3 = "3"
        } else if (k3.last().isDigit() && k3.last().digitToInt() == 0){
            safeddddk3 = "4"
        } else if (getLastChar(k3) == "ji" || getLastChar(k3) == "ou"){
            safeddddk3 = "1"
        }else if (getLastChar(k3) == "zm"){
            safeddddk3 = "2"
        }
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_user3,mutableMapOf("safedddd" to safedddd, "safeddddk1" to safeddddk1,"safeddddk3" to safeddddk3))
    }
    private fun getLastChar(str: String?): String {
        val lastChar = str?.last()
        return if (lastChar?.isDigit() == true) {
            val num = lastChar.digitToInt()
            if (num % 2 == 0) "ou" else "ji"
        }
        else if (lastChar?.isLetter() == true) {
            "zm"
        }
        else {
            ""
        }
    }
    fun stopConfigRequest() {
        periodicReportJob?.cancel()
        periodicReportJob = null
    }
    private fun getConfigParams(): String {
        val referStr: String = AppCache.gr
        var rfUrl = ""
        var refer: Rf? = null
        referStr.takeIf { it.isNotEmpty() }?.let {
            refer = App.getAppContext().jsonParser().decodeFromString<Rf>(referStr)
            rfUrl = refer.referrerUrl
        }

        val json = JSONObject().apply {
            put("qvugtr", TrackMgr.instance.getDistinctID())
            put("wkzr", "SafeDownload")
            put("sqxup", CfUtils.getVersionName(App.getAppContext()))
            put("yfux", rfUrl.ifEmpty { if (refer == null) "XXX" else "NNN" })
            put("pixm", "NA")
            put("edgp", refer?.referrerClickTimestampSeconds)
            put("oylkfx", refer?.referrerClickTimestampServerSeconds)
            put("euzblx", App.getAppContext().packageManager.getInstallerPackageName(App.getAppContext().packageName) ?: "")
            put("aa", UUID.randomUUID().toString())
            put("bb", UUID.randomUUID().toString())
        }
        return Crypt.paramsEncrypt(json)
    }
}