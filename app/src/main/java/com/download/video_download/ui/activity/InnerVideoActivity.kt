package com.download.video_download.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.download.video_download.base.BaseActivity
import com.download.video_download.base.wiget.InnerPlayer
import com.download.video_download.databinding.ActivityInnerPlayerBinding
import com.download.video_download.ui.viewmodel.InnerVideoViewModel
import java.io.File
import java.util.Locale
import kotlin.getValue

class InnerVideoActivity : BaseActivity<InnerVideoViewModel, ActivityInnerPlayerBinding>()  {
    val viewModel: InnerVideoViewModel by viewModels()
    lateinit var player: InnerPlayer
    override fun createViewBinding(): ActivityInnerPlayerBinding {
        return ActivityInnerPlayerBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): InnerVideoViewModel =viewModel
    override fun initViews(savedInstanceState: Bundle?) {
        val path = intent.getStringExtra("path")
        val fileName = intent.getStringExtra("fileName")
        player = InnerPlayer(this, playClick = {

        }, onFinish = {
            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()
        }, share = {
            shareVideo("$path/$fileName")
        })
        player.setVideoUri("$path/$fileName")
        mBind.flVideoContainer.addView(player)
    }

    override fun initListeners() {
    }
    override fun onResume() {
        super.onResume()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun handleBackPressed(): Boolean {
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        return super.handleBackPressed()
    }

    private fun shareVideo(path: String) {
        val file = File(
            path
        )
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.provider",
            file
        )

        val mimeType = when (file.extension.lowercase(Locale.getDefault())) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            else -> "video/*"
        }

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, ""))
    }
}