package com.download.video_download.base.config.utils

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.download.video_download.BuildConfig
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

object CfUtils {
    private val PACKAGE_LETTER_POOL = ('a'..'z').toList()
    private val NUMBER_POOL = ('0'..'9').toList()
    private val random: Random = ThreadLocalRandom.current()
    fun generateDistinctId (context: Context): String {
        val packageName = context.packageName
        val packagePart = getRandomPackage4L(packageName, 4)

        val timestampPart = System.currentTimeMillis().toString()

        val letterPart = generateLetters(4)

        val numberPart = generateNumbers(2)

        return "$packagePart$timestampPart$letterPart$numberPart"
    }
    private fun getRandomPackage4L(packageName: String, length: Int): String {
        val sb = StringBuilder()
        val packageChars = packageName.toCharArray()

        if (packageChars.isEmpty()) {
            return generateLetters(length)
        }

        repeat(length) {
            val randomIndex = random.nextInt(packageChars.size)
            sb.append(packageChars[randomIndex])
        }
        return sb.toString()
    }
    fun generateLetters(length: Int): String {
        val sb = StringBuilder()
        repeat(length) {
            val index = random.nextInt(PACKAGE_LETTER_POOL.size)
            sb.append(PACKAGE_LETTER_POOL[index])
        }
        return sb.toString()
    }
    fun generateNumbers(length: Int): String {
        val sb = StringBuilder()
        repeat(length) {
            val index = random.nextInt(NUMBER_POOL.size)
            sb.append(NUMBER_POOL[index])
        }
        return sb.toString()
    }
    fun getManufacturer(): String {
        return try {
            val manufacturer = Build.MANUFACTURER
            if (TextUtils.isEmpty(manufacturer)) "Unknown" else manufacturer
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }
    fun getDeviceModel(): String {
        return try {
            val model = Build.MODEL
            if (TextUtils.isEmpty(model)) "Unknown Model" else model.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown Model"
        }
    }
    fun getOSVersionName(): String {
        return try {
            val version = Build.VERSION.RELEASE
            if (TextUtils.isEmpty(version)) "Unknown" else version.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }
    fun getVersionName(context: Context): String {
        return BuildConfig.VERSION_NAME
    }
}