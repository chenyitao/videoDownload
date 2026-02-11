package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.download.video_download.base.BaseFragment
import com.download.video_download.databinding.FragmentSearchChromeBinding
import com.download.video_download.ui.viewmodel.SearchViewModel

class WebChromeFragment: BaseFragment<SearchViewModel, FragmentSearchChromeBinding>() {
    val searchViewModel: SearchViewModel by viewModels()
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchChromeBinding {
        return FragmentSearchChromeBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel  = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
    }

    override fun initListeners() {
    }
}