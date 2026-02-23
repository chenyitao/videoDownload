package com.download.video_download.base.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.core.os.LocaleListCompat
import java.util.*

object LanguageUtils {

    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    fun switchLanguage(context: Context, language: String, country: String = ""): Boolean {
        return try {
            val locale = if (country.isNotEmpty()) {
                Locale(language, country)
            } else {
                Locale(language)
            }
            
            updateLocale(context, locale)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun updateLocale(context: Context, locale: Locale) {
        val resources = context.resources
        val configuration = resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun applyLanguageConfiguration(activity: Activity) {
        val languageCode = AppCache.switchLanguage
        var locale = if (languageCode.isNotEmpty()){
            if (languageCode.contains("-r")) {
                val parts = languageCode.split("-r")
                if (parts.size == 2) {
                    Locale(parts[0], parts[1])
                } else {
                    Locale(languageCode)
                }
            } else {
                Locale(languageCode)
            }
        }else{
            getCurrentLocale(activity)
        }
        val configuration = activity.resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        @Suppress("DEPRECATION")
        activity.resources.updateConfiguration(configuration, activity.resources.displayMetrics)
    }
    fun initLocale(context: Context) {
        val sLanguage = AppCache.switchLanguage
        var context =if (sLanguage == "") {
            val systemLocale = LocaleListCompat.getAdjustedDefault()[0] ?: Locale.getDefault()
            updateLocaleConfiguration(context, systemLocale)
        }else{
            val savedLocale = Locale(sLanguage)
            updateLocaleConfiguration(context, savedLocale)
        }
    }
    fun updateContextLocale(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            configuration.setLayoutDirection(locale)
        }
        return context.createConfigurationContext(configuration)
    }
    fun setAppLocale(context: Context, locale: Locale) {
        updateLocaleConfiguration(context.applicationContext, locale) // 全局上下文
        updateLocaleConfiguration(context, locale) // 当前上下文
    }

    private fun updateLocaleConfiguration(context: Context, locale: Locale) {
        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            configuration.setLayoutDirection(locale)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    @JvmStatic
    fun setAppLanguage(context: Context, language: String, country: String = "") {
        val locale = if (country.isEmpty()) {
            if (language.contains("-r")) {
                val parts = language.split("-r")
                if (parts.size == 2) {
                    Locale(parts[0], parts[1])
                } else {
                    Locale(language)
                }
            } else {
                Locale(language)
            }
        } else {
            Locale(language, country)
        }

        setAppLocale(
            context,
            locale
        )
    }
    fun getSystemOriginalLocale(context: Context): Locale {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return LocaleList.getDefault().get(0) // 系统首选 Locale
        }

        return try {
            val systemLocaleClazz = Class.forName("android.os.SystemProperties")
            val getMethod = systemLocaleClazz.getMethod("get", String::class.java)
            val localeStr = getMethod.invoke(null, "persist.sys.locale") as String?
                ?: getMethod.invoke(null, "ro.product.locale") as String?

            if (!localeStr.isNullOrBlank()) {
                // 解析系统语言字符串（如 "zh-CN" → Locale("zh", "CN")）
                val parts = localeStr.split("_")
                if (parts.size >= 2) {
                    Locale(parts[0], parts[1])
                } else {
                    Locale(parts[0])
                }
            } else {
                Locale.getDefault()
            }
        } catch (e: Exception) {
            Locale.getDefault()
        }
    }
}