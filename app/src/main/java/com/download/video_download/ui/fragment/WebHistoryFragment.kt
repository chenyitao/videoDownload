package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.download.video_download.base.BaseFragment
import com.download.video_download.databinding.FragmentSearchHistoryBinding
import com.download.video_download.ui.viewmodel.SearchViewModel

class WebHistoryFragment: BaseFragment<SearchViewModel, FragmentSearchHistoryBinding>() {
    val searchViewModel: SearchViewModel by viewModels()
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchHistoryBinding {
        return FragmentSearchHistoryBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
    }

    override fun initListeners() {
    }
}