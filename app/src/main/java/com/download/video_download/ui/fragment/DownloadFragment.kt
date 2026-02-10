package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.download.video_download.base.BaseFragment

import com.download.video_download.databinding.FragmentDownloadBinding
import com.download.video_download.ui.viewmodel.DownloadViewModel

class DownloadFragment : BaseFragment<DownloadViewModel, FragmentDownloadBinding>() {
    val downloadViewModel: DownloadViewModel by viewModels()
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadBinding {
        return FragmentDownloadBinding.inflate(inflater,)
    }

    override fun createViewModel(): DownloadViewModel = downloadViewModel

    override fun initViews(savedInstanceState: Bundle?) {
    }

    override fun initListeners() {
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}