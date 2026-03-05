package com.download.video_download.base.nt

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.PowerManager
import android.text.format.DateUtils
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.ext.jsonParser
import com.download.video_download.base.model.NotifyData
import com.download.video_download.base.model.Nt
import com.download.video_download.base.model.Vivo.setVivoNumber
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.ui.activity.SplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import kotlin.compareTo
import kotlin.text.compareTo
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class NtMgr private constructor() {
    companion object {
        val instance: NtMgr by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NtMgr()
        }
    }
    val isLocked: Boolean
        get() = (App.getAppContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked
    private val isOrientation: Boolean
        get() = App.getAppContext().resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT
    private val isInteractive: Boolean
        get() = !(App.getAppContext().getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
    var advert_click_leave = false
    var advert_leave = false
    private var executeJB: Job? = null
    private var executeJBO: Job? = null
    private var executeJBT: Job? = null
    private val ntTaskScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var requestCode = 101010
    // ======================= TASK =========================
    fun ntTaskJob(){
        ntTaskT()
        ntTaskO()
        MainScope().launch {
            delay(38.seconds)
            if (executeJBO?.isActive == false) {
                ntTaskT()
            }
            if (executeJBT?.isActive == false) {
                ntTaskO()
            }
        }
    }
    fun startNtWorkMgrTask(){
        WorkManager.getInstance(App.getAppContext()).cancelAllWork()
        WorkManager.getInstance(App.getAppContext()).enqueueUniqueWork(
            "SAVE_VIDEO", ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NtWorkTask>().setInitialDelay(60, TimeUnit.SECONDS).build()
        )
        WorkManager.getInstance(App.getAppContext()).enqueueUniqueWork(
            "SAVE_VIDEO-15", ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NtWorkTask>().setInitialDelay(15*60, TimeUnit.SECONDS).build()
        )
    }
    fun showLeaveNt(){
        if (!advert_leave){
            return
        }
        advert_leave = false
        ntTaskScope.launch {
            delay(200)
            executePushNt(NotifyType.LEAVER)
        }
    }
    fun showLeaveClickNt(){
        if (advert_click_leave) {
            advert_click_leave = false
            ntTaskScope.launch {
                delay(200)
                executePushNt(NotifyType.CLICK_LEAVER)
            }
        }
        executeJB = ntTaskScope.launch {
            delay(1.minutes)
            executePushNt(NotifyType.ONE_MINUTE)
        }
    }
    fun removeLeaveClickNt() {
        executeJB?.cancel()
    }
    private fun ntTaskT() {
        ntTaskScope.launch {
            delay(5050L)
            while (isActive) {
                launch {
                    executePushNt(NotifyType.NORMAL)
                }
                delay(38038)
            }
        }
    }
    private fun ntTaskO() {
        ntTaskScope.launch {
            while (isActive) {
                launch {
                    executePushNt(NotifyType.NORMAL)
                }
                delay(10008)
            }
        }
    }
    // ======================= TASK =========================





    suspend fun executePushNt(type: NotifyType) {
        runCatching {
            type.mutex.withLock {
                if (!isShowNtValid(type)) {
                    return@withLock
                }
                val ntData = buildNTData(type)
                val manager = buildNtMgr(type, ntData)
                val (bRv, sRv, fRv) = createRV(type, ntData)
                updateLastNtTime(type)
                val nt = buildNt(ntData, type, sRv, bRv, fRv)
                showNt(manager, ntData, nt)
                TrackMgr.instance.trackEvent(TrackEventType.safedddd_tzdy, mapOf("safedddd" to ntData.params, "safeddddsj" to type.params))
            }
        }
    }
    private fun buildNtMgr(type: NotifyType, ntData: NotifyData): NotificationManagerCompat {
        val manager = NotificationManagerCompat.from(App.getAppContext())
        manager.cancel(ntData.id)
        if (manager.getNotificationChannel(type.notifyChannelId) == null) {
            val channel = NotificationChannel(
                type.notifyChannelId,
                type.notifyChannelName,
                NotificationManager.IMPORTANCE_HIGH
            ).also {
                it.enableVibration(true)
                it.setSound(null, null)
                it.setShowBadge(true)
                it.enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }

        return manager
    }
    // ======================= NOTIFICATION DATA =========================
    private fun updateLastNtTime(type: NotifyType) {
        if (type == NotifyType.HOME || type == NotifyType.RECENT) {
            NotifyType.HOME.lastTime = System.currentTimeMillis()
            NotifyType.RECENT.lastTime = System.currentTimeMillis()
            NotifyType.NORMAL.lastTime = System.currentTimeMillis()
        } else {
            type.lastTime = System.currentTimeMillis()
        }
    }
    private fun buildNTData(type: NotifyType): NotifyData {
        return when (type) {
            NotifyType.CLICK_LEAVER -> NotifyMsgByMutLg.instance.getClickLeaverNotifyMsg()
            NotifyType.LEAVER -> NotifyMsgByMutLg.instance.getLeaverNotifyMsg()
            NotifyType.ONE_MINUTE -> NotifyMsgByMutLg.instance.getOneMinuteNotifyMsg()
            else -> NotifyMsgByMutLg.instance.normalNotifyMsg()
        }
    }
    // ======================= NOTIFICATION DATA =========================
    // ======================= NOTIFICATION VALID CHECK =========================
    private fun isShowNtValid(type: NotifyType): Boolean{
        LogUtils.d("Notify", "${type.name}:开始触发通知展示判断")
        if (!NotificationManagerCompat.from(App.getAppContext()).areNotificationsEnabled()) {
            LogUtils.d("Notify", "${type.name}:未开启通知权限，不展示")
            return false
        }
        if (type != NotifyType.HOME && type != NotifyType.RECENT && type != NotifyType.CLICK_LEAVER) {
            if (App.isAppInForeground) {
                LogUtils.d("Notify", "${type.name}:应用内，不展示")
                return false
            }
        }
        if (isLocked) {
            LogUtils.d("Notify", "${type.name}:屏幕锁了，不展示")
            return false
        }
        if (isInteractive) {
            LogUtils.d("Notify", "${type.name}:屏幕黑屏，不展示")
            return false
        }

        if (isOrientation) {
            LogUtils.d("Notify", "${type.name}:屏幕横屏，不展示")
            return false
        }
        if (!checkNtConditionMet(type)) {
            LogUtils.d("Notify", "${type.name}:距离间隔时间不够，不展示")
            return false
        }
        return true
    }
    private fun checkNtConditionMet(type: NotifyType): Boolean{
        val installTime = App.getAppContext().packageManager.getPackageInfo(App.getAppContext().packageName, 0).firstInstallTime
        val ntCache = AppCache.nt
        val ntData = App.getAppContext().jsonParser().decodeFromString<Nt>(ntCache)
        val isNtPushMet = when (type) {
            NotifyType.NORMAL -> {
                val time = System.currentTimeMillis() - type.lastTime
                LogUtils.d("Notify", "${type.name}:距离上次发送通知间隔时间：${time}")
                time >= ntData.sNTime * DateUtils.MINUTE_IN_MILLIS
            }
            NotifyType.RECENT, NotifyType.HOME  ->{
                val time = System.currentTimeMillis() - type.lastTime
                LogUtils.d("Notify", "${type.name}:距离上次发送通知间隔时间：${time},是否展示通知:${if (ntData.rhN == "y") "展示" else "不展示"}")
                ntData.rhN == "y" && time >= ntData.rhNTime * DateUtils.MINUTE_IN_MILLIS
            }
            NotifyType.UNLOCK ->{
                val distanceTime = System.currentTimeMillis() - installTime
                val sendTime = System.currentTimeMillis() - type.lastTime
                LogUtils.d("Notify", "${type.name}:距离应用安装间隔时间：${distanceTime}")
                LogUtils.d("Notify", "${type.name}:距离上次发送通知间隔时间：${sendTime},是否展示通知:${if (ntData.lockN == "y") "展示" else "不展示"}")
                ntData.lockN == "y" && distanceTime >= 5* DateUtils.MINUTE_IN_MILLIS && sendTime >= ntData.lockNTime * DateUtils.MINUTE_IN_MILLIS
            }

            NotifyType.CLICK_LEAVER->{
                LogUtils.d("Notify", "${type.name}:是否展示通知:${if (ntData.oAdB == "y") "展示" else "不展示"}")
                ntData.oAdB == "y"
            }
            NotifyType.LEAVER ->{
                LogUtils.d("Notify", "${type.name}:是否展示通知:${if (ntData.oB == "y") "展示" else "不展示"}")
                ntData.oB == "y"
            }
            NotifyType.ONE_MINUTE ->{
                LogUtils.d("Notify", "${type.name}:是否展示通知:${if (ntData.hB == "y") "展示" else "不展示"}")
                ntData.hB == "y"
            }
        }
        return isNtPushMet
    }
    // ======================= NOTIFICATION VALID CHECK  =========================


    // ======================= NOTIFICATION BASE CONFIG  =========================
    private fun showNt(
        mgr: NotificationManagerCompat,
        ntData: NotifyData,
        notification: NotificationCompat.Builder
    ) {
        if (!mgr.areNotificationsEnabled()) {
            return
        }
        try {
            mgr.notify(ntData.id, notification.build())
        } catch (e: SecurityException) {
        }
    }
    private fun buildNt(ntData: NotifyData,type: NotifyType,sRv: RemoteViews?,bRv: RemoteViews?,fRv: RemoteViews?): NotificationCompat.Builder{
        val intent = generateNtIntent(type, ntData)
        return NotificationCompat.Builder(App.getAppContext(), type.notifyChannelId).apply {
            setSmallIcon(R.mipmap.ic_logo)
            setAutoCancel(true)
            setWhen(System.currentTimeMillis())
            setGroup("${System.currentTimeMillis()}${type.name}")
            setContentTitle(ntData.notifyTitle)
            setContentText(ntData.notifyContent.toString())
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            priority = NotificationCompat.PRIORITY_MAX
            setNumber(1)
            setVivoNumber(ntData.id)
            setContentIntent(intent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                sRv?.let {
                    setCustomContentView(it)
                    val headsUpView = if (type != NotifyType.CLICK_LEAVER && type != NotifyType.LEAVER && type != NotifyType.ONE_MINUTE) {
                        fRv
                    } else {
                        sRv
                    }
                    setCustomHeadsUpContentView(headsUpView)
                    setContent(sRv)
                }
                setCustomBigContentView(bRv)
            } else {
                setCustomContentView(bRv)
                setCustomHeadsUpContentView(bRv)
                setContent(bRv)
                setCustomBigContentView(bRv)
            }
        }
    }
    private fun generateNtIntent(type: NotifyType,ntData: NotifyData): PendingIntent {
        val requestCode = requestCode++
        val intent = Intent(App.getAppContext(), SplashActivity::class.java).apply {
            putExtra("ntType", 2)
            putExtra("ntParam", ntData.params.toString())
            putExtra("ntTime", type.params)
            putExtra("ntId", ntData.id)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(
            App.getAppContext(),
            requestCode,
            intent,
            flags
        )
    }
    private fun createRV(
        type: NotifyType,
        data: NotifyData
    ): Triple<RemoteViews, RemoteViews?, RemoteViews?> {
        val bigRemoteView = generateBigRV(type, data)
        val smallRemoteViews = generateSmallRv( data,type)
        val floatRemoteViews = generateNormalFloatRv(data,type)

        return Triple(bigRemoteView, smallRemoteViews, floatRemoteViews)
    }
    private fun generateNormalFloatRv(ntData: NotifyData,type: NotifyType): RemoteViews?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            RemoteViews(
                App.getAppContext().packageName,
                R.layout.n_normal_f
            ).apply {
                setImageViewResource(R.id.iv_normal, ntData.iconSR)
                setTextViewText(R.id.tv_normal_title, ntData.notifyTitle)
                setTextViewText(R.id.tv_normal_btn, ntData.action)
                if (type != NotifyType.CLICK_LEAVER && type != NotifyType.LEAVER && type != NotifyType.ONE_MINUTE) {
                    setTextViewText(R.id.tv_normal_des, ntData.notifyContent)
                }
            }
        }else {
            null
        }
    }
    private fun generateSmallRv(data: NotifyData,type: NotifyType): RemoteViews? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            generateSmallR(type).apply {
                setImageViewResource(R.id.iv_normal, data.iconSR)
                setTextViewText(R.id.tv_normal_title, data.notifyTitle)
                setTextViewText(R.id.tv_normal_btn, data.action)
                if (type != NotifyType.CLICK_LEAVER && type != NotifyType.LEAVER && type != NotifyType.ONE_MINUTE) {
                    setTextViewText(R.id.tv_normal_des, data.notifyContent)
                }
            }
        } else {
            null
        }
    }
    private fun generateBigRV(type: NotifyType, data: NotifyData): RemoteViews {
        return generateBigR(type).apply {
            setImageViewResource(R.id.iv_normal, data.iconBR)
            setTextViewText(R.id.tv_normal_title, data.notifyTitle)
            setTextViewText(R.id.tv_normal_btn, data.action)
            if (type != NotifyType.CLICK_LEAVER && type != NotifyType.LEAVER && type != NotifyType.ONE_MINUTE) {
                setTextViewText(R.id.tv_normal_des, data.notifyContent)
            }
        }
    }
    private fun generateSmallR(type: NotifyType): RemoteViews {
        return if (type == NotifyType.CLICK_LEAVER || type == NotifyType.LEAVER ||  type == NotifyType.ONE_MINUTE) {
            RemoteViews(
                App.getAppContext().packageName,
                R.layout.n_retain_s
            )
        } else {
            RemoteViews(
                App.getAppContext().packageName,
                R.layout.n_normal_s
            )
        }
    }
    private fun generateBigR(type: NotifyType): RemoteViews {
        return if (type == NotifyType.CLICK_LEAVER || type == NotifyType.LEAVER ||  type == NotifyType.ONE_MINUTE) {
            RemoteViews(
                App.getAppContext().packageName,
                R.layout.n_retain_b
            )
        } else {
            RemoteViews(
                App.getAppContext().packageName,
                R.layout.n_normal_b
            )
        }

    }
    // ======================= NOTIFICATION BASE CONFIG  =========================
}