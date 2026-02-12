package com.download.video_download.base.utils

import android.content.Context
import android.util.Base64
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
object DESUtil {
    private const val DES_ALGORITHM_CBC = "DES/CBC/PKCS5Padding"
    private const val DES_KEY_ALGORITHM = "DES"

    fun decryptCBC(encryptedData: String): String {
        return try {
            val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)

            val desKeySpec = DESKeySpec("zCnupXql".toByteArray(charset("UTF-8")))
            val keyFactory = SecretKeyFactory.getInstance(DES_KEY_ALGORITHM)
            val secretKey: SecretKey = keyFactory.generateSecret(desKeySpec)
            val ivSpec = IvParameterSpec("fcoph6h9".toByteArray(charset("UTF-8")))
            val cipher = Cipher.getInstance(DES_ALGORITHM_CBC)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, charset("UTF-8"))
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    fun readTxtFileSync(
        context: Context,
        fileName: String,
        charset: String = "UTF-8"
    ): String {
        val stringBuilder = StringBuilder()
        try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, charset)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append("\n")
                    }
                }
            }
            return if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1).toString()
            } else {
                ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
    suspend fun readTxtFileCoroutine(
        context: Context,
        fileName: String,
        charset: String = "UTF-8"
    ): String = kotlinx.coroutines.Dispatchers.IO.run {
        readTxtFileSync(context.applicationContext, fileName, charset)
    }
}