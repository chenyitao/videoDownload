package com.download.video_download.ui.viewmodel

import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseViewModel
import com.download.video_download.base.model.TagData

class FDViewModel: BaseViewModel(){
    fun getFeedbackData(): MutableList<TagData> {
        return mutableListOf(
            TagData(App.getAppContext().getString(R.string.not_user_friendly)),
            TagData(App.getAppContext().getString(R.string.ads_are_over)),
            TagData(App.getAppContext().getString(R.string.too_many_notifications)),
            TagData(App.getAppContext().getString(R.string.unable_save_videos)),
            TagData(App.getAppContext().getString(R.string.hd_download_not_working)),
            TagData(App.getAppContext().getString(R.string.saved_videos_won_open)),
            TagData(App.getAppContext().getString(R.string.other_problem)),
        )
    }
}