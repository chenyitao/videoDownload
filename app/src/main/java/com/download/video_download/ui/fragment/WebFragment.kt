package com.download.video_download.ui.fragment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.model.History
import com.download.video_download.base.model.SearchState
import com.download.video_download.databinding.FragmentSearchBinding
import com.download.video_download.ui.viewmodel.SearchViewModel
import kotlin.getValue

class WebFragment : BaseFragment<SearchViewModel, FragmentSearchBinding>() {
    val searchViewModel: SearchViewModel by viewModels()
    private var upFingerAnimator: ObjectAnimator? = null
    private var downFingerAnimator: ObjectAnimator? = null
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel  = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        loadFragment(SearchState.GUIDE)
        handleUpFingerUpDownAnimation()
        handleDownFingerUpDownAnimation()
    }

    override fun initListeners() {
        binding.tvSearch.setOnClickListener {
            val inputContent = binding.etSearch.text.toString().trim()
            if (inputContent.isNotEmpty()) {
                searchViewModel.addHistory(History(inputContent, System.currentTimeMillis()))
            }
        }
        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP
            if (isSearchAction || isEnterKey) {
                val inputContent = v.text.toString().trim()

                if (inputContent.isNotEmpty()) {
                    searchViewModel.addHistory(History(inputContent, System.currentTimeMillis()))
                }
                return@OnEditorActionListener true
            }
            false
        })
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
    private fun loadFragment(state: SearchState) {
        val fragment = when (state) {
            SearchState.GUIDE -> WebGuideFragment()
            SearchState.HISTORY -> WebHistoryFragment()
            SearchState.WEB -> WebChromeFragment()
        }
        fragment.let {
            childFragmentManager.beginTransaction()
                .replace(binding.flContainer.id, it)
                .commit()
        }
    }
    private fun createUpFingerUpDownAnimation(){
        upFingerAnimator = ObjectAnimator.ofFloat(binding.llFingerUp, "translationY", 0f, -20f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    private fun startUpAnimations() {
        upFingerAnimator?.start()
    }

    private fun cancelUpAnimations() {
        upFingerAnimator?.cancel()
        upFingerAnimator = null
    }
    private fun handleUpFingerUpDownAnimation() {
        if (binding.upFloatingGuide.visibility == ViewGroup.VISIBLE){
            createUpFingerUpDownAnimation()
            startUpAnimations()
        }else{
            cancelUpAnimations()
        }
    }
    private fun createDownFingerUpDownAnimation(){
        downFingerAnimator = ObjectAnimator.ofFloat(binding.llGuideDown, "translationY", 0f, -20f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    private fun startDownAnimations() {
        downFingerAnimator?.start()
    }

    private fun cancelDownAnimations() {
        downFingerAnimator?.cancel()
        downFingerAnimator = null
    }
    private fun handleDownFingerUpDownAnimation() {
        if (binding.llGuideDown.visibility == ViewGroup.VISIBLE){
            createDownFingerUpDownAnimation()
            startDownAnimations()
        }else{
            cancelDownAnimations()
        }
    }
}