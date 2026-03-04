package com.download.video_download.base.nt

import kotlinx.coroutines.sync.Mutex

enum class NotifyType(
    val notifyChannelId: String,
    val notifyChannelName: String,
    val position: String,
    val params: String,
    var lastTime: Long,
    val mutex: Mutex = Mutex()
) {
    NORMAL("SaveVideo_Notify","SaveVideo","normal", "1",0L),
    RECENT("SaveVideo_Notify","SaveVideo","recent", "2",0L),
    HOME("SaveVideo_Notify","SaveVideo","home", "3",0L),
    UNLOCK("SaveVideo_Notify","SaveVideo","unlock", "4",0L),
    CLICK_LEAVER("SaveVideo_Notify","SaveVideo","click_leaver", "5",0L),
    ONE_MINUTE("SaveVideo_Notify","SaveVideo","one_minute", "6",0L),
    LEAVER("SaveVideo_Notify","SaveVideo","leave", "7",0L);
}