package com.download.video_download.base.utils

import android.util.LruCache
import kotlin.text.toByteArray

class MermLruCache private constructor()  {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 16
    private val memoryCache = object : LruCache<String, Any>(cacheSize) {
        override fun sizeOf(key: String, value: Any): Int {
            return when (value) {
                is String -> value.toByteArray().size / 1024
                is ByteArray -> value.size / 1024
                else -> 1
            }
        }

        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: Any?, newValue: Any?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
        }
    }
    fun <T> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return memoryCache.get(key) as? T
    }

    fun getTrackMemoryCache(): LruCache<String, Any> {
        return memoryCache
    }

    fun clear() {
        memoryCache.evictAll()
    }
    fun put(key: String, value: Any) {
        memoryCache.put(key, value)
    }

    companion object {
        val instance: MermLruCache by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MermLruCache()
        }
    }
}