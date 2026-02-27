package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.model.DetectState
import com.download.video_download.base.model.DetectStatus
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.wiget.Player
import com.download.video_download.databinding.FragmentSearchGuideBinding
import com.download.video_download.ui.viewmodel.SearchViewModel

class WebGuideFragment: BaseFragment<SearchViewModel, FragmentSearchGuideBinding>() {
    val searchViewModel: SearchViewModel by viewModels(ownerProducer = { requireParentFragment() })
    lateinit var player: Player
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchGuideBinding {
        return FragmentSearchGuideBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        TrackMgr.instance.trackEvent(TrackEventType.SESSION_START)
        player = Player(requireContext(), playClick = {
            searchViewModel.showGuide()
            addVideo()
        })
        player.setVideoUri("android.resource://${context?.packageName}/" + R.raw.sample)
        binding.flVideoContainer.addView(player)
        val guide = "${App.getAppContext().getString(R.string.search_guide_1)}\n${App.getAppContext().getString(R.string.search_guide_2)}\n${App.getAppContext().getString(R.string.search_guide_3)}"
        binding.tvGuide1.text = guide
    }

    override fun initListeners() {
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
    private fun addVideo(){
        val vs = searchViewModel.videos.value
        vs?.let {
            val fs = it.filter { video ->
                video.url.contains("android.resource://${context?.packageName}/")
            }
            if (fs.isNotEmpty()){
                return
            }
            val video = Video(
                url = "android.resource://${context?.packageName}/" + R.raw.sample,
                fileName = "sample",
                mimeTypes = "video/mp4",
                duration = 16000,
                totalSize  = 882758,
                isSelect =  true
            )
            searchViewModel.detect(DetectStatus(DetectState.SUPPORTWEB))
            searchViewModel.isLoading(false)
            searchViewModel.saveVideos(mutableListOf(video))
        }?:run {
            val video = Video(
                url = "android.resource://${context?.packageName}/" + R.raw.sample,
                fileName = "sample",
                mimeTypes = "video/mp4",
                duration = 16000,
                totalSize  = 882758,
                isSelect =  true
            )
            searchViewModel.detect(DetectStatus(DetectState.SUPPORTWEB))
            searchViewModel.isLoading(false)
            searchViewModel.saveVideos(mutableListOf(video))
        }
    }
}