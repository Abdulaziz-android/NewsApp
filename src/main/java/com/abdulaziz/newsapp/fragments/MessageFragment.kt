package com.abdulaziz.newsapp.fragments

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.abdulaziz.newsapp.MainActivity
import com.abdulaziz.newsapp.R
import com.abdulaziz.newsapp.adapter.HotUpdatesAdapter
import com.abdulaziz.newsapp.databinding.FragmentMessageBinding
import com.abdulaziz.newsapp.models.News
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = arrayListOf()

        adapter = HotUpdatesAdapter(list, object : HotUpdatesAdapter.OnItemClickListener{
            override fun onItemClick(news: News) {
                val bundle = bundleOf(Pair("news", news))
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    NewsDetailFragment::class.java, bundle).addToBackStack("Detail").commit()
            }
        })

    }

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter:HotUpdatesAdapter
    private lateinit var list:ArrayList<News>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        (activity as MainActivity).hideBottomNavBar()
        setUpFireStoreAndFetchData()

        binding.rv.adapter = adapter

        binding.backIv.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        val notificationManager =
            (activity as MainActivity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
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
                adapter.notifyDataSetChanged()
            }
        }

    }

}