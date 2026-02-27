package com.download.video_download.base.wiget
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.download.video_download.R
import com.download.video_download.base.utils.ActivityManager
import com.download.video_download.databinding.LayoutInnerVideoPlayerBinding
import java.util.Locale

class InnerPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val onFinish: () -> Unit = {},
    private val playClick: () -> Unit = {},
    private val share: () -> Unit = {}
) : LinearLayout(context, attrs, defStyleAttr) {

    // 常量定义
    private val HIDE_DELAY = 3000L
    private val SEEK_STEP = 5000L
    private val PROGRESS_UPDATE_INTERVAL = 1000L

    // ViewBinding
    private val binding: LayoutInnerVideoPlayerBinding =
        LayoutInnerVideoPlayerBinding.inflate(LayoutInflater.from(context), this, true)

    // ExoPlayer
    private lateinit var player: ExoPlayer

    // 状态变量
    private var isPlaying = false
    private var currentPosition = 0L
    private var duration = 0L
    private var isPlaybackEnded = false
    private var isMuted = false
    private var isControllerVisible = true
    private var isFullScreen = false
    private var isSeeking = false
    private var wasPlayingBeforeSeek = false

    // 定时器
    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }
    private val hideControllerRunnable = Runnable {
        hideController()
    }

    init {
        initPlayer()
        initViewListeners()
        initSeekBarListeners()
        resetHideControllerTimer()
    }

    /**
     * 初始化播放器
     */
    private fun initPlayer() {
        player = ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(1000)
            .setSeekForwardIncrementMs(1000)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                setHandleAudioBecomingNoisy(true)
                playWhenReady = false

                // 播放器状态监听
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying_: Boolean) {
                        if (!isSeeking) {
                           this@InnerPlayer.isPlaying = isPlaying_
                            updatePlayPauseIcon()
                            if (isPlaying) {
                                startProgressUpdates()
                            } else {
                                stopProgressUpdates()
                                updateProgress()
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_ENDED -> {
                                isPlaybackEnded = true
                                this@InnerPlayer.isPlaying = false
                                player.seekTo(0)
                                player.playWhenReady = false
                                player.stop()
                                player.prepare()
                                this@InnerPlayer.currentPosition = 0
                                updateProgress()
                                updatePlayPauseIcon()
                                stopProgressUpdates()
                            }
                            Player.STATE_READY -> {
                                this@InnerPlayer.duration = player.duration
                                updateTimeText()
                                updateProgress()
                            }
                        }
                    }
                })
            }

        binding.playerView.player = player
        binding.playerView.useController = false
    }

    /**
     * 初始化View事件监听
     */
    private fun initViewListeners() {
        // 中间区域点击 - 切换控制器显示/隐藏
        binding.clickArea.setOnClickListener {
            toggleControllerVisibility()
        }

        // 返回按钮
        binding.ivBack.setOnClickListener {
            resetHideControllerTimer()
            if (isFullScreen) {
                exitFullScreen()
            } else {
                onFinish.invoke()
            }
        }

        // 分享按钮
        binding.ivShare.setOnClickListener {
            share.invoke()
            resetHideControllerTimer()
        }

        // 竖屏播放/暂停
        binding.ivPlayPausePortrait.setOnClickListener {
            togglePlayPause()
            resetHideControllerTimer()
        }
        // 竖屏静音
        binding.ivMutePortrait.setOnClickListener {
            toggleMute()
            resetHideControllerTimer()
        }

        // 竖屏快退
        binding.ivSeekBackPortrait.setOnClickListener {
            seekBackward()
            resetHideControllerTimer()
        }
        // 竖屏快进
        binding.ivSeekForwardPortrait.setOnClickListener {
            seekForward()
            resetHideControllerTimer()
        }

        // 竖屏全屏
        binding.ivFullScreenPortrait.setOnClickListener {
            if (isFullScreen) {
                exitFullScreen()
            } else {
                enterFullScreen()
            }
            resetHideControllerTimer()
        }
    }

    /**
     * 初始化进度条监听
     */
    private fun initSeekBarListeners() {
        // 竖屏进度条
        binding.progressBarPortrait.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && duration > 0) {
                    val newPosition = (progress * duration / 100).coerceAtMost(duration)
                    currentPosition = newPosition
                    updateTimeText()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeeking = true
                wasPlayingBeforeSeek = isPlaying
                stopProgressUpdates()
                if (wasPlayingBeforeSeek) {
                    player.pause()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (duration > 0) {
                    val progress = seekBar?.progress ?: 0
                    val newPosition = (progress * duration / 100).coerceAtMost(duration)
                    player.seekTo(newPosition)
                    currentPosition = newPosition

                    if (wasPlayingBeforeSeek) {
                        player.play()
                    }
                }
                isSeeking = false
                if (player.isPlaying) {
                    startProgressUpdates()
                }
                resetHideControllerTimer()
            }
        })
    }
    fun getFullScreen(): Boolean{
        return isFullScreen
    }
    // ===================== 核心功能方法 =====================

    /**
     * 设置视频播放地址
     */
    fun setVideoUri(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    /**
     * 切换播放/暂停
     */
    private fun togglePlayPause() {
        if (isPlaybackEnded) {
            player.prepare()
            player.play()
            isPlaybackEnded = false
            playClick.invoke()
        } else {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
                playClick.invoke()
            }
        }
    }

    /**
     * 切换静音状态
     */
    private fun toggleMute() {
        isMuted = !isMuted
        player.volume = if (isMuted) 0f else 1f
        updateMuteIcon()
    }

    /**
     * 快进5秒
     */
    private fun seekForward() {
        val newPosition = (currentPosition + SEEK_STEP).coerceAtMost(duration)
        player.seekTo(newPosition)
        currentPosition = newPosition
        updateProgress()
    }

    /**
     * 快退5秒
     */
    private fun seekBackward() {
        val newPosition = (currentPosition - SEEK_STEP).coerceAtLeast(0L)
        player.seekTo(newPosition)
        currentPosition = newPosition
        updateProgress()
    }

    /**
     * 进入全屏
     */
    private fun enterFullScreen() {
        isFullScreen = true
        val activity = ActivityManager.currentActivity()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity?.window?.let { hideSystemUI(it) }
    }
    /**
     * 退出全屏
     */
     fun exitFullScreen() {
        isFullScreen = false
        val activity = ActivityManager.currentActivity()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        activity?.window?.let { showSystemUI(it) }
    }

    // ===================== 控制器显示/隐藏 =====================

    /**
     * 切换控制器显示/隐藏
     */
    private fun toggleControllerVisibility() {
        handler.removeCallbacks(hideControllerRunnable)
        if (isControllerVisible) {
            hideController()
        } else {
            showController()
        }
    }

    /**
     * 显示控制器
     */
    private fun showController() {
        isControllerVisible = true
        binding.controllerContainer.alpha = 1f
        resetHideControllerTimer()
    }

    /**
     * 隐藏控制器
     */
    private fun hideController() {
        isControllerVisible = false
        binding.controllerContainer.alpha = 0f
    }
    private fun resetHideControllerTimer() {
        handler.removeCallbacks(hideControllerRunnable)
        handler.postDelayed(hideControllerRunnable, HIDE_DELAY)
    }
    private fun updatePlayPauseIcon() {
        if (isPlaying) {
            binding.ivPlayPausePortrait.setImageResource(R.mipmap.ic_video_pause)
        } else {
            binding.ivPlayPausePortrait.setImageResource(R.mipmap.ic_video_play)
        }
    }

    private fun updateMuteIcon() {
        if (isMuted) {
            binding.ivMutePortrait.setImageResource(R.mipmap.ic_video_volume_open)
        } else {
            binding.ivMutePortrait.setImageResource(R.mipmap.ic_video_volume_close)
        }
    }

    private fun updateProgress() {
        if (isSeeking) return

        currentPosition = player.currentPosition
        duration = player.duration

        if (duration > 0) {
            val progress = (currentPosition * 100 / duration).toInt()
            binding.progressBarPortrait.progress = progress
        }

        updateTimeText()
    }

    private fun updateTimeText() {
        val currentTime = formatTime(currentPosition)
        val totalTime = formatTime(duration)

        binding.tvTimePortrait.text = "$currentTime / $totalTime"
    }

    private fun formatTime(milliseconds: Long): String {
        if (milliseconds <= 0) return "00:00"

        val seconds = (milliseconds / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    private fun startProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
    }

    private fun hideSystemUI(window: Window) {
        val decorView = window.decorView
        val controller = WindowCompat.getInsetsController(window, decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }

    private fun showSystemUI(window: Window) {
        val decorView = window.decorView
        val controller = WindowCompat.getInsetsController(window, decorView)
        controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        WindowCompat.setDecorFitsSystemWindows(window, true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }

    fun setVideoPath(path: String) {
        setVideoUri(path)
    }

    fun play() {
        player.play()
        playClick.invoke()
    }

    fun pause() {
        player.pause()
    }

    fun release() {
        stopProgressUpdates()
        handler.removeCallbacks(hideControllerRunnable)
        player.release()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    private fun Int.dpToPx(context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (this * density).toInt()
    }
}