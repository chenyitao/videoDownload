package com.download.video_download.base.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PUtils {
    private val STORAGE_PERMISSIONS = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    const val REQUEST_CODE_STORAGE = 1001
    fun hasStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                activity,
                STORAGE_PERMISSIONS,
                REQUEST_CODE_STORAGE
            )
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CODE_STORAGE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}