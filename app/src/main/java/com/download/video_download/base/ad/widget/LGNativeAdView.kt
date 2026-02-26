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
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class LGNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var nativeAdView: NativeAdView
    private var currentNativeAd: NativeAd? = null

    init {
        initAdView()
    }
    private fun initAdView() {
        val inflater = LayoutInflater.from(context)
        nativeAdView = inflater.inflate(R.layout.lg_native_ad_layout, null) as NativeAdView
        with(nativeAdView) {
            headlineView = findViewById(R.id.ad_headline)
            bodyView = findViewById(R.id.ad_body)
            callToActionView = findViewById(R.id.ad_call_to_action)
            iconView = findViewById(R.id.ad_app_icon)
            mediaView = findViewById(R.id.ad_media_view)
        }
        removeAllViews()
        addView(nativeAdView)
    }

    fun setNativeAd(nativeAd: NativeAd) {
        currentNativeAd?.destroy()
        currentNativeAd = nativeAd

        bindNativeAdToView(nativeAd, nativeAdView)
    }

    private fun bindNativeAdToView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline

        (nativeAdView.bodyView as? TextView)?.text = nativeAd.body

        (nativeAdView.callToActionView as? AppCompatButton)?.text = nativeAd.callToAction

        nativeAd.icon?.let { icon ->
            (nativeAdView.iconView as? AppCompatImageView)?.setImageDrawable(icon.drawable)
            nativeAdView.iconView?.visibility = VISIBLE
        } ?: run {
            nativeAdView.iconView?.visibility = GONE
        }

        nativeAd.mediaContent?.let { mediaContent ->
            nativeAdView.mediaView?.mediaContent = mediaContent
            nativeAdView.mediaView?.visibility = VISIBLE
        } ?: run {
            nativeAdView.mediaView?.visibility = GONE
        }
        nativeAdView.setNativeAd(nativeAd)
    }
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
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