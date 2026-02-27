package com.download.video_download.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.arialyy.aria.core.inf.IEntity
import com.download.video_download.base.BaseFragment
import com.download.video_download.base.model.NavState
import com.download.video_download.base.model.NavigationItem
import com.download.video_download.base.task.DownloadTaskManager

import com.download.video_download.databinding.FragmentDownloadBinding
import com.download.video_download.ui.adapter.DownloadTaskAdapter
import com.download.video_download.ui.dialog.DownloadCancelDialog
import com.download.video_download.ui.dialog.DownloadDialog
import com.download.video_download.ui.dialog.DownloadStatusDialog
import com.download.video_download.ui.dialog.isFragmentShowing
import com.download.video_download.ui.viewmodel.DownloadViewModel
import com.download.video_download.ui.viewmodel.MainViewModel

class DownloadFragment : BaseFragment<DownloadViewModel, FragmentDownloadBinding>() {
    val downloadViewModel: DownloadViewModel by viewModels()
    val mainViewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    var adapter: DownloadTaskAdapter? = null
    var downloadDialog:DownloadStatusDialog? = null
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadBinding {
        return FragmentDownloadBinding.inflate(inflater)
    }

    override fun createViewModel(): DownloadViewModel = downloadViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        adapter = DownloadTaskAdapter({
            when (it.downloadStatus) {
                IEntity.STATE_RUNNING -> {
                    DownloadTaskManager.INSTANCE.stop(it.id)
                }
                IEntity.STATE_STOP, IEntity.STATE_CANCEL -> {
                    DownloadTaskManager.INSTANCE.resume(it.id)
                }
                IEntity.STATE_FAIL -> {
                    DownloadTaskManager.INSTANCE.resume(it.id)
                }
            }
        },{
            val downloadDialog = DownloadCancelDialog()
            downloadDialog.setOnConfirmListener {
                DownloadTaskManager.INSTANCE.cancel(it.id)
            }
            downloadDialog.show(this.childFragmentManager, "DownloadDialog")
        },{
            if (adapter?.getData()?.any { video ->
                    video.id == it.id && video.url == it.url
                } == true) {
                adapter?.removeItem(it)
            }
            if (downloadDialog?.isFragmentShowing() == true) {
                return@DownloadTaskAdapter
            }
            downloadDialog = DownloadStatusDialog().apply {
                setIsComplete(true)
                setOnConfirmListener {
                    mainViewModel.navigate(NavigationItem("", NavState.DOWNLOAD, NavState.PLAYER))
                }
            }
            downloadDialog?.show(this.childFragmentManager, "DownloadDialog")
        })
        binding.rvDownload.adapter = adapter
        if (DownloadTaskManager.INSTANCE.videoItems.value?.isEmpty() == true){
            if (adapter?.getData()?.isEmpty() == true){
                binding.rlEmpty.visibility = View.VISIBLE
                DownloadTaskManager.INSTANCE.startResumeTask()
        }else{
            binding.rlEmpty.visibility = View.GONE
        }
        }
    }

    override fun initListeners() {
        DownloadTaskManager.INSTANCE.videoItems.observe( this){
            if (it.isEmpty()){
                binding.rlEmpty.visibility = View.VISIBLE
            }else{
                binding.rlEmpty.visibility = View.GONE
            }
            adapter?.updateData( it)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}