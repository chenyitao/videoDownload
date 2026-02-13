package com.download.video_download.ui.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.inf.IEntity
import com.bumptech.glide.Glide
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.model.LanguageSelectData
import com.download.video_download.base.room.entity.Video
import com.download.video_download.base.utils.PopMenu
import com.download.video_download.base.utils.TimeFormateUtils
import com.download.video_download.databinding.ItemDownloadBinding
import com.download.video_download.databinding.ItemLanguageBinding
import com.download.video_download.databinding.ItemNavDownloadBinding
import com.download.video_download.databinding.ItemPlayerBinding

class PlayerAdapter(
    private val onItemClick: (Video) -> Unit,
    private val onItemDel: (Video) -> Unit,
    private val onItemShare: (Video) -> Unit,
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

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
    fun getData() = videoList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun getItemCount(): Int = videoList.size

    inner class PlayerViewHolder(
        private val binding: ItemPlayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Video) {
            binding.playerSize.text = convertBytesToHumanReadable(data.totalSize)
            binding.playerName.text = data.fileName
            binding.playerMenu.setOnClickListener {
                PopMenu.showCustomPopupMenu(binding.playerMenu,{
                    onItemShare.invoke( data)
                },{
                    onItemDel.invoke( data)
                })
            }
            binding.root.setOnClickListener {
                onItemClick(data)
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
    fun convertBytesToHumanReadable(bytes: Long): String {
        val kilobyte = 1024L
        val megabyte = kilobyte * 1024L
        val gigabyte = megabyte * 1024L

        return when {
            bytes >= gigabyte -> String.format("%.2f GB", bytes.toDouble() / gigabyte)
            bytes >= megabyte -> String.format("%.2f MB", bytes.toDouble() / megabyte)
            bytes >= kilobyte -> String.format("%.2f KB", bytes.toDouble() / kilobyte)
            bytes > 0 -> String.format("%.2f B", bytes.toDouble())
            else -> "0 B"
        }
    }
}