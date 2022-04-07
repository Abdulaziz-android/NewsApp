package com.abdulaziz.newsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulaziz.newsapp.databinding.ItemNewsBinding
import com.abdulaziz.newsapp.models.News
import com.squareup.picasso.Picasso

class NewsAdapter(
    private val list: List<News>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<NewsAdapter.NewsVH>() {

    inner class NewsVH(private val itemBinding: ItemNewsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(news: News) {
            itemBinding.apply {
                titleTv.text = news.title
                authorNameTv.text = news.author
                dateTv.text = news.date
                Picasso.get().load(news.image_url).into(imageView)
                root.setOnClickListener {
                    listener.onItemClick(news)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVH {
        return NewsVH(ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NewsVH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = if (list.size>8) 8 else list.size

    interface OnItemClickListener {
        fun onItemClick(news: News)
    }
}