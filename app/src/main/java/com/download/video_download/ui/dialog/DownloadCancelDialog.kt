package com.download.video_download.ui.dialog

import android.R
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.download.video_download.databinding.DialogDownloadCancelBinding

/**
 * 清除所有历史记录确认弹窗
 */
class DownloadCancelDialog : DialogFragment() {
    
    private var _binding: DialogDownloadCancelBinding? = null
    private val binding get() = _binding!!
    
    private var onConfirmListener: (() -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDownloadCancelBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }
    
    private fun initViews() {
        // 设置对话框样式
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun initListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }
    }
    
    /**
     * 设置确认按钮点击监听器
     */
    fun setOnConfirmListener(listener: () -> Unit) {
        this.onConfirmListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}