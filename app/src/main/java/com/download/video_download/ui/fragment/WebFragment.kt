package com.download.video_download.ui.fragment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.ext.showToast
import com.download.video_download.base.model.DetectState
import com.download.video_download.base.model.History
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.model.SearchState
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.task.DownloadTaskManager
import com.download.video_download.base.utils.AnimaUtils
import com.download.video_download.base.utils.AnimaUtils.initRotateAnimation
import com.download.video_download.base.utils.AnimaUtils.startRotateAnimation
import com.download.video_download.base.utils.AnimaUtils.stopRotateAnimation
import com.download.video_download.base.utils.AppCache
import com.download.video_download.base.utils.DpUtils
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.base.utils.PUtils
import com.download.video_download.base.utils.RawResourceUtils
import com.download.video_download.databinding.FragmentSearchBinding
import com.download.video_download.ui.dialog.DownloadDialog
import com.download.video_download.ui.dialog.DownloadStatusDialog
import com.download.video_download.ui.dialog.isFragmentShowing
import com.download.video_download.ui.viewmodel.MainViewModel
import com.download.video_download.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import kotlin.getValue

class WebFragment : BaseFragment<SearchViewModel, FragmentSearchBinding>() {
    val searchViewModel: SearchViewModel by viewModels()
    val mainViewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    private var fingerAnimator: ObjectAnimator? = null
    private var curPage = SearchState.GUIDE
    private val fragmentCache = HashMap<SearchState, Fragment>()
    private var currentFragment: Fragment? = null
    private var permissionDenied = false
    private var taskCreatD:DownloadStatusDialog? = null
    private var taskCD:DownloadStatusDialog? = null
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        initRotateAnimation(binding.ivFloating)
        searchViewModel.nav.observe(this) {
            curPage = it ?: SearchState.GUIDE
            switchPage()
        }
        searchViewModel.showGuide.observe(this) {
            if (it) {
                binding.downFloatingGuide.visibility = View.VISIBLE
                createUpFingerAnimation(false)
                startAnimations()
                TrackMgr.instance.trackEvent(TrackEventType.safedddd_search2)
            }
        }
        searchViewModel.setSearchInput.observe(this) {
            binding.etSearch.setText(it)
        }
        searchViewModel.goBackDone.observe(this) {
            binding.etSearch.text?.clear()
            hideKeyboard(binding.etSearch)
            searchViewModel.saveVideos(mutableListOf())
            if (AppCache.isFirstDetect && binding.downFloatingGuide.isVisible){
                AppCache.isFirstDetect = false
                binding.downFloatingGuide.visibility = View.GONE
                cancelAnimations()
            }
            TrackMgr.instance.trackAdEvent(AdPosition.BACK, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)
            val cache = AdMgr.INSTANCE.getAdLoadState(AdPosition.BACK, AdType.INTERSTITIAL) == AdLoadState.LOADED

            if (cache) {
                lifecycleScope.launch {
                    AdMgr.INSTANCE.showAd(AdPosition.BACK, AdType.INTERSTITIAL,requireActivity(),
                        onShowResult = { position, adType, success, error->
                            if (error?.code == -2){
                                viewModel.preloadBkAd(requireActivity())
                                if (AppCache.history.isNotEmpty() && AppCache.history != "[]" && AppCache.history != "{}") {
                                    curPage = SearchState.HISTORY
                                } else {
                                    curPage = SearchState.GUIDE
                                }
                                switchPage()
                            }
                        }, onAdDismissed =  {position, adType->
                            viewModel.preloadBkAd(requireActivity())
                            if (AppCache.history.isNotEmpty() && AppCache.history != "[]" && AppCache.history != "{}") {
                                curPage = SearchState.HISTORY
                            } else {
                                curPage = SearchState.GUIDE
                            }
                            switchPage()
                        })
                }
                return@observe
            }
            viewModel.preloadBkAd(requireActivity())
            if (AppCache.history.isNotEmpty() && AppCache.history != "[]" && AppCache.history != "{}") {
                curPage = SearchState.HISTORY
            } else {
                curPage = SearchState.GUIDE
            }
            switchPage()
        }
        searchViewModel.detect.observe(this) {
            when (it.state) {
                DetectState.SUPPORTWEB -> {
                    binding.ivFloating.setImageResource(R.mipmap.ic_floating_normal)
                }

                DetectState.YOUTUBE -> {
                    binding.ivFloatingNum.visibility = View.GONE
                    AnimaUtils.stopRippleAnimation(requireContext(),binding.ivFloatingAnim)
                    binding.ivFloating.setImageResource(R.mipmap.ic_floating_grey)
                }

                else -> {}
            }
        }
        searchViewModel.isLoading.observe(this) {
            if (searchViewModel.detect.value?.state == DetectState.YOUTUBE) {
                stopRotateAnimation(binding.ivFloating)
                return@observe
            }
            if (it) {
                binding.ivFloating.setImageResource(R.mipmap.ic_floating_loading)
                startRotateAnimation(binding.ivFloating)
            } else {
                stopRotateAnimation(binding.ivFloating)
                binding.ivFloating.setImageResource(R.mipmap.ic_floating_normal)
            }
        }
        searchViewModel.videos.observe(this) {
            if (it.isNotEmpty()) {
                if (AppCache.isFirstDetect && !binding.downFloatingGuide.isVisible) {
                    binding.downFloatingGuide.visibility = View.VISIBLE
                    createUpFingerAnimation(false)
                    startAnimations()
                    TrackMgr.instance.trackEvent(TrackEventType.safedddd_search2)
                }
                AnimaUtils.startRippleAnimation(requireContext(),binding.ivFloatingAnim)
                binding.ivFloatingNum.visibility = View.VISIBLE
                binding.ivFloatingNum.text = it.size.toString()
                viewModel.preloadSdAd(requireContext())
            } else {
                binding.ivFloatingAnim.visibility = View.GONE
                binding.ivFloatingNum.visibility = View.GONE
                AnimaUtils.stopRippleAnimation(requireContext(),binding.ivFloatingAnim)
            }
        }
        binding.tvSearch.background = if (binding.etSearch.text.toString()
                .isEmpty()
        ) ContextCompat.getDrawable(requireContext(), R.drawable.shape_radius_6_grey)
        else ContextCompat.getDrawable(requireContext(), R.drawable.shape_red_botton_5)

        DownloadTaskManager.INSTANCE.isCompete.observe(this){
            if (it && isVisible){
                showTaskComp()
                DownloadTaskManager.INSTANCE.resetCompete(false)
            }
        }
    }

    override fun initListeners() {
        binding.tvSearch.setOnClickListener {
            val inputContent = binding.etSearch.text.toString().trim()
            if (inputContent.isNotEmpty()) {
                TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser2, mapOf("safedddd1" to inputContent))
                val data =
                    if (URLUtil.isNetworkUrl(inputContent) || inputContent.contains("www.") || inputContent.contains(
                            ".com"
                        )
                    ) {
                        inputContent
                    } else {
                        "https://www.google.com/search?q=${
                            URLEncoder.encode(
                                inputContent,
                                "UTF-8"
                            )
                        }"
                    }
                searchViewModel.addHistory(History(data, System.currentTimeMillis()))
                if (curPage != SearchState.WEB) {
                    binding.upFloatingGuide.visibility = View.GONE
                    loadFragment(SearchState.WEB)
                }
                searchViewModel.setChromeUrl(data)
                hideKeyboard(binding.etSearch)
                return@setOnClickListener
            }
            requireContext().showToast(getString(R.string.search_hint))
        }
        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP
            if (isSearchAction || isEnterKey) {
                val inputContent = v.text.toString().trim()

                if (inputContent.isNotEmpty()) {
                    TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser2, mapOf("safedddd1" to inputContent))
                    val data =
                        if (URLUtil.isNetworkUrl(inputContent) || inputContent.contains("www.") || inputContent.contains(
                                ".com"
                            )
                        ) {
                            inputContent
                        } else {
                            "https://www.google.com/search?q=${
                                URLEncoder.encode(
                                    inputContent,
                                    "UTF-8"
                                )
                            }"
                        }
                    searchViewModel.addHistory(History(data, System.currentTimeMillis()))
                    if (curPage != SearchState.WEB) {
                        curPage = SearchState.WEB
                        binding.upFloatingGuide.visibility = View.GONE
                       switchPage()
                    }
                    searchViewModel.setChromeUrl(data)
                    hideKeyboard(binding.etSearch)
                    return@OnEditorActionListener true
                }
                requireContext().showToast(getString(R.string.search_hint))
                return@OnEditorActionListener true
            }
            false
        })
        binding.etSearch.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (binding.etSearch.text.toString().isNotEmpty()) {
                    binding.ivSearchClear.visibility = View.VISIBLE
                } else {
                    binding.ivSearchClear.visibility = View.GONE
                }
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.ivSearchClear.visibility = View.GONE
                }, 100)
            }
        }
        binding.etSearch.addTextChangedListener {
            if (binding.etSearch.isFocused) {
                if (it.toString().isNotEmpty()) {
                    binding.ivSearchClear.visibility = View.VISIBLE
                } else {
                    binding.ivSearchClear.visibility = View.GONE
                }
            }
            binding.tvSearch.background = if (it.toString().isEmpty()) ContextCompat.getDrawable(
                requireContext(),
                R.drawable.shape_radius_6_grey
            )
            else ContextCompat.getDrawable(requireContext(), R.drawable.shape_red_botton_5)
        }
        binding.ivSearchClear.setOnClickListener {
            binding.etSearch.text?.clear()
        }
        binding.upFloatingGuide.setOnClickListener {
            AppCache.isFirstInstall = false
            binding.upFloatingGuide.visibility = View.GONE
            cancelAnimations()
        }
        binding.downFloatingGuide.setOnClickListener {
            AppCache.isFirstDetect = false
            binding.downFloatingGuide.visibility = View.GONE
            cancelAnimations()
        }
        binding.ivSearch.setOnClickListener {
            searchViewModel.refreshWeb()
        }
        binding.ivSearchBack.setOnClickListener {
            val isKeyboardOpen = isKeyboardReallyShowing(
                requireContext(),
                requireActivity().window.decorView.findViewById<View>(android.R.id.content)
            )
            if (isKeyboardOpen) {
                hideKeyboard(binding.etSearch)
                return@setOnClickListener
            }
            if (curPage == SearchState.WEB) {
                searchViewModel.canWebGoBack()
            } else {
                TrackMgr.instance.trackAdEvent(AdPosition.BACK, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)

                val cache = AdMgr.INSTANCE.getAdLoadState(AdPosition.BACK, AdType.INTERSTITIAL) == AdLoadState.LOADED

                if (cache) {
                    lifecycleScope.launch {
                        AdMgr.INSTANCE.showAd(AdPosition.BACK, AdType.INTERSTITIAL,requireActivity(),
                            onShowResult = { position, adType, success, error->
                                if (error?.code == -2){
                                    viewModel.preloadBkAd(requireActivity())
                                    binding.etSearch.text?.clear()
                                    mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.HOME))
                                }
                            }, onAdDismissed =  {position, adType->
                                viewModel.preloadBkAd(requireActivity())
                                binding.etSearch.text?.clear()
                                mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.HOME))
                            })
                    }
                    return@setOnClickListener
                }
                viewModel.preloadBkAd(requireActivity())
                binding.etSearch.text?.clear()
                mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.HOME))
            }
        }
        binding.ivFloating.setOnClickListener {
            if(AppCache.isFirstDetect){
                AppCache.isFirstDetect = false
                binding.downFloatingGuide.visibility = View.GONE
                cancelAnimations()
            }
            if (searchViewModel.detect.value?.state == DetectState.YOUTUBE) {
                requireContext().showToast(getString(R.string.no_file))
                return@setOnClickListener
            }
            if ((searchViewModel.detect.value?.state == DetectState.SUPPORTWEB && (searchViewModel.videos.value == null || searchViewModel.videos.value?.isEmpty() == true))
                || (searchViewModel.isLoading.value == null || searchViewModel.isLoading.value == true)
            ) {
                requireContext().showToast(getString(R.string.a_video))
                return@setOnClickListener
            }
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser8, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
            showDownloadDialog()
        }
        viewModel.adjustGuideView.observe(this){
            if (binding.upFloatingGuide.isVisible && it){
                val bgLayoutParams   = binding.upFloatingGuideBg.layoutParams as ConstraintLayout.LayoutParams
                bgLayoutParams.topMargin = DpUtils.dp2px(requireContext(),356f)
                binding.upFloatingGuideBg.layoutParams = bgLayoutParams
                val llupParams   = binding.llFingerUp.layoutParams as ConstraintLayout.LayoutParams
                llupParams.topMargin = DpUtils.dp2px(requireContext(),278f)
                binding.llFingerUp.layoutParams = llupParams
            }
        }
    }

    override fun handleBackPressed(): Boolean {
        if (!isVisible) {
            return false
        }
        val isKeyboardOpen = isKeyboardReallyShowing(
            requireContext(),
            requireActivity().window.decorView.findViewById<View>(android.R.id.content)
        )
        if (isKeyboardOpen) {
            hideKeyboard(binding.etSearch)
        } else {
            if (curPage == SearchState.WEB) {
                searchViewModel.canWebGoBack()
            } else {
                TrackMgr.instance.trackAdEvent(AdPosition.BACK, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)

                val cache = AdMgr.INSTANCE.getAdLoadState(AdPosition.BACK, AdType.INTERSTITIAL) == AdLoadState.LOADED

                if (cache) {
                    lifecycleScope.launch {
                        AdMgr.INSTANCE.showAd(AdPosition.BACK, AdType.INTERSTITIAL,requireActivity(),
                            onShowResult = { position, adType, success, error->

                            }, onAdDismissed =  {position, adType->
                                viewModel.preloadBkAd(requireActivity())
                                binding.etSearch.text?.clear()
                                mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.HOME))
                            })
                    }
                    return true
                }
                viewModel.preloadBkAd(requireActivity())
                binding.etSearch.text?.clear()
                mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.HOME))
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        parseParams()
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_main2)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(binding.etSearch)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun parseParams() {
        mainViewModel.nav.value?.let {
            when (mainViewModel.nav.value?.from) {
                NavState.HOME -> {
                    if (mainViewModel.nav.value?.params?.isNotEmpty() == true) {
                        searchViewModel.setChromeUrl(mainViewModel.nav.value?.params.toString())
                        loadFragment(SearchState.WEB)
                        curPage = SearchState.WEB
                    } else if (AppCache.history.isNotEmpty() && AppCache.history != "[]" && AppCache.history != "{}") {
                        loadFragment(SearchState.HISTORY)
                        curPage = SearchState.HISTORY
                        autoShowKeyboard(binding.etSearch)
                    } else {
                        loadFragment(SearchState.GUIDE)
                        curPage = SearchState.GUIDE
                        autoShowKeyboard(binding.etSearch)
                    }
                    mainViewModel.clearNavigation()
                }

                else -> {
                    switchPage()
                }
            }
        } ?: run {
            switchPage()
        }
        if (curPage == SearchState.GUIDE && AppCache.isFirstInstall) {
            binding.upFloatingGuide.visibility = View.VISIBLE
            createUpFingerAnimation()
            startAnimations()
        } else {
            binding.upFloatingGuide.visibility = View.GONE
        }
        if (curPage == SearchState.HISTORY) {
            binding.floating.visibility = View.GONE
        } else {
            binding.floating.visibility = View.VISIBLE
        }
        if (curPage == SearchState.WEB) {
            binding.tvSearch.visibility = View.GONE
            binding.ivSearch.visibility = View.VISIBLE
        } else {
            binding.tvSearch.visibility = View.VISIBLE
            binding.ivSearch.visibility = View.GONE
        }
    }

    private fun switchPage(params: String = "") {
        if (curPage == SearchState.WEB) {
            searchViewModel.setChromeUrl("")
            loadFragment(SearchState.WEB)
            curPage = SearchState.WEB
        } else if (AppCache.history.isNotEmpty() && AppCache.history != "[]" && AppCache.history != "{}") {
            loadFragment(SearchState.HISTORY)
            curPage = SearchState.HISTORY
        } else {
            loadFragment(SearchState.GUIDE)
            curPage = SearchState.GUIDE
        }
        if (curPage == SearchState.HISTORY) {
            binding.floating.visibility = View.GONE
        } else {
            binding.floating.visibility = View.VISIBLE
        }
        if (curPage == SearchState.WEB) {
            binding.tvSearch.visibility = View.GONE
            binding.ivSearch.visibility = View.VISIBLE
        } else {
            binding.tvSearch.visibility = View.VISIBLE
            binding.ivSearch.visibility = View.GONE
        }
        if (curPage == SearchState.GUIDE && AppCache.isFirstInstall) {
            binding.upFloatingGuide.visibility = View.VISIBLE
            createUpFingerAnimation()
            startAnimations()
        }
    }

    private fun loadFragment(state: SearchState) {
        val targetFragment = fragmentCache.getOrPut(state) {
            when (state) {
                SearchState.GUIDE -> WebGuideFragment()
                SearchState.HISTORY -> WebHistoryFragment()
                SearchState.WEB -> WebChromeFragment()
            }
        }
        if (currentFragment == targetFragment) {
            return
        }
        childFragmentManager.beginTransaction().apply {
            currentFragment?.let { hide(it) }

            if (!targetFragment.isAdded) {
                add(binding.flContainer.id, targetFragment, state.name)
            } else {
                show(targetFragment)
            }
            commitNowAllowingStateLoss()
        }
        currentFragment = targetFragment
    }

    private fun createUpFingerAnimation(up: Boolean = true) {
        cancelAnimations()
        fingerAnimator = ObjectAnimator.ofFloat(
            if (up) binding.llFingerUp else binding.llGuideDown,
            "translationY",
            0f,
            -20f
        ).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private fun startAnimations() {
        fingerAnimator?.start()
    }

    private fun cancelAnimations() {
        fingerAnimator?.cancel()
        fingerAnimator = null
    }

    private fun autoShowKeyboard(editText: AppCompatEditText) {
        Handler(Looper.getMainLooper()).postDelayed({
            showKeyboard(editText)
        }, 50)
    }

    private fun showKeyboard(editText: AppCompatEditText) {
        editText.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(editText: AppCompatEditText) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun isKeyboardReallyShowing(context: Context, rootView: View): Boolean {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val visibleHeight = rect.bottom - rect.top

        val heightDiff = screenHeight - visibleHeight
        val keyboardThreshold = screenHeight / 4

        return heightDiff > keyboardThreshold
    }


    fun showDownloadDialog() {
        searchViewModel.videos.value?.let {video->
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser9, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
            video.forEach { it.isSelect = true }
            val downloadDialog = DownloadDialog()
            downloadDialog.updateData(video)
            downloadDialog.setOnCancelListener {
                TrackMgr.instance.trackAdEvent(AdPosition.START_DOWNLOAD, AdType.INTERSTITIAL, TrackEventType.safedddd_bg)

                val hasCache = AdMgr.INSTANCE.getAdLoadState(AdPosition.START_DOWNLOAD, AdType.INTERSTITIAL) == AdLoadState.LOADED

                if (hasCache){
                    lifecycleScope.launch {
                        AdMgr.INSTANCE.showAd(
                            AdPosition.START_DOWNLOAD,
                            AdType.INTERSTITIAL,
                            requireActivity(),
                            onShowResult={ position, adType, success, error->
                                LogUtils.d("广告: ${error?.message}${error?.domain}")
                                if (error?.code == -2){
                                    viewModel.preloadSdAd(requireContext())
                                    handleStartDownload(video)
                                }
                            },
                            onAdDismissed = { position, adType ->
                                viewModel.preloadSdAd(requireContext())
                                handleStartDownload(video)
                            })
                    }
                    return@setOnCancelListener
                }
                viewModel.preloadSdAd(requireContext())
                handleStartDownload(video)
            }
            downloadDialog.show(this.childFragmentManager, "DownloadDialog")
            viewModel.preloadSdAd(requireContext())
        }
    }
    private fun handleStartDownload(video:MutableList<Video>){
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser10, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
        if (PUtils.hasStoragePermission(requireContext())) {
            val vf = video.filter { fv-> fv.isSelect }
            showTaskCreate(vf.toMutableList())
            if (video.size == 1 && video[0].url.contains("android.resource:")){
                return
            }
            DownloadTaskManager.INSTANCE.startResumeTask(vf.toMutableList())
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_down1, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
        } else {
            if (permissionDenied){
                mainViewModel.isFromPermissionBack = true
                goToPermissionSetting()
                return
            }
            requestPermissionLauncher.launch(arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }
    private fun showTaskCreate(video: MutableList<Video>){
        if (taskCreatD == null){
            taskCreatD = DownloadStatusDialog()
        }
        if (taskCreatD?.isFragmentShowing() == true){
            taskCreatD?.dismissNow()
        }
        taskCreatD?.setIsComplete(false)
        taskCreatD?.setOnConfirmListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser12, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
            if (searchViewModel.videos.value?.isNotEmpty() == true){
                if (video.size == 1 && video[0].url.contains("android.resource:")){
                    mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.PLAYER))
                    return@setOnConfirmListener
                }
                mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.DOWNLOAD, video))
            }
        }
        taskCreatD?.show(this.childFragmentManager, "DownloadDialog")
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser11, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
        val videos = searchViewModel.videos.value?: mutableListOf()
        if (videos.size == 1 && videos[0].url.contains("android.resource:")){
            Handler(Looper.getMainLooper()).postDelayed({
                taskCreatD?.dismissNow()
                val playList = DownloadTaskManager.INSTANCE.getPlayList()
                DownloadTaskManager.INSTANCE.processNewVideos(playList,videos)
                videos[0].downloadCompletedTime = System.currentTimeMillis()
                playList.add(videos[0])
                AppCache.playVideos = Json.encodeToString(playList)
                RawResourceUtils.copyRawVideoToPrivatePath(
                    App.getAppContext(),
                    R.raw.sample,
                    videos[0].fileName+".mp4"
                )
                showTaskComp()
            }, 2000)
        }
    }
    private fun showTaskComp(){
        if (taskCD == null){
            taskCD = DownloadStatusDialog()
        }
        if (taskCD?.isFragmentShowing() == true){
            return
        }
        taskCD?.setIsComplete(true)
        taskCD?.setOnConfirmListener {
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser14, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
            mainViewModel.navigate(NavigationItem("", NavState.SEARCH, NavState.PLAYER))
        }
        taskCD?.show(this.childFragmentManager, "DownloadDialog")
        TrackMgr.instance.trackEvent(TrackEventType.safedddd_browser13, mapOf("safedddd" to binding.etSearch.text.toString().trim()))
    }
    private fun goToPermissionSetting() {
        App.isJumpingToSystemSetting = true
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.all { it.value }
        if (allGranted) {
            val videos = searchViewModel.videos.value?: mutableListOf()
            val vf = videos.filter { fv-> fv.isSelect }
            showTaskCreate(vf.toMutableList())
            if (videos.size == 1 && videos[0].url.contains("android.resource:")){
                return@registerForActivityResult
            }
            DownloadTaskManager.INSTANCE.startResumeTask(vf.toMutableList())
            TrackMgr.instance.trackEvent(TrackEventType.safedddd_down1, mapOf("safedddd" to binding.etSearch.text.toString().trim()))

        } else {
            permissionDenied = true
        }
    }

}