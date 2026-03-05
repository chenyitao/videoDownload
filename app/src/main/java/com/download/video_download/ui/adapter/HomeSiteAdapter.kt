package com.download.video_download.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.download.video_download.App
import com.download.video_download.R
import com.download.video_download.base.model.LanguageSelectData
import com.download.video_download.base.model.WebsiteData
import com.download.video_download.databinding.ItemHomeWebsiteBinding
import com.download.video_download.databinding.ItemLanguageBinding

class HomeSiteAdapter(
    private val onItemClick: (WebsiteData) -> Unit
) : RecyclerView.Adapter<HomeSiteAdapter.WebSiteHolder>() {
    
    private var websiteList: MutableList<WebsiteData> = mutableListOf()

    fun updateData(newList: List<WebsiteData>) {
        websiteList.clear()
        websiteList.addAll(newList)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebSiteHolder {
        val binding = ItemHomeWebsiteBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return WebSiteHolder(binding)
    }
    
    override fun onBindViewHolder(holder: WebSiteHolder, position: Int) {
        holder.bind(websiteList[position])
    }
    
    override fun getItemCount(): Int = websiteList.size
    inner class WebSiteHolder(
        private val binding: ItemHomeWebsiteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(data: WebsiteData) {
            if (data.color != -1){
                binding.ivSite.visibility = View.GONE
                binding.ivSiteT.visibility = View.VISIBLE
                binding.tvSiteT.text = data.title.first().uppercaseChar().toString()
                binding.tvSiteT.background = ContextCompat.getDrawable(App.getAppContext(), data.color)
                binding.tvSite.text = data.title
            }else{
                binding.ivSite.visibility = View.VISIBLE
                binding.ivSiteT.visibility = View.GONE
                binding.ivSite.setImageResource(data.image)
                binding.tvSite.text = data.title
            }
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(data)
                }
            }
        }
    }
}