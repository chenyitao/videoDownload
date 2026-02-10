package com.download.video_download.ui.viewmodel

import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.model.GuideData

class GuideViewModel : BaseViewModel() {
    fun getGuideList(): MutableList<GuideData> {
        val list = mutableListOf<GuideData>()
        val guide1 = GuideData(
            title = App.getAppContext().getString(R.string.guide1_title),
            image = R.mipmap.bg_g_1,
            description = ""
        )
        val guide2 = GuideData(
            title = App.getAppContext().getString(R.string.guide2_title),
            image = R.mipmap.bg_g_2,
            description = ""
        )
        val guide3 = GuideData(
            title = App.getAppContext().getString(R.string.guide3_title),
            image = R.mipmap.bg_g_3,
            description = App.getAppContext().getString(R.string.guide3_des)
        )
        list.add(guide1)
        list.add(guide2)
        list.add(guide3)
        return list
    }
}