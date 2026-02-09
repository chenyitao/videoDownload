package com.download.video_download.base.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.core.os.LocaleListCompat
import java.util.*

/**
 * 多语言工具类
 */
object LanguageUtils {
    
    const val LANGUAGE_CHINESE = "zh"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_SYSTEM = "system"
    
    /**
     * 获取当前语言
     */
    fun getCurrentLanguage(context: Context): String {
        val locale = getCurrentLocale(context)
        return locale.language
    }
    
    /**
     * 获取当前地区
     */
    fun getCurrentCountry(context: Context): String {
        val locale = getCurrentLocale(context)
        return locale.country
    }
    
    /**
     * 获取当前Locale
     */
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
    
    /**
     * 切换语言
     */
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
    
    /**
     * 更新Locale配置
     */
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
    
    /**
     * 应用语言配置到Activity
     */
    fun applyLanguageConfiguration(activity: Activity) {
        val locale = getCurrentLocale(activity)
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
    
    /**
     * 获取系统语言
     */
    fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }
    
    /**
     * 获取支持的语言列表
     */
    fun getSupportedLanguages(): List<String> {
        return listOf(LANGUAGE_CHINESE, LANGUAGE_ENGLISH)
    }
    
    /**
     * 检查语言是否支持
     */
    fun isLanguageSupported(language: String): Boolean {
        return getSupportedLanguages().contains(language)
    }
    fun initLocale(context: Context) {
        val sLanguage = AppCache.switchLanguage
        var context =if (sLanguage == "default") {
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
        Locale.setDefault(locale)
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
}