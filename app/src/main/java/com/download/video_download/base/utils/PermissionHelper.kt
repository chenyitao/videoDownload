package com.download.video_download.base.utils
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle

class PermissionHelper private constructor(private val activity: ComponentActivity) {
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var permissionCallback: ((Map<String, Boolean>, Boolean) -> Unit)? = null

    init {
        initPermissionLauncher()
    }
    private fun initPermissionLauncher() {
        val currentState = activity.lifecycle.currentState
        check(currentState == Lifecycle.State.INITIALIZED || currentState == Lifecycle.State.CREATED) {
            "PermissionHelper 必须在Activity.onCreate()中同步初始化！当前生命周期状态：$currentState"
        }
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            val allGranted = permissionsMap.all { it.value }
            permissionCallback?.invoke(permissionsMap, allGranted)
            permissionCallback = null
        }
    }

    fun checkSinglePermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkMultiplePermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { checkSinglePermission(context, it) }
    }

    fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }

    fun requestPermissions(
        permissions: Array<String>,
        callback: (permissionsResult: Map<String, Boolean>, allGranted: Boolean) -> Unit
    ) {
        val allGranted = checkMultiplePermissions(activity, permissions)
        if (allGranted) {
            val resultMap = permissions.associateWith { true }
            callback(resultMap, true)
            return
        }
        permissionCallback = callback
        permissionLauncher?.launch(permissions)
    }

    companion object {
        fun with(activity: ComponentActivity): PermissionHelper {
            return PermissionHelper(activity)
        }
    }
    fun checkNtPermission(): Boolean{
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            return true
        }
        val check = ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED || shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        return check
    }
}