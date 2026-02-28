package com.download.video_download.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.ad.AdMgr
import com.download.video_download.base.ad.model.AdLoadState
import com.download.video_download.base.ad.model.AdPosition
import com.download.video_download.base.ad.model.AdType
import com.download.video_download.base.config.sensor.TrackEventType
import com.download.video_download.base.config.sensor.TrackMgr
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.utils.LogUtils
import com.download.video_download.databinding.DialogDownloadTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadStatusDialog : DialogFragment() {
    
    private var _binding: DialogDownloadTaskBinding? = null
    private val binding get() = _binding!!
    
    private var onConfirmListener: (() -> Unit)? = null
    private var isCompete: Boolean = false
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(R.color.color_FFFFFF00)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDownloadTaskBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        lifecycleScope.launch {
            TrackMgr.instance.trackAdEvent(AdPosition.DOWNLOAD_TASK_DIALOG, AdType.NATIVE, TrackEventType.safedddd_bg)

            val cache = AdMgr.INSTANCE.getAdLoadState(AdPosition.DOWNLOAD_TASK_DIALOG, AdType.NATIVE) == AdLoadState.LOADED
            if (!cache){
                initAd()
                return@launch
            }
            AdMgr.INSTANCE.showAd(AdPosition.DOWNLOAD_TASK_DIALOG, AdType.NATIVE,requireActivity(),
                onShowResult = { position, adType, success, error->
                    if (success){
                        LogUtils.d("ad:"+error?.domain+ error?.message)
                        AdMgr.INSTANCE.getNativeAd( position)?.let {
                            binding.taskAdView.visibility = View.VISIBLE
                            binding.taskAdView.setNativeAd(it,requireActivity())
                        }
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main){
                                delay(200)
                                initAd()
                            }
                        }
                    }
                })
        }
    }
    
    private fun initViews() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (isCompete) {
            binding.tvTitle.text = App.getAppContext().getString(R.string.download_task2)
            binding.tvMessage.text = App.getAppContext().getString(R.string.download_task2_green)
        } else {
            binding.tvTitle.text = App.getAppContext().getString(R.string.download_task1)
            binding.tvMessage.text = App.getAppContext().getString(R.string.download_task1_green)
            initAd()
        }
    }

    private fun initAd(){
        lifecycleScope.launch {
           AdMgr.INSTANCE.preloadAd(AdPosition.DOWNLOAD_TASK_DIALOG, AdType.NATIVE, requireActivity()
               ,onLoadStateChanged = { position, adType, state,error ->
                   LogUtils.d("ad:"+error?.domain+ error?.message)
           })
        }
    }
    
    private fun initListeners() {
        binding.btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }
    }
    fun setOnConfirmListener(listener: () -> Unit) {
        this.onConfirmListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.taskAdView.releaseAd()
        _binding = null
    }
    fun setIsComplete(isComplete: Boolean) {
        this.isCompete = isComplete
    }
}
fun DialogFragment.isFragmentShowing(): Boolean {
    return isAdded
            && isVisible
            && dialog?.isShowing == true
            && !isRemoving
}
fun DownloadStatusDialog?.isFragmentValidAndNotAdded(): Boolean {
    this ?: return true
    return !(this.isAdded || this.isRemoving || this.isDetached)
}