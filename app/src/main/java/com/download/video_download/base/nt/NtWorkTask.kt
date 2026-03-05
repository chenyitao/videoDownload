package com.download.video_download.base.nt

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NtWorkTask(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
    override fun doWork(): Result {
        runCatching {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                NtMgr.instance.executePushNt(NotifyType.NORMAL)
            }
            NtMgr.instance.startNtWorkMgrTask()
        }
        return Result.success()
    }
}