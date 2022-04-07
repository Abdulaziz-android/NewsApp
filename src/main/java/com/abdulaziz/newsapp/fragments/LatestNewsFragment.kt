package com.abdulaziz.newsapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.abdulaziz.newsapp.R
import com.abdulaziz.newsapp.adapter.NewsAdapter
import com.abdulaziz.newsapp.databinding.FragmentLatestNewsBinding
import com.abdulaziz.newsapp.models.News
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LatestNewsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = arrayListOf()
        adapter  = NewsAdapter(list, object : NewsAdapter.OnItemClickListener {
            override fun onItemClick(news: News) {
                val bundle = bundleOf(Pair("news", news))
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    NewsDetailFragment::class.java, bundle).addToBackStack("Detail").commit()
            }
        })
    }

    private var _binding:FragmentLatestNewsBinding?=null
    private val binding get() = _binding!!
    private lateinit var list: ArrayList<News>
    private lateinit var adapter:NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLatestNewsBinding.inflate(layoutInflater, container, false)

        binding.rv.adapter = adapter

        setUpFireStoreAndFetchData()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpFireStoreAndFetchData() {
        val db = FirebaseDatabase.getInstance()

        db.getReference("news").get().addOnCompleteListener {
            if (it.isSuccessful) {
                list.clear()
                it.result.children.forEach { ds ->
                    val news = ds.getValue(News::class.java)
                    list.add(news!!)
                }
                list.sortByDescending { news -> news.timestamp }
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
        }

    }
}