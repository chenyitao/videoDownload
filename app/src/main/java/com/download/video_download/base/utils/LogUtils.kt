package com.download.video_download.base.utils

import android.util.Log
import com.download.video_download.BuildConfig
object LogUtils {
    private const val TAG = "SafeApp"
    private val LOG_ENABLE = BuildConfig.DEBUG_MODE

    fun d(tag: String, msg: String) {
        if (LOG_ENABLE) Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        if (LOG_ENABLE) Log.i(tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (LOG_ENABLE) {
            if (tr != null) {
                Log.e(tag, msg, tr)
            } else {
                Log.e(tag, msg)
            }
        }
    }

    fun d(msg: String) = d(TAG, msg)
    fun i(msg: String) = i(TAG, msg)
    fun e(msg: String, tr: Throwable? = null) = e(TAG, msg, tr)
}