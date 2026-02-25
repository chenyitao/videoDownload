package com.download.video_download.base.config.sensor

import android.os.Bundle
import android.os.Parcelable
import com.download.video_download.App
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.Serializable

/**
 * Firebase上报策略
 */
class FirebaseTrackStrategy : TrackReportStrategy {
    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(App.getAppContext()) }

    override fun reportEvent(eventName: String, params: Map<String, Any>) {
        val bundle = convertParamsToBundle(params)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    /**
     * 转换参数为Firebase支持的Bundle格式
     */
    private fun convertParamsToBundle(params: Map<String, Any>): Bundle {
        val bundle = Bundle()
        params.forEach { (key, value) ->
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