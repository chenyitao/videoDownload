package com.download.video_download.base.utils

import android.content.Context
import android.content.res.Resources
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.also
import kotlin.io.use

object RawResourceUtils {

    fun copyRawVideoToSystemPath(
        context: Context,
        resourceId: Int,
        fileName: String,
        directory: String = Environment.DIRECTORY_DOWNLOADS
    ): Boolean {
        return try {
            val externalDir = context.getExternalFilesDir(directory)
            
            if (externalDir == null) {
                Log.e("RawResourceUtils", "")
                return false
            }
            
            if (!externalDir.exists()) {
                if (!externalDir.mkdirs()) {
                    return false
                }
            }
            
            val targetFile = File(externalDir, fileName)
            
            context.resources.openRawResource(resourceId).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            
            true
        } catch (e: IOException) {
            false
        } catch (e: Resources.NotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun copyRawVideoToPrivatePath(
        context: Context,
        resourceId: Int,
        fileName: String,
        subDir: String? = null
    ): Boolean {
        return try {
            var targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
            if (targetDir == null) {
                return false
            }
            
            val targetFile = File(targetDir, fileName)
            
            context.resources.openRawResource(resourceId).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            
            true
        } catch (e: IOException) {
            false
        } catch (e: Resources.NotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun getRawResourceId(context: Context, resourceName: String, resourceType: String = "raw"): Int {
        return context.resources.getIdentifier(resourceName, resourceType, context.packageName)
    }
}