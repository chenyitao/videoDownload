package com.download.video_download.base.utils

import android.app.Activity
import java.util.Stack

/**
 * Activity管理器
 * 用于统一管理所有Activity的生命周期
 */
object ActivityManager {
    
    private val activityStack = Stack<Activity>()
    
    /**
     * 添加Activity到栈中
     */
    fun addActivity(activity: Activity) {
        activityStack.push(activity)
    }
    
    /**
     * 从栈中移除Activity
     */
    fun removeActivity(activity: Activity) {
        activityStack.remove(activity)
    }
    
    /**
     * 获取当前Activity
     */
    fun currentActivity(): Activity? {
        return if (activityStack.isEmpty()) null else activityStack.lastElement()
    }
    
    /**
     * 结束指定Activity
     */
    fun finishActivity(activity: Activity?) {
        activity?.let {
            if (!it.isFinishing) {
                activityStack.remove(it)
                it.finish()
            }
        }
    }
    
    /**
     * 结束指定类名的Activity
     */
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
    
    /**
     * 结束所有Activity
     */
    fun finishAllActivity() {
        for (activity in activityStack) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activityStack.clear()
    }
    
    /**
     * 获取Activity栈大小
     */
    fun getActivityCount(): Int {
        return activityStack.size
    }
    
    /**
     * 检查是否存在指定Activity
     */
    fun isActivityExists(cls: Class<*>): Boolean {
        return activityStack.any { it.javaClass == cls }
    }
    
    /**
     * 退出应用程序
     */
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