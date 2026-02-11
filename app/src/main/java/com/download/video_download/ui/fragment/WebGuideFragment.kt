package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.wiget.Player
import com.download.video_download.databinding.FragmentSearchGuideBinding
import com.download.video_download.ui.viewmodel.SearchViewModel

class WebGuideFragment: BaseFragment<SearchViewModel, FragmentSearchGuideBinding>() {
    val searchViewModel: SearchViewModel by viewModels()
    lateinit var player: Player
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchGuideBinding {
        return FragmentSearchGuideBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        player = Player(requireContext())
        player.setVideoUri("android.resource://${context?.packageName}/" + R.raw.sample)
        binding.flVideoContainer.addView(player)
        binding.tvGuide1.text = App.getAppContext().getString(R.string.search_guide_1)+"\n"+App.getAppContext().getString(R.string.search_guide_2)+"\n"+App.getAppContext().getString(R.string.search_guide_3)
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
}