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
                LogUtils.d(TAG, "请求URL：$url")
                LogUtils.d(TAG, "请求超时时间：${TIMEOUT_MS / 1000}秒")
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
                }
                LogUtils.d(TAG, "===== 请求头信息 =====")
                connection.requestProperties?.forEach { (key, values) ->
                    LogUtils.d(TAG, "$key: ${values.joinToString(", ")}")
                }

                LogUtils.d(TAG, "===== 请求体参数 =====")
                LogUtils.d(TAG, bodyParams)
                BufferedOutputStream(connection.outputStream).use { outputStream ->
                    val paramsBytes = bodyParams.toByteArray(StandardCharsets.UTF_8)
                    outputStream.write(paramsBytes)
                    outputStream.flush()
                }

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                // 打印响应基础信息
                LogUtils.d(TAG, "===== 响应结果 [状态码：$responseCode] =====")
                LogUtils.d(TAG, "响应消息：$responseMessage")
                when {
                    responseCode in 200..299 -> {
                        inputStream = connection.inputStream
                        val response = inputStream.readToString()
                        LogUtils.d(TAG, "响应体：$response")
                        postToMainThread {onSuccess.invoke (response) }
                    }
                    else -> {
                        val errorResponse = connection.errorStream?.readToString() ?: "无错误响应体"
                        LogUtils.e(TAG, "服务器响应错误，错误响应体：$errorResponse")
                        val errorMsg = "error，code：$responseCode"
                        postToMainThread { onFailure.invoke(errorMsg) }
                    }
                }

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is java.net.SocketTimeoutException -> "timeout（${TIMEOUT_MS/1000}s）"
                    is java.net.UnknownHostException -> "parse error"
                    else -> "exception：${e.message ?: "unkown"}"
                }
                LogUtils.e(TAG, "请求异常：$errorMsg", e)
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