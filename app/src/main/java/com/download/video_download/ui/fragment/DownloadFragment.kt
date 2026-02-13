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
import com.download.video_download.base.task.AriaDownloadManager
import com.download.video_download.base.utils.PUtils

import com.download.video_download.databinding.FragmentDownloadBinding
import com.download.video_download.ui.adapter.DownloadTaskAdapter
import com.download.video_download.ui.dialog.DownloadCancelDialog
import com.download.video_download.ui.dialog.DownloadDialog
import com.download.video_download.ui.dialog.DownloadStatusDialog
import com.download.video_download.ui.viewmodel.DownloadViewModel
import com.download.video_download.ui.viewmodel.MainViewModel

class DownloadFragment : BaseFragment<DownloadViewModel, FragmentDownloadBinding>() {
    val downloadViewModel: DownloadViewModel by viewModels()
    val mainViewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    var adapter: DownloadTaskAdapter? = null
    var taskManager: AriaDownloadManager? = null
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadBinding {
        return FragmentDownloadBinding.inflate(inflater)
    }

    override fun createViewModel(): DownloadViewModel = downloadViewModel

    override fun initViews(savedInstanceState: Bundle?) {
        taskManager = AriaDownloadManager(requireActivity())
        adapter = DownloadTaskAdapter({
            when (it.downloadStatus) {
                IEntity.STATE_RUNNING -> {
                    taskManager?.stop(it.id)
                }
                IEntity.STATE_STOP, IEntity.STATE_CANCEL -> {
                    taskManager?.resume(it.id)
                }
                IEntity.STATE_FAIL -> {
                    taskManager?.resume(it.id)
                }
            }
        },{
            val downloadDialog = DownloadCancelDialog()
            downloadDialog.setOnConfirmListener {
                taskManager?.cancel(it.id)
            }
            downloadDialog.show(this.childFragmentManager, "DownloadDialog")
        },{
            val downloadDialog = DownloadStatusDialog()
            downloadDialog.setIsComplete(true)
            downloadDialog.setOnConfirmListener {
            }
            downloadDialog.show(this.childFragmentManager, "DownloadDialog")
        })
        binding.rvDownload.adapter = adapter
        mainViewModel.nav.observe( this){
            taskManager?.startResumeDownloadTask( it?.video?:mutableListOf())
        }
    }

    override fun initListeners() {
        taskManager?.videoItems?.observe( this){
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
//        if (adapter?.getData()?.isEmpty() == true){
//            binding.rlEmpty.visibility = View.VISIBLE
//            taskManager?.startResumeDownloadTask()
//        }else{
//            binding.rlEmpty.visibility = View.GONE
//        }
    }

    override fun onPause() {
        super.onPause()
    }
}