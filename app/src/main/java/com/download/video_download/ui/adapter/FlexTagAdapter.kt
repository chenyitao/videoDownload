package com.download.video_download.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.download.video_download.R
import com.download.video_download.base.model.History
import com.download.video_download.base.model.TagData
import com.download.video_download.databinding.ItemFlexboxBinding
import com.download.video_download.databinding.ItemHistoryBinding
class FlexTagAdapter(
    private val onItemClick: (TagData) -> Unit,
) : RecyclerView.Adapter<FlexTagAdapter.TagViewHolder>() {
    
    private var tagList: MutableList<TagData> = mutableListOf()
    fun updateData(newList: List<TagData>) {
        tagList.clear()
        tagList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemFlexboxBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return TagViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tagList[position])
    }
    
    override fun getItemCount(): Int = tagList.size

    inner class TagViewHolder(
        private val binding: ItemFlexboxBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(data: TagData) {
            binding.tag.background = if (data.isSelected) {
                ContextCompat.getDrawable(binding.root.context, R.drawable.shape_red_botton_6)
            } else {
                ContextCompat.getDrawable(binding.root.context, R.drawable.shape_ffffff33_6)
            }
           binding.tag.text =  data.title
            binding.tag.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    tagList.forEach {
                        if (it.title == data.title){
                            it.isSelected = true
                        }else{
                            it.isSelected = false
                        }
                    }
                    notifyDataSetChanged()
                    onItemClick(data)
                }
            }
        }
    }
}