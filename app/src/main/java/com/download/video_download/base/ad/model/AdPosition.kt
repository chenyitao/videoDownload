package com.download.video_download.base.ad.model

enum class AdPosition(val code: Int, val desc: String) {
    LOADING(1, "loading"),
    LANGUAGE(2, "language"),
    GUIDE(3, "guide"),
    HOME(4, "home"),
    SEARCH(5, "search"),
    START_DOWNLOAD(6, "startDownload"),
    BACK(7, "back"),
    TAB(8, "tab"),
    DOWNLOAD_TASK_DIALOG(9, "downloadTaskDialog"),
}