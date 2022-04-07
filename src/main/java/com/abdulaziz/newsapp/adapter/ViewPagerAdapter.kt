package com.abdulaziz.newsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulaziz.newsapp.databinding.ItemPagerBinding
import com.abdulaziz.newsapp.models.News
import com.squareup.picasso.Picasso

class ViewPagerAdapter(
    private val list: List<News>,
    private val listener: OnPageClickListener
) : RecyclerView.Adapter<ViewPagerAdapter.PagerVH>() {

    inner class PagerVH(private val itemBinding: ItemPagerBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(news: News) {
            itemBinding.apply {
                Picasso.get().load(news.image_url).into(itemBinding.imageView)
                authorNameTv.text = news.author
                titleTv.text = news.title
                descriptionTv.text = news.description
                root.setOnClickListener {
                    listener.onPageClick(news)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        return PagerVH(ItemPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PagerVH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface OnPageClickListener {
        fun onPageClick(news: News)
    }
}