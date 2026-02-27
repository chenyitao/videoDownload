package com.download.video_download.base.config.sensor

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.download.video_download.App
import com.facebook.appevents.AppEventsLogger
import java.io.Serializable
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
    fun reportEvent(eventName: String, params: MutableMap<String, Any>) {
        val bundle = convertParamsToBundle(params)
        fbLogger.logEvent(eventName, bundle)
    }

    /**
     * 转换参数为Facebook支持的Bundle格式
     */
    private fun convertParamsToBundle(map: MutableMap<String, Any>): Bundle {
        val bundle = Bundle()
        map.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Float -> bundle.putFloat(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                is Byte -> bundle.putByte(key, value)
                is Short -> bundle.putShort(key, value)
                is Char -> bundle.putChar(key, value)

                is ArrayList<*> -> {
                    when {
                        value.all { it is String } -> bundle.putStringArrayList(key, value as ArrayList<String>)
                        value.all { it is Int } -> bundle.putIntegerArrayList(key, value as ArrayList<Int>)
                        value.all { it is Parcelable } -> bundle.putParcelableArrayList(key, value as ArrayList<Parcelable>)
                    }
                }
                is Parcelable -> bundle.putParcelable(key, value)
                is Serializable -> bundle.putSerializable(key, value)

                null -> bundle.putString(key, null)
                else -> bundle.putString(key, value.toString())
            }
        }
        return bundle
    }
}