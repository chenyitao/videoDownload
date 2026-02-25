package com.download.video_download.base.config.cg

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Crypt {
    private fun aesEncrypt(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
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
        val jsonParams = params.toString()
        val paramsBase64 = Base64.encodeToString(jsonParams.toByteArray(), Base64.NO_WRAP)
        val aesKey = Base64.decode("tZTjValb2BynAg7Y8iPRSE==", Base64.NO_WRAP)
        val paramsAes =
           aesEncrypt(paramsBase64.toByteArray(), aesKey)
        val paramsAesBase64 = Base64.encodeToString(paramsAes, Base64.NO_WRAP)
        val pAesBase64Ascii = paramsAesBase64.map { c ->
            (c.code + 5).toChar()
        }.joinToString("")
        val asciiBase64 = Base64.encodeToString(pAesBase64Ascii.toByteArray(), Base64.NO_WRAP)
        asciiBase64.swapCaseLetter()
        val asciiBase64Rever = asciiBase64.reversed()
        val prefix = "a"
        val suffix = "1234567"
        return prefix + asciiBase64Rever + suffix
    }

    fun paramsDecrypt(params: String): JSONObject {
        if (params.isEmpty()){
            return JSONObject()
        }
        val decryptedParams = String(params.toByteArray(), StandardCharsets.UTF_8)
        if (decryptedParams.length < 11) {
            return JSONObject()
        }
        val paramsSub = decryptedParams.substring(7, decryptedParams.length - 3)
        val paramsExchange = exchange9Chars(paramsSub)
        val paramsRever = paramsExchange.reversed()
        val paramsAsc = paramsRever.map { c ->
            (c.code - 4).toChar()
        }.joinToString("")
        var paramsB64 = String(Base64.decode(paramsAsc, Base64.DEFAULT), Charsets.UTF_8)
        paramsB64.swapCaseLetter()
        paramsB64 =  String(Base64.decode(paramsB64, Base64.DEFAULT), Charsets.UTF_8)
        val aesKey = Base64.decode("yoKPOFRSjbAeWkMJ7ZXnq8==", Base64.DEFAULT)
        val decryptedAes = aesEncrypt(paramsB64.toByteArray(), aesKey)
        val json = String(decryptedAes, StandardCharsets.UTF_8)
        return JSONObject(json)
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