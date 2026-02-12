package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.model.SearchState
import com.download.video_download.databinding.FragmentSearchHistoryBinding
import com.download.video_download.ui.adapter.HistoryAdapter
import com.download.video_download.ui.dialog.HistoryClearAllDialog
import com.download.video_download.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

class WebHistoryFragment: BaseFragment<SearchViewModel, FragmentSearchHistoryBinding>() {
    val searchViewModel: SearchViewModel by viewModels(
        ownerProducer = { requireParentFragment() } // 作用域指向父 Fragment
    )
    lateinit var adapter: HistoryAdapter
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchHistoryBinding {
        return FragmentSearchHistoryBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        adapter = HistoryAdapter(onItemDelClick = {
            viewModel.deleteHistory(it)
        }, onItemClick ={
            searchViewModel.navigate(SearchState.WEB)
            searchViewModel.setChromeUrl(it.url)
            searchViewModel.setSearchInput(it.url)
        })
        binding.rvHistory.adapter = adapter
        viewModel.getHistoryData()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyList.collect { newList ->
                    adapter.updateData(newList)
                    if (newList.isEmpty()){
                        viewModel.navigate(SearchState.GUIDE)
                    }
                }
            }
        }
    }

    override fun initListeners() {
        binding.tvClearAll.setOnClickListener {
            showClearAllDialog()
        }
    }
    
    private fun showClearAllDialog() {
        val dialog = HistoryClearAllDialog()
        dialog.setOnConfirmListener {
          viewModel.clearHistory()
            viewModel.navigate(SearchState.GUIDE)
        }
        dialog.show(parentFragmentManager, "HistoryClearAllDialog")
    }
}