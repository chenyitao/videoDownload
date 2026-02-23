package com.download.video_download.base.utils

import android.app.Activity
import java.util.Stack

object ActivityManager {
    
    private val activityStack = Stack<Activity>()

    fun addActivity(activity: Activity) {
        activityStack.push(activity)
    }

    fun removeActivity(activity: Activity) {
        activityStack.remove(activity)
    }

    fun currentActivity(): Activity? {
        return if (activityStack.isEmpty()) null else activityStack.lastElement()
    }

    fun finishActivity(activity: Activity?) {
        activity?.let {
            if (!it.isFinishing) {
                activityStack.remove(it)
                it.finish()
            }
        }
    }

    fun finishActivity(cls: Class<*>) {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            if (activity.javaClass == cls) {
                iterator.remove()
                if (!activity.isFinishing) {
                    activity.finish()
                }
            }
        }
    }

    fun finishAllActivity() {
        for (activity in activityStack) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activityStack.clear()
    }

    fun getActivityCount(): Int {
        return activityStack.size
    }

    fun isActivityExists(cls: Class<*>): Boolean {
        return activityStack.any { it.javaClass == cls }
    }

    fun exitApp() {
        try {
            finishAllActivity()
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}