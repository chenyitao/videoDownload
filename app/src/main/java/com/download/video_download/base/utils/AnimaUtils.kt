package com.download.video_download.base.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import com.download.video_download.R

object AnimaUtils {
    private var rippleAnimation: Animation? = null
    private var isRippleAnimRunning = false
    private var isRotateAnimRunning = false
    fun startRippleAnimation(context: Context, v: View) {
        if (isRippleAnimRunning) return

        val floatingAnimView = v
        floatingAnimView.visibility = View.VISIBLE

        rippleAnimation = AnimationUtils.loadAnimation(context, R.anim.ripple_circle_anim)
        rippleAnimation?.let { anim ->
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    isRippleAnimRunning = true
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (isRippleAnimRunning) {
                        floatingAnimView.startAnimation(anim)
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            floatingAnimView.startAnimation(anim)
        }
    }

    fun stopRippleAnimation(context: Context, v: View) {
        isRippleAnimRunning = false

        val floatingAnimView = v

        rippleAnimation?.cancel()
        rippleAnimation = null
        floatingAnimView.visibility = View.GONE
    }
     fun initRotateAnimation( v: View) {
        val ivFloating = v
        (ivFloating.tag as? ValueAnimator)?.cancel()
        ivFloating.rotation = 0f
        val rotateAnimator = ObjectAnimator.ofFloat(ivFloating, "rotation", 0f, 360f)
        rotateAnimator.apply {
            duration = 200
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isRotateAnimRunning = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isRotateAnimRunning = false
                    ivFloating.rotation = 0f
                }

                override fun onAnimationCancel(animation: Animator) {
                    isRotateAnimRunning = false
                    ivFloating.rotation = 0f
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        ivFloating.tag = rotateAnimator
    }

     fun startRotateAnimation(v: View) {
        val animator =v.tag as? ValueAnimator
        animator?.takeIf { !isRotateAnimRunning }?.start()
    }
     fun stopRotateAnimation(v: View) {
        val animator = v.tag as? ValueAnimator
        animator?.cancel()
        v.rotation = 0f
        isRotateAnimRunning = false
    }
}