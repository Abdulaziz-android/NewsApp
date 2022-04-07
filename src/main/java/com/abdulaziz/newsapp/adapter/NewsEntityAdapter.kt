package com.abdulaziz.newsapp.adapter

import android.animation.Animator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulaziz.newsapp.database.NewsEntity
import com.abdulaziz.newsapp.databinding.ItemNewsWithLikeBinding
import com.squareup.picasso.Picasso

class NewsEntityAdapter(
    private val list: List<NewsEntity>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<NewsEntityAdapter.NewsVH>() {

    inner class NewsVH(private val itemBinding: ItemNewsWithLikeBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(news: NewsEntity, position: Int) {
            itemBinding.apply {
                titleTv.text = news.title
                authorNameTv.text = news.author
                dateTv.text = news.date
                Picasso.get().load(news.image_url).into(imageView)
                root.setOnClickListener {
                    listener.onItemClick(news)
                }
                saveCard.setOnClickListener {
                    saveCard.animate().alpha(0f)
                        .scaleX(0f)
                        .setDuration(500)
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator?) {

                            }

                            override fun onAnimationEnd(p0: Animator?) {
                                listener.onLikeClick(news, position)
                            }

                            override fun onAnimationCancel(p0: Animator?) {

                            }

                            override fun onAnimationRepeat(p0: Animator?) {

                            }

                        })
                        .start()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVH {
        return NewsVH(
            ItemNewsWithLikeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NewsVH, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    interface OnItemClickListener {
        fun onItemClick(news: NewsEntity)
        fun onLikeClick(news: NewsEntity, position: Int)
    }
}