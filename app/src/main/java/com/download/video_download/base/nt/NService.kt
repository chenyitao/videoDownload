package com.download.video_download.base.nt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.model.FrontData
import com.download.video_download.base.utils.AppCache
import com.download.video_download.ui.activity.SplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class NService: Service() {
    private var scheduledJob: Job? = null
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        beginFS()
        serviceExist = true
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.getIntExtra(KEY, 0) == 1) {
            beginFS()
        }
        startNtRefJob()
        return START_STICKY
    }
    private fun isRefreshNotify(): Boolean {
        return NotificationManagerCompat.from(App.getAppContext()).areNotificationsEnabled() && !NtMgr.instance.isLocked

    }
    private suspend fun notifyRfCycle(){
        if (isRefreshNotify()) {
            delay(AppCache.fNTime.minutes)
            beginFS()
        } else {
            delay(1.minutes)
        }
    }
    private fun startNtRefJob() {
        if (scheduledJob?.isActive != true) {
            scheduledJob = ntScope.launch {
                ntRefLoop()
            }
        }
    }
    private suspend fun ntRefLoop() {
        coroutineScope {
            while (coroutineContext[Job]?.isActive == true) {
                notifyRfCycle()
                delay(1000)
            }
        }
    }
    private fun beginFS(){
        val remoteViews = generateRView().also { rv ->
            rv.setTextViewText(R.id.enter_video_url, App.getAppContext().getString(R.string.enter_video_url))
            rv.setOnClickPendingIntent(R.id.iv_msg_dot, clickBtPendingIntent())
            rv.setOnClickPendingIntent(R.id.enter_video_url, enterPendingIntent())
        }
        val notification = buildFrontNotification(remoteViews).build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(FOREGROUND_N_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(FOREGROUND_N_ID, notification)
            }
        } catch (_: Throwable) {  }
    }
    private fun buildFrontNotification(remoteViews: RemoteViews): NotificationCompat.Builder {
        val data = FrontData("","")
        return NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo)
            .setWhen(System.currentTimeMillis())
            .setOngoing(AppCache.og != "y")
            .setSound(null)
            .setContentTitle(data.title)
            .setContentText(data.action)
            .setContent(remoteViews)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup("${System.currentTimeMillis()}_${NOTIFY_CHANNEL_NAME}")
            .setUsesChronometer(false)
            .setSilent(true)
            .setNumber(1)
            .setOnlyAlertOnce(true)
    }
    private fun generateRView(): RemoteViews {
        return RemoteViews(packageName, R.layout.n_scheduled).apply {
            setViewVisibility(R.id.iv_msg_dot, if (isShowMsgDot) View.VISIBLE else View.GONE)
        }
    }
    private fun clickBtPendingIntent(): PendingIntent {
        val intent = Intent(this, SplashActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra("param", "fn")
            putExtra("notify_channel_id", NOTIFY_CHANNEL_ID)
            putExtra("data", "customscheme://download/${System.currentTimeMillis()}".toUri())
        }
        return PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE
                    or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private fun enterPendingIntent(): PendingIntent {
        val intent = Intent(this, SplashActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra("param", "enter_url")
            putExtra("notify_channel_id", NOTIFY_CHANNEL_ID)
            putExtra("data", "customscheme://ipt/${System.currentTimeMillis()}".toUri())
        }
        return PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE
                    or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    companion object{
        var isShowMsgDot: Boolean =  true
        private const val NOTIFY_CHANNEL_ID = "save_video_front_notify"
        private const val NOTIFY_CHANNEL_NAME = "Save_Video_FN"
        private const val FOREGROUND_N_ID = 33
        var serviceExist: Boolean = false
        private const val KEY = "notify_ref"
        private const val VALUE = 1
        private val ntScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        private fun starNtConditionsMet(context: Context, referValue: Int? = null) {
            runCatching {
                Looper.getMainLooper().queue.addIdleHandler {
                    if (canStartNtService()) {
                        val intent = Intent(context, NService::class.java)
                        referValue?.let { intent.putExtra(KEY, it) }
                        try {
                            ContextCompat.startForegroundService(context, intent)
                        } catch (_: Throwable) {}
                    }
                    false
                }
            }
        }
        private fun canStartNtService(): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || App.isAppInForeground
        }
        private fun generateNtChannel(mgr: NotificationManagerCompat) {
            if (mgr.getNotificationChannel(NOTIFY_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    NOTIFY_CHANNEL_ID,
                    NOTIFY_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).also {
                    it.enableVibration(false)
                    it.setSound(null, null)
                    it.enableLights(true)
                    it.setShowBadge(true)
                }
                mgr.createNotificationChannel(channel)
            }
        }
        fun createNt(){
            if (!NotificationManagerCompat.from(App.getAppContext()).areNotificationsEnabled() || serviceExist)
                return
            val manager = NotificationManagerCompat.from(App.getAppContext())
            generateNtChannel(manager)
            starNtConditionsMet(App.getAppContext())
        }
        fun updateNt(msgDot: Boolean = true) {
            if (!NotificationManagerCompat.from(App.getAppContext()).areNotificationsEnabled())
                return
            if (App.isColdStart){
                TrackMgr.instance.trackEvent(TrackEventType.safedddd_czzs)
            }
            val manager = NotificationManagerCompat.from(App.getAppContext())
            generateNtChannel(manager)
            starNtConditionsMet(App.getAppContext(), VALUE)
            this.isShowMsgDot = msgDot
        }
    }
}