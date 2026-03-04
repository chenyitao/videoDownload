package com.download.video_download.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.download.video_download.databinding.DialogFdBinding

class FDDialog: DialogFragment() {
    private var _binding: DialogFdBinding? = null
    private val binding get() = _binding!!
    private var onKeep: (() -> Unit)? = null
    private var onStill: (() -> Unit)? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFdBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }
    private fun initViews() {
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(android.view.Gravity.BOTTOM)
            attributes = attributes?.apply {
                windowAnimations = android.R.style.Animation_Dialog
                height = WindowManager.LayoutParams.WRAP_CONTENT
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            }
        }
    }
    private fun initListeners() {
        binding.keep.setOnClickListener {
            onKeep?.invoke()
            dismiss()
        }
        binding.still.setOnClickListener {
            onStill?.invoke()
            dismiss()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun setOnKeep(listener: () -> Unit) {
        this.onKeep = listener
    }
    fun setOnStill(listener: () -> Unit) {
        this.onStill = listener
    }
}