package com.download.video_download.base.wiget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.download.video_download.databinding.LayoutPlayerBinding

class Player @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutPlayerBinding =
        LayoutPlayerBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var player: ExoPlayer
    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 1000) // 每秒更新一次
        }
    }
    
    private var isSeeking = false
    private var wasPlayingBeforeSeek = false

    init {
        setupPlayer()
        setupClickListeners()
        setupSeekBarListener()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(context).build()
        binding.playerView.player = player
        binding.playerView.useController = false // 禁用默认控制器
        
        // 设置播放器高度
        val layoutParams = binding.playerView.layoutParams
        layoutParams.height = 232.dpToPx(context)
        binding.playerView.layoutParams = layoutParams
    }

    private fun setupClickListeners() {
        // 播放按钮点击事件
        binding.playButton.setOnClickListener {
            togglePlayPause()
        }

        // 整个播放器视图点击事件
        binding.playerView.setOnClickListener {
            togglePlayPause()
        }

        // 监听播放状态变化
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying && !isSeeking) {
                    binding.playButton.visibility = View.GONE
                    startProgressUpdates()
                } else if (!isSeeking) {
                    binding.playButton.visibility = View.VISIBLE
                    stopProgressUpdates()
                    // 暂停时立即更新一次进度显示
                    updateProgress()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {
                        // 更新进度和时间显示
                        updateProgress()
                    }
                    ExoPlayer.STATE_ENDED -> {
                        // 播放结束，显示播放按钮
                        binding.playButton.visibility = View.VISIBLE
                        stopProgressUpdates()
                        // 播放结束时更新最终进度显示
                        updateProgress()
                    }
                }
            }
        })
    }

    private fun setupSeekBarListener() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && player.duration > 0) {
                    // 用户拖动时更新时间显示，但不改变播放位置
                    val newPosition = (progress * player.duration / 100).coerceAtMost(player.duration)
                    binding.currentTimeText.text = formatTime(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeeking = true
                wasPlayingBeforeSeek = player.isPlaying
                stopProgressUpdates()
                if (wasPlayingBeforeSeek) {
                    player.pause()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (player.duration > 0) {
                    val progress = seekBar?.progress ?: 0
                    val newPosition = (progress * player.duration / 100).coerceAtMost(player.duration)
                    player.seekTo(newPosition)
                    
                    if (wasPlayingBeforeSeek) {
                        player.play()
                    }
                }
                isSeeking = false
                // 重新开始进度更新
                if (player.isPlaying) {
                    startProgressUpdates()
                }
            }
        })
    }

    private fun togglePlayPause() {
        when (player.playbackState) {
            ExoPlayer.STATE_ENDED -> {
                // 播放完毕，重新开始播放
                player.seekTo(0)
                player.play()
            }
            else -> {
                // 正常播放/暂停切换
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }
    }

    private fun startProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
    }

    private fun updateProgress() {
        if (isSeeking) return // 拖动时不要更新进度条
        
        val currentPosition = player.currentPosition
        val duration = player.duration
        
        if (duration > 0) {
            val progress = (currentPosition * 100 / duration).toInt()
            binding.seekBar.progress = progress
        }
        
        binding.currentTimeText.text = formatTime(currentPosition)
        binding.totalTimeText.text = formatTime(duration)
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun setVideoUri(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun release() {
        stopProgressUpdates()
        player.release()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}

// 扩展函数：dp转px
private fun Int.dpToPx(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this * density).toInt()
}