package com.download.video_download.base.nt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SysKeyReceiver {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        val instance: SysKeyReceiver by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SysKeyReceiver()
        }
        private  val PRIORITY = 88888
        private  val SYS_HOMEKEY = "homekey"
        private  val SYS_RECENTAPPS = "recentapps"
    }
    private fun createIntentFilter(): IntentFilter = IntentFilter().apply {
        priority = PRIORITY
        addAction(Intent.ACTION_USER_PRESENT)
        addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
    }
    fun startKeyReceiver(ctx: Context){
        val sysBroadcast = SysBroadcast()
        val intentFilter = createIntentFilter()

        ContextCompat.registerReceiver(
            ctx,
            sysBroadcast,
            intentFilter,
            ActivityCompat.RECEIVER_EXPORTED
        )
    }
    private inner class SysBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> processUP()
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> processCSD(intent)
            }
        }

        private fun processUP() {
            scope.launch {
                NtMgr.instance.executePushNt(NotifyType.UNLOCK)
            }
        }

        private fun processCSD(intent: Intent) {
            val reason = intent.getStringExtra("reason")
            SystemKey.fromValue(reason)?.let { dialogReason ->
                scope.launch {
                    val notifyPos = when (dialogReason) {
                        SystemKey.HomeKey -> NotifyType.HOME
                        SystemKey.RecentApps -> NotifyType.RECENT
                    }
                    if (notifyPos == NotifyType.HOME){
                        NtMgr.instance.showLeaveNt()
                    }
                    NtMgr.instance.executePushNt(notifyPos)
                }
            }
        }
    }
    sealed class SystemKey(val value: String) {
        object HomeKey : SystemKey(SYS_HOMEKEY)
        object RecentApps : SystemKey(SYS_RECENTAPPS)

        companion object {
            fun fromValue(value: String?): SystemKey? = when (value) {
                SYS_HOMEKEY -> HomeKey
                SYS_RECENTAPPS -> RecentApps
                else -> null
            }
        }
    }
}