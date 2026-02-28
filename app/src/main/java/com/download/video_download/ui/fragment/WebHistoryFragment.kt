package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.model.SearchState
import com.download.video_download.databinding.FragmentSearchHistoryBinding
import com.download.video_download.ui.adapter.HistoryAdapter
import com.download.video_download.ui.dialog.HistoryClearAllDialog
import com.download.video_download.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        TrackMgr.instance.trackEvent(TrackEventType.SESSION_START)
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
        viewModel.isAdLoaded.observe(this, Observer { isLoaded ->
            if (!isLoaded) return@Observer
            lifecycleScope.launch {
                AdMgr.INSTANCE.showAd(AdPosition.SEARCH, AdType.NATIVE,requireActivity(),
                    onShowResult = { position, adType, success, error->
                        if (success){
                            AdMgr.INSTANCE.getNativeAd( position)?.let {
                                binding.sId.visibility = View.VISIBLE
                                binding.sId.setNativeAd(it,requireContext())
                            }
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main){
                                    delay(200)
                                    viewModel.preloadNAd(requireContext())
                                }
                            }
                        }else{
                            if (error?.code == -2){
                                binding.sId.visibility = View.GONE
                                binding.sId.releaseAd()
                            }
                        }
                    })
            }
        })
    }
    
    private fun showClearAllDialog() {
        val dialog = HistoryClearAllDialog()
        dialog.setOnConfirmListener {
          viewModel.clearHistory()
            viewModel.navigate(SearchState.GUIDE)
        }
        dialog.show(parentFragmentManager, "HistoryClearAllDialog")
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkNAd(requireContext())
        viewModel.preloadBkAd(requireContext())
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser1)
        TrackMgr.instance.trackAdEvent(AdPosition.SEARCH, AdType.NATIVE, TrackEventType.safedddd_bg)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.sId.releaseAd()
    }
}