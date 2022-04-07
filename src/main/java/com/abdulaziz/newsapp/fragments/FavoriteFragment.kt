package com.abdulaziz.newsapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.abdulaziz.newsapp.MainActivity
import com.abdulaziz.newsapp.R
import com.abdulaziz.newsapp.adapter.NewsEntityAdapter
import com.abdulaziz.newsapp.database.AppDatabase
import com.abdulaziz.newsapp.database.NewsEntity
import com.abdulaziz.newsapp.databinding.FragmentFavoriteBinding
import com.abdulaziz.newsapp.models.News


class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var adapter: NewsEntityAdapter
    private lateinit var list: ArrayList<NewsEntity>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as MainActivity).showBottomNavBar()
        _binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)

        database = AppDatabase.getInstance(requireContext())
        list = arrayListOf()
        list.addAll(database.newsDao().getAllNews())

        adapter = NewsEntityAdapter(list, object : NewsEntityAdapter.OnItemClickListener {
            override fun onItemClick(news: NewsEntity) {
                val bundle = bundleOf(Pair("news",
                    News(news.author,
                        news.title,
                        news.description,
                        news.text,
                        news.category,
                        news.date,
                        news.timestamp,
                        news.image_url,
                        news.view_count)))
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    NewsDetailFragment::class.java, bundle).addToBackStack("Detail").commit()
            }

            override fun onLikeClick(news: NewsEntity, position: Int) {
                database.newsDao().deleteNews(news)
                list.remove(news)
                if (list.isEmpty()) binding.emptyTv.visibility = View.VISIBLE
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, list.size)
            }

        })

        if (list.isNotEmpty()) {
            binding.rv.adapter = adapter
            binding.emptyTv.visibility = View.GONE
        }

        return binding.root
    }


    override fun onStop() {
        super.onStop()
        (activity as MainActivity).hideBottomNavBar()
    }
}