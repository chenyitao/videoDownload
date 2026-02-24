package com.download.video_download.base.config.sensor

import android.os.Bundle
import com.download.video_download.App
import com.facebook.appevents.AppEventsLogger
import java.math.BigDecimal
import java.util.Currency

class FacebookTrackStrategy {
    private val fbLogger by lazy { AppEventsLogger.newLogger(App.getAppContext()) }

    /**
     * 上报购买事件
     */
    fun reportPurchaseEvent(currency: Currency, amount: BigDecimal) {
        fbLogger.logPurchase(amount, currency)
    }

    /**
     * 上报普通事件
     */
    fun reportEvent(eventName: String, params: Map<String, Any>) {
        val bundle = convertParamsToBundle(params)
        fbLogger.logEvent(eventName, bundle)
    }

    /**
     * 转换参数为Facebook支持的Bundle格式
     */
    private fun convertParamsToBundle(params: Map<String, Any>): Bundle {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            bundle.putString(key, value.toString())
        }
        return bundle
    }
}