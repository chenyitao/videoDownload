package com.download.video_download.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.inf.IEntity
import com.bumptech.glide.Glide
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.model.LanguageSelectData
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.utils.TimeFormateUtils
import com.download.video_download.databinding.ItemDownloadBinding
import com.download.video_download.databinding.ItemLanguageBinding
import com.download.video_download.databinding.ItemNavDownloadBinding

class DownloadTaskAdapter(
    private val onItemClick: (Video) -> Unit,
    private val onItemDel: (Video) -> Unit,
    private val onComp: (Video) -> Unit
) : RecyclerView.Adapter<DownloadTaskAdapter.DownloadTaskViewHolder>() {
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var videoList: MutableList<Video> = mutableListOf()

    fun updateData(newList: List<Video>) {
        videoList.clear()
        videoList.addAll(newList)
        notifyDataSetChanged()
    }
    fun removeItem(video: Video) {
        val index = videoList.indexOfFirst {
            it.id == video.id && it.url == video.url && it.fileName == video.fileName
        }

        if (index >= 0) {
            videoList.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, videoList.size)
        }
    }

    fun updateSelection(selectedPosition: Int) {
        val newState = !videoList[selectedPosition].isSelect
        videoList[selectedPosition].isSelect = newState
        notifyItemChanged(selectedPosition)
    }
    fun getData() = videoList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadTaskViewHolder {
        val binding = ItemNavDownloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DownloadTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadTaskViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun getItemCount(): Int = videoList.size

    inner class DownloadTaskViewHolder(
        private val binding: ItemNavDownloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Video) {
            binding.progress.progressDrawable = ContextCompat.getDrawable(App.getAppContext(),R.drawable.web_progress_style_green)
            binding.taskName.text = data.fileName
            binding.taskDPercent.text = data.downloadProcess
            binding.progress.progress = data.process.toInt()
            binding.taskSpeed.setTextColor(ContextCompat.getColor(App.getAppContext(),R.color.color_19C81C))
            binding.taskAction.visibility = View.VISIBLE
            Log.e("task","taskname:${data.fileName}  ===== " +data.downloadStatus)
            when (data.downloadStatus) {
                IEntity.STATE_RUNNING -> {
                    binding.taskAction.setImageResource(R.mipmap.ic_task_run)
                    binding.taskSpeed.text = data.speed
                }
                IEntity.STATE_STOP, IEntity.STATE_CANCEL -> {
                    binding.taskAction.setImageResource(R.mipmap.ic_task_pause)
                    binding.taskSpeed.text = "--B/S"
                }
                IEntity.STATE_FAIL -> {
                    binding.progress.progressDrawable = ContextCompat.getDrawable(App.getAppContext(),R.drawable.web_progress_style_yellow)
                    binding.taskAction.setImageResource(R.mipmap.ic_task_error)
                    binding.taskSpeed.setTextColor(ContextCompat.getColor(App.getAppContext(),R.color.color_FF951B))
                    binding.taskSpeed.text= App.getAppContext().getString(R.string.failed_to_connect)
                }
                else->{
                    if (data.downloadStatus == IEntity.STATE_WAIT){
                        binding.taskSpeed.setTextColor(ContextCompat.getColor(App.getAppContext(),R.color.white))
                        binding.taskSpeed.text= App.getAppContext().getString(R.string.pending)
                    }else if (data.downloadStatus == IEntity.STATE_POST_PRE){
                        binding.taskSpeed.setTextColor(ContextCompat.getColor(App.getAppContext(),R.color.color_19C81C))
                        binding.taskSpeed.text= App.getAppContext().getString(R.string.connecting)
                    }

                    if (data.downloadStatus == IEntity.STATE_COMPLETE){
                        mainHandler.post {
                            onComp.invoke(data)
                        }
                    }
                    binding.taskAction.visibility = View.INVISIBLE
                }
             }
            binding.taskAction.setOnClickListener {
                onItemClick.invoke(data)
            }
            binding.avDel.setOnClickListener {
                onItemDel.invoke(data)
            }
            if (data.thumb.isNotEmpty()) {
                Glide.with(binding.ivLogo.context)
                    .load(data.thumb)
                    .placeholder(R.mipmap.ic_video_default)
                    .into(binding.ivLogo)
            } else {
                binding.ivLogo.setImageResource(R.mipmap.ic_video_default)
            }
            binding.tvDuration.visibility = if (data.duration > 0) View.VISIBLE else View.GONE
            binding.tvDuration.text = TimeFormateUtils.formatTime(data.duration)
        }
    }
}