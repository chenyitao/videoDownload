package com.download.video_download.base.config.cg

import android.util.Base64
import com.download.video_download.base.utils.LogUtils
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.toByteArray

object Crypt {
    private fun aesEncrypt(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }
    private fun aesDecrypt(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }
    fun String.swapCaseLetter(): String {
        return this.map { c ->
            when {
                c.isUpperCase() -> c.lowercaseChar()
                c.isLowerCase() -> c.uppercaseChar()
                else -> c
            }
        }.joinToString("")
    }
    fun  paramsEncrypt(params: JSONObject): String {
        runCatching {
            val jsonStr = params.toString()
            val step1Base64 = Base64.encodeToString(jsonStr.toByteArray(), Base64.NO_WRAP)
            val aesKeyBytes = Base64.decode("tZTjValb2BynAg7Y8iPRSE==", Base64.NO_WRAP)
            val step2AesEncrypted = aesEncrypt(step1Base64.toByteArray(), aesKeyBytes)
            val step2Base64 = Base64.encodeToString(step2AesEncrypted, Base64.NO_WRAP)
            val step3Str = step2Base64.map { c ->
                (c.code + 5).toChar()
            }.joinToString("")
            val step4Base64 = Base64.encodeToString(step3Str.toByteArray(), Base64.NO_WRAP)
            val step5SwapCase = step4Base64.swapCaseLetter()
            val step6Reversed = step5SwapCase.reversed()
            val prefix = "a"
            val suffix = "1234567"
            val finalEncrypted = prefix + step6Reversed + suffix
            return finalEncrypted
        }
        return ""
    }
    fun paramsDecrypt(params: String): JSONObject {
        runCatching {
            var decodedStr = params
            if (decodedStr.length < 10) {
                return@runCatching null
            }
            decodedStr = decodedStr.substring(7, decodedStr.length - 3)
            decodedStr = exchange9Chars(decodedStr)
            decodedStr = decodedStr.reversed()
            decodedStr = decodedStr.map { c ->
                (c.code - 4).toChar()
            }.joinToString("")
            val step6Bytes = Base64.decode(decodedStr, Base64.NO_WRAP)
            decodedStr = String(step6Bytes, Charsets.UTF_8)
            decodedStr = decodedStr.swapCaseLetter()
            val step8Bytes = Base64.decode(decodedStr, Base64.NO_WRAP)
            val aesKeyBytes = Base64.decode("yoKPOFRSjbAeWkMJ7ZXnq8==", Base64.NO_WRAP)
            val step9DecryptedBytes = aesDecrypt(step8Bytes, aesKeyBytes)
            val step10Str = String(step9DecryptedBytes)
            return  JSONObject(step10Str)
        }
        return JSONObject()
    }
    private fun exchange9Chars(str: String): String {
        val len = str.length
        return if (len <= 18) {
            val l9 = if (len >=9) str.take(9) else str
            val f9 = if (len >9) str.substring(9) else ""
            f9 + l9
        } else {
            val l9 = str.take(9)
            val middle = str.substring(9, len -9)
            val f9 = str.substring(len -9)
            f9 + middle + l9
        }
    }
}