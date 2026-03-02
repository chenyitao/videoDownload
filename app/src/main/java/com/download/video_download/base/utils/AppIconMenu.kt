package com.download.video_download.base.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.download.video_download.R
import com.download.video_download.ui.activity.MainActivity

object AppIconMenu {
    fun addAppIconMenuItem(
        context: Context,
    ) {
        val exisUs = ShortcutManagerCompat.getDynamicShortcuts(context)
        val hasAdd = exisUs.any { it.id == APP_ICON_MENU_ID }
        if (hasAdd) {
            return
        }
        val unInstallMenu = ShortcutInfoCompat.Builder(context, APP_ICON_MENU_ID)
            .setShortLabel(context.getString(R.string.short_uninstall))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_short_uninstall))
            .setIntent(Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("param", "us")
                addCategory(Intent.CATEGORY_DEFAULT)
            })
            .build()
        ShortcutManagerCompat.addDynamicShortcuts(context, listOf(unInstallMenu))
    }
    fun removeAppIconMenuItem(
        context: Context,
    ) {
        ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(APP_ICON_MENU_ID))
    }
    private val APP_ICON_MENU_ID = "long_press_uins"
}