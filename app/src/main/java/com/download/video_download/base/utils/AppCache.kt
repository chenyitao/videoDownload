package com.download.video_download.base.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppCache {
    private const val PREFS_NAME = "preferences"
    private var sharedPreferences: SharedPreferences? = null
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    fun  isInit(): Boolean {
        return sharedPreferences != null
    }
    fun <T> saveValue(key: String, value: T) {
        sharedPreferences?.edit(commit = false) {
            putValue(key, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is Boolean -> sharedPreferences?.getBoolean(key, defaultValue) as T
            is Int -> sharedPreferences?.getInt(key, defaultValue) as T
            is Long -> sharedPreferences?.getLong(key, defaultValue) as T
            is Float -> sharedPreferences?.getFloat(key, defaultValue) as T
            is String -> sharedPreferences?.getString(key, defaultValue) as T
            is Set<*> -> sharedPreferences?.getStringSet(key, defaultValue as Set<String>) as T
            else -> defaultValue
        }
    }

    private fun SharedPreferences.Editor.putValue(key: String, value: Any?) {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value as Set<String>)
            null -> remove(key)
            else -> {}
        }
    }
    var switchLanguage: String
        set(value) {
            saveValue("selectLanguage", value)
        }
        get() =getValue("selectLanguage", "")
    var switchLanguageName: String
        set(value) {
            saveValue("switchLanguageName", value)
        }
        get() =getValue("switchLanguageName", "")
    var isSelectLng: Boolean
        set(value) {
            saveValue("isSelectLng", value)
        }
        get() =getValue("isSelectLng", false)
    var guideShow : Boolean
        set(value) {
            saveValue("guideShow", value)
        }
        get() =getValue("guideShow", false)
    var history : String
        set(value) {
            saveValue("history", value)
        }
        get() =getValue("history", "")
    var isFirstInstall : Boolean
        set(value) {
            saveValue("isFirstInstall", value)
        }
        get() =getValue("isFirstInstall", true)
    var isFirstDetect : Boolean
        set(value) {
            saveValue("isFirstDetect", value)
        }
        get() =getValue("isFirstDetect", true)
    var downloadTask: String
        set(value) {
            saveValue("downloadTask", value)
        }
        get() =getValue("downloadTask", "")
    var playVideos: String
        set(value) {
            saveValue("playVideos", value)
        }
        get() =getValue("playVideos", "")
    var adLimitC : String
        set(value) {
            saveValue("adLimitC", value)
        }
        get() =getValue("adLimitC", "")
    var adcf : String
        set(value) {
            saveValue("adcf", value)
        }
        get() =getValue("adcf", "")
    var gr : String
        set(value) {
            saveValue("gr", value)
        }
        get() =getValue("gr", "")
    var sawdust: String
        set(value) {
            saveValue("sawdust", value)
        }
        get() =getValue("sawdust", "")
    var fb: String
        set(value) {
            saveValue("fb", value)
        }
        get() = getValue("fb", "")
    var isFirstGetConfig: Boolean
        set(value) {
            saveValue("isFirstGetConfig", value)
        }
        get() = getValue("isFirstGetConfig", true)
}