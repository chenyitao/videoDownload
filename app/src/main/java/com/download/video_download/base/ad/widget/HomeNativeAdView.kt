package com.download.video_download.base.ad.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import com.download.video_download.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class HomeNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var nativeAdView: NativeAdView
    private var currentNativeAd: NativeAd? = null

    fun setNativeAd(nativeAd: NativeAd,context: Context) {
        currentNativeAd?.destroy()
        currentNativeAd = nativeAd

        bindNativeAdToView(nativeAd,context)
    }

    private fun bindNativeAdToView(nativeAd: NativeAd,context: Context) {
        val inflater = LayoutInflater.from(context)
        val nativeAdView = inflater.inflate(R.layout.home_native_ad_layout, null) as NativeAdView
        val headlineView = nativeAdView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = nativeAdView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = nativeAdView.findViewById<AppCompatButton>(R.id.ad_call_to_action)
        val iconView = nativeAdView.findViewById<AppCompatImageView>(R.id.ad_app_icon)
        val mediaView = nativeAdView.findViewById<MediaView>(R.id.ad_media_view)

        headlineView.text = nativeAd.headline
        nativeAdView.headlineView = headlineView
        bodyView.text = nativeAd.body
        nativeAdView.bodyView = bodyView
        callToActionView.text = nativeAd.callToAction
        nativeAdView.callToActionView = callToActionView
        nativeAd.icon?.let { icon ->
            iconView.setImageDrawable(icon.drawable)
            nativeAdView.iconView = iconView
            nativeAdView.iconView?.visibility = VISIBLE
        } ?: run {
            nativeAdView.iconView?.visibility = GONE
        }

        nativeAd.mediaContent?.let { mediaContent ->
            mediaView.mediaContent = mediaContent
            nativeAdView.mediaView = mediaView
            nativeAdView.mediaView?.visibility = VISIBLE
        } ?: run {
            nativeAdView.mediaView?.visibility = GONE
        }
        nativeAdView.setNativeAd(nativeAd)
        removeAllViews()
        addView(nativeAdView)
    }
    fun releaseAd() {
        currentNativeAd?.destroy()
        currentNativeAd = null
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseAd()
    }
}