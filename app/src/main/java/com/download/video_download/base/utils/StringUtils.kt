package com.download.video_download.base.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.graphics.toColorInt

object StringUtils {
    fun boldTargetSubStr(originalStr: String, targetSubStr: String): SpannableString {
        val spannableString = SpannableString(originalStr)
        val startIndex = originalStr.indexOf(targetSubStr)
        if (startIndex == -1) {
            return spannableString
        }
        val endIndex = startIndex + targetSubStr.length

        val boldSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(
            boldSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }
    fun buildTargetNotifyContent(content: String, replaceString: String): SpannableString {
        val index = content.indexOf(replaceString)
        return try {
            if (index == -1) SpannableString(content) else SpannableString(content).apply {
                this@apply.setSpan(
                    ForegroundColorSpan("#AF181E".toColorInt()),
                    index,
                    index + replaceString.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                this@apply.setSpan(
                    StyleSpan(Typeface.BOLD),
                    index,
                    index + replaceString.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } catch (_: Exception) {
            SpannableString(content)
        }
    }
}