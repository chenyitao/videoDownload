package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.databinding.FragmentHomeBinding
import com.download.video_download.ui.activity.GuideActivity
import com.download.video_download.ui.adapter.HomeSiteAdapter
import com.download.video_download.ui.viewmodel.HomeViewModel
import com.download.video_download.ui.viewmodel.MainViewModel

class HomeFragment: BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    val homeViewModel: HomeViewModel by viewModels()
    lateinit var adapter: HomeSiteAdapter
    val mainViewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): HomeViewModel = homeViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        adapter = HomeSiteAdapter{
            mainViewModel.navigate(NavigationItem(it.url, NavState.HOME, NavState.SEARCH))
        }
        binding.rvWebsite.adapter =  adapter
        viewModel.videoList.observe(viewLifecycleOwner) { videoUrls ->
            adapter.updateData(videoUrls)
        }
    }

    override fun initListeners() {
        binding.ivGuideHelp.setOnClickListener {
            requireActivity().startActivity<GuideActivity>()
        }
        binding.rlSearch.setOnClickListener {
            mainViewModel.navigate(NavigationItem("", NavState.HOME, NavState.SEARCH))
        }
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

}