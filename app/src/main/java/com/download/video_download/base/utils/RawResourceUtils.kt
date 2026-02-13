package com.download.video_download.base.utils

import android.content.Context
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.MetadataRetriever
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
    fun getLocalVideoDurationQuickly(context: Context, videoPath: String): Long {
        val videoFile = File(videoPath)
        if (!videoFile.exists() || !videoFile.isFile) {
            return 0L
        }

        val retriever = MediaMetadataRetriever()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                retriever.setDataSource(videoPath)
            } else {
                retriever.setDataSource(videoFile.absolutePath)
            }

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            retriever.release()
        }
    }
}