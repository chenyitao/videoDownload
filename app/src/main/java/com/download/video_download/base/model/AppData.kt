package com.download.video_download.base.model

data class LanguageSelectData(
    val language: String,
    var isSelected: Boolean,  // 改为可变属性以支持选中状态更新
    val languageIv: Int,
    val languageCode: String,
)
data class GuideData(
    val title: String,
    val image: Int,
    val description: String
)