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
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.ext.startActivity
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.databinding.FragmentHomeBinding
import com.download.video_download.ui.activity.GuideActivity
import com.download.video_download.ui.adapter.HomeSiteAdapter
import com.download.video_download.ui.viewmodel.HomeViewModel
import com.download.video_download.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        TrackMgr.instance.trackEvent(TrackEventType.SESSION_START)
        adapter = HomeSiteAdapter{
            TrackMgr.instance.trackAdEvent(AdPosition.HOME, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_home2, mapOf("safedddd1" to it.url))
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
        homeViewModel.preloadBkAd( requireContext())
        homeViewModel.preloadTabAd(requireContext())
    }

    override fun initListeners() {
        binding.ivGuideHelp.setOnClickListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_home3)
            requireActivity().startActivity<GuideActivity>()
        }
        binding.rlSearch.setOnClickListener {
            mainViewModel.navigate(NavigationItem("", NavState.HOME, NavState.SEARCH))
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_home1)
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
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main){
                                    delay(200)
                                    AdMgr.INSTANCE.preloadAd(AdPosition.HOME, AdType.NATIVE, requireContext(),
                                        onLoadStateChanged = { position, adType, loadState,error ->
                                            LogUtils.d("广告:  ${error?.message}${error?.domain}")
                                        })
                                }
                            }
                        }
                    })
            }
        })
    }
    override fun onResume() {
        super.onResume()
        homeViewModel.preloadNAd(requireContext())
        TrackMgr.instance.trackAdEvent(AdPosition.HOME, AdType.NATIVE, TrackEventType.safedddd_bg)

        TrackMgr.instance.trackEvent(TrackEventType.safedddd_main1)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.homeAd.releaseAd()
    }
}