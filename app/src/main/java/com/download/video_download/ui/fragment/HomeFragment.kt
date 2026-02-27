package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.databinding.FragmentHomeBinding
import com.download.video_download.ui.activity.GuideActivity
import com.download.video_download.ui.adapter.HomeSiteAdapter
import com.download.video_download.ui.viewmodel.HomeViewModel
import com.download.video_download.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

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
            val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.HOME, AdType.INTERSTITIAL) == AdLoadState.LOADED
            if (hasCache){
                lifecycleScope.launch {
                    AdMgr.INSTANCE.showAd(
                        AdPosition.HOME,
                        AdType.INTERSTITIAL,
                        requireActivity(),
                        onShowResult={ position, adType, success, error->
                            LogUtils.d("广告: ${error?.message}${error?.domain}")
                        },
                        onAdDismissed = { position, adType ->
                            homeViewModel.preloadAd( requireContext())
                            mainViewModel.navigate(NavigationItem(it.url, NavState.HOME, NavState.SEARCH))
                        })
                }
                return@HomeSiteAdapter
            }
            homeViewModel.preloadAd( requireContext())
            mainViewModel.navigate(NavigationItem(it.url, NavState.HOME, NavState.SEARCH))
        }
        binding.rvWebsite.adapter =  adapter
        viewModel.videoList.observe(viewLifecycleOwner) { videoUrls ->
            adapter.updateData(videoUrls)
        }
        homeViewModel.preloadAd(requireContext())
        homeViewModel.preloadSNAd(requireContext())
    }

    override fun initListeners() {
        binding.ivGuideHelp.setOnClickListener {
            requireActivity().startActivity<GuideActivity>()
        }
        binding.rlSearch.setOnClickListener {
            mainViewModel.navigate(NavigationItem("", NavState.HOME, NavState.SEARCH))
        }
        viewModel.isAdLoaded.observe(this, Observer { isLoaded ->
            if (!isLoaded) return@Observer
            lifecycleScope.launch {
                AdMgr.INSTANCE.showAd(AdPosition.HOME, AdType.NATIVE,requireActivity(),
                    onShowResult = { position, adType, success, error->
                        if (success){
                            AdMgr.INSTANCE.getNativeAd( position)?.let {
                                binding.homeAd.visibility = View.VISIBLE
                                binding.homeAd.setNativeAd(it,requireContext())
                            }
                        }
                    })
            }
        })
    }
    override fun onResume() {
        super.onResume()
        homeViewModel.preloadNAd(requireContext())
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.homeAd.releaseAd()
    }
}