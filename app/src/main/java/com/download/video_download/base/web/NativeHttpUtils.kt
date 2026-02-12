package com.download.video_download.base.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NativeHttpUtils {
    suspend fun getUrlHeaders(url: String): Map<String, String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        return@withContext try {
            val urlObj = URL(url)
            connection = urlObj.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.instanceFollowRedirects = true

            connection.connect()
            val headersMap = mutableMapOf<String, String>()
            connection.headerFields.forEach { (key, values) ->
                if (key != null) {
                    headersMap[key] = values.firstOrNull() ?: ""
                }
            }
            headersMap
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        } finally {
            connection?.disconnect()
        }
    }
}