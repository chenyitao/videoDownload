package com.download.video_download.base.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
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

    suspend fun checkIsM3U8ByHttpURLConnection(url: String): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        return@withContext try {
            val urlObj = URL(url)
            connection = urlObj.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Range", "bytes=0-1023")

            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext false
            }

            val inputStream = if (responseCode >= 400) {
                connection.errorStream
            } else {
                connection.inputStream
            } ?: return@withContext false

            reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val stringBuilder = StringBuilder()
            val buffer = CharArray(1024)
            var charRead: Int
            while (stringBuilder.length < 1024) {
                charRead = reader.read(buffer)
                if (charRead == -1) break
                stringBuilder.append(buffer, 0, charRead)
            }

            val content = stringBuilder.toString().trim()
            content.startsWith("#EXTM3U")

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }
}
