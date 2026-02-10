package com.download.video_download.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.download.video_download.base.model.LanguageSelectData
import com.download.video_download.databinding.ItemLanguageBinding

/**
 * 语言选择列表适配器
 */
class LanguageAdapter(
    private val onItemClick: (LanguageSelectData) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    
    private var languageList: MutableList<LanguageSelectData> = mutableListOf()
    
    /**
     * 更新语言列表数据
     */
    fun updateData(newList: List<LanguageSelectData>) {
        languageList.clear()
        languageList.addAll(newList)
        notifyDataSetChanged()
    }
    
    /**
     * 更新选中状态
     */
    fun updateSelection(selectedPosition: Int) {
        languageList.forEachIndexed { index, data ->
            data.isSelected = (index == selectedPosition)
        }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return LanguageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languageList[position])
    }
    
    override fun getItemCount(): Int = languageList.size
    
    /**
     * ViewHolder 内部类
     */
    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(data: LanguageSelectData) {
            // 设置语言名称
            binding.tvLg.text = data.language
            
            // 设置语言图标
            binding.ivLg.setImageResource(data.languageIv)
            
            // 设置选中状态图标
            if (data.isSelected) {
                binding.ivLgCheck.setImageResource(com.download.video_download.R.mipmap.ic_check)
                binding.ivLgCheck.visibility = android.view.View.VISIBLE
            } else {
                binding.ivLgCheck.visibility = android.view.View.GONE
            }
            
            // 设置点击事件
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(data)
                    updateSelection(currentPosition)
                }
            }
        }
    }
}