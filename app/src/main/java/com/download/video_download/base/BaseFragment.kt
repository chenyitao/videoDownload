package com.download.video_download.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding> : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    lateinit var mContext: Context
    protected lateinit var viewModel: VM
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    protected open fun handleBackPressed(): Boolean {
        return false
    }
    protected open fun registerOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (handleBackPressed()) {
                    return
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createViewBinding(inflater, container)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerOnBackPressedCallback()
        viewModel = createViewModel()
        initViews(savedInstanceState)
        initData()
        initListeners()
    }
    protected abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected abstract fun createViewModel(): VM
    protected abstract fun initViews(savedInstanceState: Bundle?)
    protected open fun initData() {}
    protected abstract fun initListeners()
}