package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.download.video_download.base.BaseFragment
import com.download.video_download.databinding.FragmentPlayerBinding
import com.download.video_download.ui.viewmodel.PlayerViewModel

class PlayerFragment : BaseFragment<PlayerViewModel, FragmentPlayerBinding>() {
    val playerViewModel: PlayerViewModel by viewModels()
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlayerBinding {
        return FragmentPlayerBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): PlayerViewModel = playerViewModel

    override fun initViews(savedInstanceState: Bundle?) {
    }

    override fun initListeners() {
    }
}