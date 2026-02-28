package com.download.video_download.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Environment.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.ext.showToast
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.model.SearchState
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.utils.AppCache
import com.download.video_download.databinding.FragmentPlayerBinding
import com.download.video_download.ui.activity.GuideActivity
import com.download.video_download.ui.activity.InnerVideoActivity
import com.download.video_download.ui.adapter.PlayerAdapter
import com.download.video_download.ui.viewmodel.MainViewModel
import com.download.video_download.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale.getDefault

class PlayerFragment : BaseFragment<PlayerViewModel, FragmentPlayerBinding>() {
    val playerViewModel: PlayerViewModel by viewModels()
    val mainViewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    var adapter: PlayerAdapter? = null
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlayerBinding {
        return FragmentPlayerBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): PlayerViewModel = playerViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        TrackMgr.instance.trackEvent(TrackEventType.SESSION_START)
        adapter = PlayerAdapter(
            onItemClick = {
                if (!isFileExist(data =  it)){
                    requireContext().showToast(requireContext().getString(R.string.file_valide))
                    return@PlayerAdapter
                }
                TrackMgr.instance.trackEvent(TrackEventType.safedddd_play1)
                playVideoWithSystemPlayer(data = it)
            },
            {
                playerViewModel.removeVideo(it)
                requireContext().showToast(requireContext().getString(R.string.delete_successfully))
            },{
                shareVideo(it)
            }
        )
        binding.rvPlaye.adapter = adapter
    }

    override fun initListeners() {
        playerViewModel.videoList.observe(this){
            it?.takeIf { video-> video.isNotEmpty() }?.let { list->
                binding.rlEmpty.visibility = View.GONE
                adapter?.updateData( list)
            }?:run {
                binding.rlEmpty.visibility = View.VISIBLE
            }
        }
    }
    override fun onResume() {
        super.onResume()
        playerViewModel.initVideoData()
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_main4)
    }

    override fun onPause() {
        super.onPause()
    }
    fun shareVideo(item: Video) {
        val videoFile = File(
            requireContext().getExternalFilesDir(
                DIRECTORY_DOWNLOADS
            )?.absolutePath,
            item.fileName + getVideoFileExtension(
                item.mimeTypes,
                item.url
            )
        )
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            videoFile
        )
        if (!videoFile.exists()) {
            return
        }
        val mimeType = when (videoFile.extension.lowercase(getDefault())) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            else -> "video/*" // 兜底
        }
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        requireContext().startActivity(Intent.createChooser(intent, ""))
    }
    fun getVideoFileExtension(mimeType: String, url: String): String {
        return when (mimeType) {
            "video/mp4" -> ".mp4"
            "video/avi" -> ".avi"
            "video/mov" -> ".mov"
            "video/wmv" -> ".wmv"
            "video/flv" -> ".flv"
            "video/webm" -> ".webm"
            "audio/mp3" -> ".mp3"
            "audio/wav" -> ".wav"
            "audio/ogg" -> ".ogg"
            "application/x-mpegURL"-> ".mp4"
            else -> {
                when {
                    url.endsWith(".mp4", ignoreCase = true) -> ".mp4"
                    url.endsWith(".avi", ignoreCase = true) -> ".avi"
                    url.endsWith(".mov", ignoreCase = true) -> ".mov"
                    url.endsWith(".wmv", ignoreCase = true) -> ".wmv"
                    url.endsWith(".flv", ignoreCase = true) -> ".flv"
                    url.endsWith(".webm", ignoreCase = true) -> ".webm"
                    url.endsWith(".mp3", ignoreCase = true) -> ".mp3"
                    url.endsWith(".wav", ignoreCase = true) -> ".wav"
                    url.endsWith(".ogg", ignoreCase = true) -> ".ogg"
                    else -> ".mp4"
                }
            }
        }
    }
    private fun isFileExist(data: Video): Boolean {
        val file = File(
            requireContext().getExternalFilesDir(
                DIRECTORY_DOWNLOADS
            )?.absolutePath,
            data.fileName + getVideoFileExtension(
                data.mimeTypes,
                data.url
            )
        )
        return file.exists()
    }
    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            TrackMgr.instance.trackAdEvent(AdPosition.BACK, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)

            val cache = AdMgr.INSTANCE.getAdLoadState(AdPosition.BACK, AdType.INTERSTITIAL) == AdLoadState.LOADED

            if (cache) {
                lifecycleScope.launch {
                    AdMgr.INSTANCE.showAd(AdPosition.BACK, AdType.INTERSTITIAL,requireActivity(),
                        onShowResult = { position, adType, success, error->

                        }, onAdDismissed =  {position, adType->
                            viewModel.preloadBkAd(requireActivity())
                        })
                }
                return@registerForActivityResult
            }
            viewModel.preloadBkAd(requireActivity())
        }
    }
    fun playVideoWithSystemPlayer(data: Video) {
        val file = File(
            requireContext().getExternalFilesDir(
                DIRECTORY_DOWNLOADS
            )?.absolutePath,
            data.fileName + getVideoFileExtension(
                data.mimeTypes,
                data.url
            )
        )
        if (!file.exists()) {
            return
        }
        val path = requireContext().getExternalFilesDir(
            DIRECTORY_DOWNLOADS
        )?.absolutePath
        val fileName = data.fileName + getVideoFileExtension(
            data.mimeTypes,
            data.url
        )
        val intent = Intent(requireContext(), InnerVideoActivity::class.java)
        intent.putExtra("path", path)
        intent.putExtra("fileName",fileName)
        activityLauncher.launch(intent)
//        val file = File(
//            requireContext().getExternalFilesDir(
//                DIRECTORY_DOWNLOADS
//            )?.absolutePath,
//            data.fileName + getVideoFileExtension(
//                data.mimeTypes,
//                data.url
//            )
//        )
//        val uri = FileProvider.getUriForFile(
//            App.getAppContext(),
//            "${App.getAppContext().packageName}.provider",
//            file
//        )
//        if (!file.exists()) {
//            return
//        }
//        val mimeType = when (file.extension.lowercase(getDefault())) {
//            "mp4" -> "video/mp4"
//            "mkv" -> "video/x-matroska"
//            "avi" -> "video/x-msvideo"
//            else -> "video/*" // 兜底
//        }
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, mimeType)
//            putExtra(Intent.EXTRA_TITLE, data.fileName)
//            putExtra("filename", data.fileName)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//
//        requireContext().startActivity(Intent.createChooser(intent, ""))
    }
}