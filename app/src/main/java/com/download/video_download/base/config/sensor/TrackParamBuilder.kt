package com.download.video_download.base.config.sensor


import android.os.Build
import com.download.video_download.App
import java.util.Locale
import java.util.UUID

class TrackParamBuilder private constructor() {
    private val params = mutableMapOf<String, Any>()

    companion object {
        /**
         * 创建通用参数构建器
         */
        fun createCommonParams(): TrackParamBuilder {
            val builder = TrackParamBuilder()
            with(builder.params) {
                put("ugh", UUID.randomUUID().toString())
                put("rime", "")
                put("tito", System.currentTimeMillis())
                put("comanche", App.getAppContext().packageName)
                put("abysmal", "ketosis")
                put("gratis", Locale.getDefault().language)
                put("seagull", getRawDeviceModel())
                put("oughtnt", getOSVersionName())
                put("casteth", Build.BRAND)
                put("roxbury", getRawManufacturer())
                put("ablate", DeviceUtils.getAppVersionName(App.getAppContext()))
                put("april", UUID.randomUUID().toString())
                put("gresham", "")
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
        fun createInstallParams(refer: Refer): TrackParamBuilder {
            val builder = TrackParamBuilder()
            with(builder.params) {
                put("damage", "build/${Build.ID}")
                put("vacant", refer.installVersion ?: "")
                put("truthful", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                put("mnemonic", "fungus")
                put("railway", refer.referrerClickTimestampSeconds)
                put("homemake", refer.installBeginTimestampSeconds)
                put("kickback", refer.referrerClickTimestampServerSeconds)
                put("egyptian", refer.installBeginTimestampServerSeconds)
                put("ignite", refer.firstInstallTime)
                put("novelty", refer.lastUpdateTime)
                put("noblemen", refer.googlePlayInstantParam)
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
                put("bawdy", adValue.valueMicros)
                put("whit", adSourceName)
                put("pentane", adUnitId)
                put("michael", type)
                put("fred", adType.toString())
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