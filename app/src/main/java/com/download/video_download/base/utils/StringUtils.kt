package com.download.video_download.base.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

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
}