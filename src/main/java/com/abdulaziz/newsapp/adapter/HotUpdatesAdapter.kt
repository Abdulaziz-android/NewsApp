package com.abdulaziz.newsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulaziz.newsapp.databinding.ItemHotNewsBinding
import com.abdulaziz.newsapp.models.News
import com.squareup.picasso.Picasso

class HotUpdatesAdapter(
    private val list: List<News>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<HotUpdatesAdapter.NewsVH>() {

    inner class NewsVH(private val itemBinding: ItemHotNewsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(news: News) {
            itemBinding.apply {
                titleTv.text = news.title
                dateTv.text = news.date
                descriptionTv.text = news.description
                Picasso.get().load(news.image_url).into(imageView)
                root.setOnClickListener {
                    listener.onItemClick(news)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVH {
        return NewsVH(
            ItemHotNewsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NewsVH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = if (list.size>5) 5 else list.size


    interface OnItemClickListener {
        fun onItemClick(news: News)
    }
}