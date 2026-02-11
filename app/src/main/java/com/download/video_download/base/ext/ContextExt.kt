package com.download.video_download.base.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.serialization.json.Json

/**
 * Context 扩展函数
 */

/**
 * 显示Toast消息
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

/**
 * 启动Activity
 */
inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}

/**
 * 带参数启动Activity
 */
inline fun <reified T : Activity> Context.startActivityWithExtras(extras: Bundle.() -> Unit) {
    val intent = Intent(this, T::class.java)
    val bundle = Bundle().apply(extras)
    intent.putExtras(bundle)
    startActivity(intent)
}
inline fun <reified T : Activity> Context.jsonParser(): Json {
    return Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = false
        isLenient = true
    }
}

/**
 * 设置View可见性
 */
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

/**
 * View点击防抖扩展
 */
fun View.setOnDebounceClickListener(debounceTime: Long = 500, action: () -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            action()
        }
    }
}