package com.download.video_download.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.databinding.DialogDownloadTaskBinding

/**
 * 清除所有历史记录确认弹窗
 */
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
        _binding = null
    }
    fun setIsComplete(isComplete: Boolean) {
        this.isCompete = isComplete
    }
}