package com.download.video_download.base.utils

import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.download.video_download.App
import com.download.video_download.R

object PopMenu {
    fun showCustomPopupMenu(anchor: View,onShare: () -> Unit,
                            onDel: () -> Unit) {
        val popupView = LayoutInflater.from(App.getAppContext()).inflate(R.layout.popup_menu_custom, null)

       val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow?.apply {
            setBackgroundDrawable(App.getAppContext().resources.getDrawable(android.R.color.transparent))
            isOutsideTouchable = true
            isFocusable = true
        }

        popupView.findViewById<LinearLayout>(R.id.ll_del).setOnClickListener {
            onDel.invoke()
            popupWindow.dismiss()
        }
        popupView.findViewById<LinearLayout>(R.id.ll_share).setOnClickListener {
            onShare.invoke()
            popupWindow.dismiss()
        }

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorRect = Rect(
            location[0],
            location[1],
            location[0] + anchor.width,
            location[1] + anchor.height
        )

        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        val screenHeight = App.getAppContext().resources.displayMetrics.heightPixels
        val yOffset = if (anchorRect.bottom + popupHeight > screenHeight) {
            -popupHeight - anchor.height
        } else {
            0
        }

        popupWindow?.showAtLocation(
            anchor,
            Gravity.NO_GRAVITY,
            anchorRect.left,
            anchorRect.bottom + yOffset
        )
    }
}