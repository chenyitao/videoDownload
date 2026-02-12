package com.download.video_download.base.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.apply
import kotlin.takeIf
import kotlin.text.format

class TimeFormateUtils {
    
    companion object {
        fun formatTime(milliseconds: Long): String {
            val totalSeconds = milliseconds / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

        fun formatTime(milliseconds: Long, forceShowHours: Boolean = false): String {
            val totalSeconds = milliseconds / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            return if (forceShowHours || hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
        fun isSameDayApi26(time1: Long, time2: Long): Boolean {
            val zoneId = ZoneId.systemDefault()
            val date1 = Instant.ofEpochMilli(time1).atZone(zoneId).toLocalDate()
            val date2 = Instant.ofEpochMilli(time2).atZone(zoneId).toLocalDate()
            return date1.isEqual(date2)
        }
    }

}