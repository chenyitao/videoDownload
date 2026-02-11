package com.download.video_download.base

import androidx.lifecycle.ViewModel
import kotlinx.serialization.json.Json

open class BaseViewModel: ViewModel() {
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = false
        isLenient = true
    }
}