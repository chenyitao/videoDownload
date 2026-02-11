package com.download.video_download.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.download.video_download.base.model.History
import com.download.video_download.databinding.ItemHistoryBinding

/**
 * 语言选择列表适配器
 */
class HistoryAdapter(
    private val onItemClick: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    
    private var historyList: MutableList<History> = mutableListOf()
    fun updateData(newList: List<History>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return HistoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }
    
    override fun getItemCount(): Int = historyList.size

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(data: History) {
            binding.historyUrl .text = data.url
            binding.ivDelete.setOnClickListener{
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(data)
                }
            }
        }
    }
}