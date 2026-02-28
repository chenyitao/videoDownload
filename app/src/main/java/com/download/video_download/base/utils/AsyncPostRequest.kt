package com.download.video_download.base.utils

import android.os.Handler
import android.os.Looper
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AsyncPostRequest {
    private const val TAG = "AsyncPostRequest"
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    private const val TIMEOUT_MS = 20 * 1000
    fun sendPost(
        url: String,
        bodyParams: String,
        timeoutMs: Int = TIMEOUT_MS, // 可选参数，默认20秒
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        sendPostWithRetry(url, bodyParams, timeoutMs, onSuccess, onFailure)
    }
    fun sendPost(
        url: String,
        bodyParams: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        sendPostWithRetry(url, bodyParams,  TIMEOUT_MS,onSuccess, onFailure)
    }

    private fun sendPostWithRetry(
        url: String,
        bodyParams: String,
        timeoutMs: Int,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        executor.execute {
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null

            try {
                LogUtils.d(TAG, "REQUEST URL：$url")
                val requestUrl = URL(url)
                connection = requestUrl.openConnection() as HttpURLConnection

                with(connection) {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    connectTimeout = timeoutMs
                    readTimeout = timeoutMs
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Connection", "Keep-Alive")
                    setRequestProperty("Charset", "UTF-8")
                }

                LogUtils.d(TAG, "===== REQUEST PARAMS =====")
                LogUtils.d(TAG, bodyParams)
                BufferedOutputStream(connection.outputStream).use { outputStream ->
                    val paramsBytes = bodyParams.toByteArray(StandardCharsets.UTF_8)
                    outputStream.write(paramsBytes)
                    outputStream.flush()
                }

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                // 打印响应基础信息
                LogUtils.d(TAG, "===== RESPONSE RESULT [CODE：$responseCode] =====")
                LogUtils.d(TAG, "RESPONSE MSG：$responseMessage")
                when {
                    responseCode in 200..299 -> {
                        inputStream = connection.inputStream
                        val response = inputStream.readToString()
                        LogUtils.d(TAG, "RESPONSE BODY：$response")
                        postToMainThread {onSuccess.invoke (response) }
                    }
                    else -> {
                        val errorResponse = connection.errorStream?.readToString() ?: "NO ERROR RESPONSE"
                        LogUtils.e(TAG, "SERVER RESPONSE ERROR, ERROR RESPONSE：$errorResponse")
                        val errorMsg = "$responseCode"
                        postToMainThread { onFailure.invoke(errorMsg) }
                    }
                }

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is java.net.SocketTimeoutException -> "timeout（${TIMEOUT_MS/1000}s）"
                    is java.net.UnknownHostException -> "parse error"
                    else -> "exception：${e.message ?: "unkown"}"
                }
                LogUtils.e(TAG, "REQUEST ERROR：$errorMsg", e)
                postToMainThread { onFailure.invoke(errorMsg) }
            } finally {
                inputStream?.close()
                connection?.disconnect()
            }
        }
    }

    private fun InputStream.readToString(): String {
        return BufferedReader(InputStreamReader(this, StandardCharsets.UTF_8)).use { reader ->
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            stringBuilder.toString()
        }
    }

    private fun postToMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}