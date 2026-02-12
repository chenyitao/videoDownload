package com.download.video_download.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.room.entity.Video
import com.download.video_download.databinding.DialogDownloadBottomBinding
import com.download.video_download.ui.adapter.WebDownloadAdapter

class DownloadDialog : DialogFragment() {
    
    private var _binding: DialogDownloadBottomBinding? = null
    private val binding get() = _binding!!
    private var onCancelListener: ((MutableList<Video>) -> Unit)? = null
    lateinit var adapter: WebDownloadAdapter
    var videos: MutableList<Video> = mutableListOf()
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
        _binding = DialogDownloadBottomBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }
    
    private fun initViews() {
        dialog?.window?.apply {
            val screenHeight = resources.displayMetrics.heightPixels
            val maxHeight = (screenHeight * 0.7f).toInt()
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                maxHeight
            )
            setGravity(android.view.Gravity.BOTTOM)
            attributes = attributes?.apply {
                windowAnimations = android.R.style.Animation_Dialog
                val screenHeight = resources.displayMetrics.heightPixels
                height = WindowManager.LayoutParams.WRAP_CONTENT
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            }
        }

        adapter = WebDownloadAdapter {
            videos.forEach {video ->
                if (video.url == it.url){
                    video.isSelect = it.isSelect
                }
            }
            updateSelection()
        }
        binding.rvVideo.adapter = adapter
        adapter.updateData(videos)
    }
    private fun updateSelection() {
        val isAllSelected1 = if (videos.isEmpty()) false else videos.all { it.isSelect }
        binding.tvAllSelect.text = if (isAllSelected1) getString(R.string.deselect_all) else getString(
            R.string.select_all
        )
        val count = videos.count { it.isSelect }
        binding.btnCancel.text = if (count == 0) getString(R.string.nav_download) else getString(
            R.string.fast_download,
            count
        )
        binding.btnCancel.background = if (count == 0) ContextCompat.getDrawable(
            App.getAppContext(),R.drawable.shape_717171_8,
        ) else ContextCompat.getDrawable(
            App.getAppContext(),R.drawable.shape_red_botton_8,
        )
    }
    private fun initListeners() {
        binding.btnCancel.setOnClickListener {
            onCancelListener?.invoke(videos)
        }
        binding.tvAllSelect.setOnClickListener {
            if (binding.tvAllSelect.text.toString() == getString(R.string.select_all)){
                videos.forEach {
                    it.isSelect = true
                }
            }else{
                videos.forEach {
                    it.isSelect = false
                }
            }
            adapter.updateData(videos)
            updateSelection()
        }
        binding.back.setOnClickListener {
            dismiss()
        }
    }
    fun setOnCancelListener(listener: (MutableList<Video>) -> Unit) {
        this.onCancelListener = listener
    }
    fun updateData(newList: MutableList<Video>) {
        this.videos = newList
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}