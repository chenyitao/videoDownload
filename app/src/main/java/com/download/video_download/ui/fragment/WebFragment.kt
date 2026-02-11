package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
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
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun createViewModel(): SearchViewModel  = searchViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        loadFragment(SearchState.HISTORY)
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
}