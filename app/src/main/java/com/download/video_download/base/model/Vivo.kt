package com.download.video_download.base.model

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.download.video_download.App
import java.lang.reflect.Method
import kotlin.apply
import kotlin.collections.find
import kotlin.collections.isNullOrEmpty
import kotlin.collections.map
import kotlin.collections.toSet
import kotlin.jvm.java
import kotlin.runCatching
import kotlin.text.contains
import kotlin.text.lowercase

object Vivo {
    private val getSystemProperty: Method? by lazy {
        try {
            val clazz = Class.forName("android.os.SystemProperties")
            clazz.getMethod("get", String::class.java, String::class.java)
        } catch (e: Exception) {
            null
        }
    }
    private fun getProp(key: String, def: String = ""): String {
        return try {
            getSystemProperty?.invoke(null, key, def) as? String ?: def
        } catch (e: Exception) {
            def
        }
    }
    fun isVivoDevice(): Boolean {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        return brand.contains("vivo") || brand.contains("iqoo")
                || manufacturer.contains("vivo") || manufacturer.contains("iqoo")
    }
    fun NotificationCompat.Builder.setVivoNumber(notifyId: Int): NotificationCompat.Builder{
        if(isVivoDevice()){
            setNotificationCountByVIVO(notifyId)
        }
        return this
    }

    private fun setNotificationCountByVIVO(notifyId: Int){
        runCatching {
            val number = findNotificationCountByNotifyId(notifyId)
            runCatching {
                val intent = Intent().apply {
                    setAction("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM")
                    putExtra("packageName", App.getAppContext().packageName)
                    putExtra("className", App.getAppContext().packageName+".MainActivity")
                    putExtra("notificationNum", number)
                    addFlags(invokeVIVOIntConstants())
                }
                App.getAppContext().sendBroadcast(intent)
            }
        }

    }

    private fun findNotificationCountByNotifyId(notifyId: Int): Int{
        val notifications = (App.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).activeNotifications
        if(notifications.isNullOrEmpty()){
            return 1
        }
        val hasCurrentNotification = notifications.map {
            it.id
        }.toSet().find {
            it == notifyId
        } != null
        return if(hasCurrentNotification){
            notifications.size
        } else {
            notifications.size + 1
        }
    }

    private fun invokeVIVOIntConstants(): Int {
        var value = 0
        try {
            val c = Class.forName(Intent::class.java.getCanonicalName() ?: "")
            val field = c.getField("FLAG_RECEIVER_INCLUDE_BACKGROUND")
            value = field.get(c) as Int
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }

}