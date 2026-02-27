package com.download.video_download.base.config.sensor


import android.os.Build
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme.AD_TYPE
import com.appsflyer.AdRevenueScheme.AD_UNIT
import com.appsflyer.AdRevenueScheme.COUNTRY
import com.appsflyer.AdRevenueScheme.PLACEMENT
import com.appsflyer.MediationNetwork
import com.download.video_download.App
import com.download.video_download.base.config.utils.CfUtils
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.model.Rf
import com.download.video_download.base.utils.AppCache
import java.util.Locale
import java.util.UUID
import kotlin.collections.set

class TrackParamBuilder private constructor() {
    private val params = mutableMapOf<String, Any>()

    companion object {
        /**
         * 创建通用参数构建器
         */
        fun createCommonParams(): TrackParamBuilder {
            val builder = TrackParamBuilder()
            with(builder.params) {
                put("ambulant", UUID.randomUUID().toString())
                put("sawdust", "")
                put("mcdowell", System.currentTimeMillis())
                put("woe", App.getAppContext().packageName)
                put("delight", "mob")
                put("vilify", Locale.getDefault().language)
                put("memorial", CfUtils.getDeviceModel())
                put("missoula", CfUtils.getOSVersionName())
                put("abutted", Build.BRAND)
                put("reverent", CfUtils.getManufacturer())
                put("cogitate", CfUtils.getVersionName(App.getAppContext()))
                put("furbish", UUID.randomUUID().toString())
                put("megavolt", "")
            }
            return builder
        }

        /**
         * 创建会话参数构建器
         */
        fun createSessionParams(): TrackParamBuilder {
            return TrackParamBuilder()
        }

        /**
         * 创建安装参数构建器
         */
        fun createInstallParams(): TrackParamBuilder {
            val rf = AppCache.gr
            val refer =  App.getAppContext().jsonParser().decodeFromString<Rf>(rf)
            val builder = TrackParamBuilder()
            with(builder.params) {
                put("atheism", "build/${Build.ID}")
                put("abeyant", refer.installVersion ?: "")
                put("constant", "Mozilla/5.0 (Linux; Android 12; Galaxy S23) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                put("hayward", "pot")
                put("prize", refer.referrerClickTimestampSeconds)
                put("fraction", refer.installBeginTimestampSeconds)
                put("child", refer.referrerClickTimestampServerSeconds)
                put("hookworm", refer.installBeginTimestampServerSeconds)
                put("afraid", refer.firstInstallTime)
                put("moses", refer.lastUpdateTime)
//                put("noblemen", refer.googlePlayInstantParam)
            }
            return builder
        }

        /**
         * 创建广告曝光参数构建器
         */
        fun createAdImpressionParams(
            adValue: com.google.android.gms.ads.AdValue,
            response: com.google.android.gms.ads.ResponseInfo?,
            adUnitId: String,
            type: String = "",
            adType: Int
        ): TrackParamBuilder {
            val builder = TrackParamBuilder()
            val loadedAdapterResponseInfo = response?.loadedAdapterResponseInfo
            val adSourceName = loadedAdapterResponseInfo?.adSourceName ?: ""

            with(builder.params) {
                put("scruple", adValue.valueMicros)
                put("piteous", adSourceName)
                put("carlton", adUnitId)
                put("ancestor", type)
                put("load", adType.toString())
            }
            return builder
        }
        fun createAfAdRevenueParams(
            adValue: com.google.android.gms.ads.AdValue,
            adUnitId: String,
            type: String = "",
            adType: Int
        ): TrackParamBuilder {
            val builder = TrackParamBuilder()
            val valueMicros = adValue.valueMicros
            val currencyCode = adValue.currencyCode
            val localeCountry = Locale.getDefault().country.uppercase(Locale.US)
            val adRevenueData = AFAdRevenueData(
                "admob",
                MediationNetwork.GOOGLE_ADMOB,
                currencyCode,
                (valueMicros/1000000.0)
            )
            with(builder.params) {
                put(COUNTRY,localeCountry)
                put(AD_UNIT, adUnitId)
                put(AD_TYPE, adType)
                put(PLACEMENT, type)
            }
            return builder
        }
    }

    /**
     * 添加自定义参数
     */
    fun addParam(key: String, value: Any): TrackParamBuilder {
        params[key] = value
        return this
    }

    /**
     * 添加多个自定义参数
     */
    fun addParams(newParams: Map<String, Any>): TrackParamBuilder {
        params.putAll(newParams)
        return this
    }

    /**
     * 构建最终参数Map
     */
    fun build(): Map<String, Any> {
        return HashMap(params) // 返回不可变副本，保证线程安全
    }
}