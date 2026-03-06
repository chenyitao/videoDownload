package com.download.video_download.base.nt

import android.text.SpannableString
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.model.NotifyData
import com.download.video_download.base.utils.StringUtils

class NotifyMsgByMutLg private constructor() {
    companion object {
        val instance: NotifyMsgByMutLg by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NotifyMsgByMutLg()
        }
    }
    var curMsgIndex = -1
    private fun nextNotifyData(size: Int): Int {
        curMsgIndex = if (curMsgIndex == -1) {
            0
        } else {
            (curMsgIndex + 1) % size
        }
        return curMsgIndex
    }
    fun normalNotifyMsg(): NotifyData {
        val notfyDataList = getCurLgNotifyMsg()
        return notfyDataList[nextNotifyData(notfyDataList.size)]
    }
    fun getClickLeaverNotifyMsg(): NotifyData {
        return NotifyData(
            1006,
            R.mipmap.ic_nt_r_s_sa,
            R.mipmap.ic_nt_r_sa,
            App.getAppContext().getString(R.string.nt_r_1_title),
            SpannableString(""),
            App.getAppContext().getString(R.string.nt_r_1_action),
            6
        )
    }
    fun getLeaverNotifyMsg(): NotifyData {
        return NotifyData(
            1007,
            R.mipmap.ic_nt_r_s_ols,
            R.mipmap.ic_nt_r_ols,
            App.getAppContext().getString(R.string.nt_r_2_title),
            SpannableString(""),
            App.getAppContext().getString(R.string.nt_r_2_action),
            7
        )
    }
    fun getOneMinuteNotifyMsg(): NotifyData {
        return NotifyData(
            1008,
            R.mipmap.ic_nt_r_s_rfsn,
            R.mipmap.ic_nt_r_rfsn,
            App.getAppContext().getString(R.string.nt_r_3_title),
            SpannableString(""),
            App.getAppContext().getString(R.string.nt_r_3_action),
            8
        )
    }
    private fun getCurLgNotifyMsg(): List<NotifyData>{
        val n1 = NotifyData(
            1001,
            R.mipmap.ic_nt_s_vp,
            R.mipmap.ic_nt_b_vp,
            App.getAppContext().getString(R.string.nt_1_title),
            StringUtils.buildTargetNotifyContent(
                App.getAppContext().getString(R.string.nt_1_content),
                App.getAppContext().getString(R.string.nt_1_sub_content)
            ),
            App.getAppContext().getString(R.string.nt_1_action),
            1
        )
        val n2 = NotifyData(
            1002,
            R.mipmap.ic_nt_s_fd,
            R.mipmap.ic_nt_b_fd,
            App.getAppContext().getString(R.string.nt_2_title),
            StringUtils.buildTargetNotifyContent(
                App.getAppContext().getString(R.string.nt_2_content),
                App.getAppContext().getString(R.string.nt_2_sub_content)
            ),
            App.getAppContext().getString(R.string.nt_2_action),
            2
        )
        val n3 = NotifyData(
            1003,
            R.mipmap.ic_nt_s_dms,
            R.mipmap.ic_nt_b_dms,
            App.getAppContext().getString(R.string.nt_3_title),
            StringUtils.buildTargetNotifyContent(
                App.getAppContext().getString(R.string.nt_3_content),
                App.getAppContext().getString(R.string.nt_3_sub_content)
            ),
            App.getAppContext().getString(R.string.nt_3_action),
            3
        )
        val n4 = NotifyData(
            1004,
            R.mipmap.ic_nt_s_ym,
            R.mipmap.ic_nt_b_ym,
            App.getAppContext().getString(R.string.nt_4_title),
            StringUtils.buildTargetNotifyContent(
                App.getAppContext().getString(R.string.nt_4_content),
                App.getAppContext().getString(R.string.nt_4_sub_content)
            ),
            App.getAppContext().getString(R.string.nt_4_action),
            4
        )
        val n5 = NotifyData(
            1001,
            R.mipmap.ic_nt_s_va,
            R.mipmap.ic_nt_b_va,
            App.getAppContext().getString(R.string.nt_5_title),
            StringUtils.buildTargetNotifyContent(
                App.getAppContext().getString(R.string.nt_5_content),
                App.getAppContext().getString(R.string.nt_5_sub_content)
            ),
            App.getAppContext().getString(R.string.nt_5_action),
            5
        )
        return listOf(n1, n2, n3, n4, n5)
    }

}