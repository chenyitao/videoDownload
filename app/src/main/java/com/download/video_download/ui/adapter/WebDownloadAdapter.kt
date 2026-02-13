package com.download.video_download.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.download.video_download.R
import com.download.video_download.base.model.LanguageSelectData
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.utils.TimeFormateUtils
import com.download.video_download.databinding.ItemDownloadBinding
import com.download.video_download.databinding.ItemLanguageBinding

class WebDownloadAdapter(
    private val onItemClick: (Video) -> Unit
) : RecyclerView.Adapter<WebDownloadAdapter.WebDownloadViewHolder>() {

    private var videoList: MutableList<Video> = mutableListOf()

    fun updateData(newList: List<Video>) {
        videoList.clear()
        videoList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateSelection(selectedPosition: Int) {
        val newState = !videoList[selectedPosition].isSelect
        videoList[selectedPosition].isSelect = newState
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebDownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WebDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WebDownloadViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun getItemCount(): Int = videoList.size

    inner class WebDownloadViewHolder(
        private val binding: ItemDownloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Video) {
            binding.tvTitle.text = data.fileName
            binding.ivCheck.setImageResource(if (data.isSelect) R.mipmap.ic_select else R.mipmap.ic_unselect)
            if (data.thumb.isNotEmpty()) {
                Glide.with(binding.logo.context)
                    .load(data.thumb)
                    .placeholder(R.mipmap.ic_video_default)
                    .into(binding.logo)
            } else {
                binding.logo.setImageResource(R.mipmap.ic_video_default)
            }
            binding.tvTme.visibility = if (data.duration > 0) View.VISIBLE else View.GONE
            binding.tvTme.text = TimeFormateUtils.formatTime(data.duration)
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    updateSelection(currentPosition)
                    onItemClick(data)
                }
            }
        }
    }
}