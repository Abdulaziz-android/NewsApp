package com.abdulaziz.newsapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.abdulaziz.newsapp.MainActivity
import com.abdulaziz.newsapp.R
import com.abdulaziz.newsapp.database.AppDatabase
import com.abdulaziz.newsapp.database.ChannelEntity
import com.abdulaziz.newsapp.database.NewsEntity
import com.abdulaziz.newsapp.databinding.FragmentNewsDetailBinding
import com.abdulaziz.newsapp.models.News
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import eightbitlab.com.blurview.RenderScriptBlur

private const val ARG_PARAM1 = "news"


class NewsDetailFragment : Fragment() {

    private var news: News? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            news = it.getSerializable(ARG_PARAM1) as News
        }
    }

    private var _binding: FragmentNewsDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private var isSaved = false
    private var isSubs = false
    private var newsEntity: NewsEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNewsDetailBinding.inflate(layoutInflater, container, false)
        (activity as MainActivity).hideBottomNavBar()

        database = AppDatabase.getInstance(requireContext())
        binding.mytoolbar.setNavigationIcon(R.drawable.back_button_layer)

        binding.mytoolbar.setNavigationOnClickListener {
            (activity as MainActivity).onBackPressed()
        }


        database.newsDao().getAllNews().forEach {
            if (it.text == news?.text) {
                isSaved = true
                newsEntity = it
            }
        }
        database.newsDao().getAllChannels().forEach {
            if (it.name == news?.author) {
                isSubs = true
            }
        }


        binding.appbarlayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (binding.collapsingToolbar.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(
                    binding.collapsingToolbar)
            ) {
                binding.cardView.animate().alpha(0f).duration = 600
            } else {
                binding.cardView.animate().alpha(1f).duration = 600
            }
        })

        setUpBlurView()
        if (news != null) {
            loadData()
        }

        val reference = FirebaseDatabase.getInstance().getReference("news")
        reference.orderByChild("title").equalTo(news?.title)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (shot in snapshot.children) {
                        val value = shot.getValue(News::class.java)
                        binding.seenTv.text = value?.view_count.toString()
                        binding.seenTv.visibility = View.VISIBLE
                        shot.ref.child("view_count").setValue(value?.view_count?.plus(1))
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })


        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        if (newsEntity == null) {
            val lastNews = database.newsDao().getLastNews()
            val id = lastNews?.id ?: 0

            newsEntity = NewsEntity(id = id+1,
                author = news?.author!!,
                title = news?.title!!,
                description = news?.description!!,
                text = news?.text!!,
                category = news?.category!!,
                date = news?.date!!,
                timestamp = news?.timestamp!!,
                image_url = news?.image_url,
                view_count = news?.view_count!!)
        }
        binding.apply {
            Picasso.get().load(news?.image_url).into(imageView)
            authorNameTv.text = news?.author
            dateTv.text = news?.date
            titleTv.text = news?.title
            fullTextTv.text = news?.text
            if (isSubs) {
                followBtn.text = "Unfollow ${news?.author}"
            } else followBtn.text = "Follow ${news?.author}"
            if (isSaved) {
                saveIv.setImageResource(R.drawable.ic_double_like)
                saveCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.primary))
            }

            binding.saveCard.setOnClickListener {
                saveClick()
            }
            binding.followBtn.setOnClickListener {
                if (!isSubs) {
                    followBtn.text = "Unfollow ${news?.author}"
                    database.newsDao().insertChannel(ChannelEntity(news?.author!!))
                } else {
                    followBtn.text = "Follow ${news?.author}"
                    database.newsDao().deleteChannel(ChannelEntity(news?.author!!))
                }
                isSubs = !isSubs
            }
        }
    }

    private fun saveClick() {
        isSaved = !isSaved
        binding.apply {
            if (isSaved) {
                saveIv.setImageResource(R.drawable.ic_double_like)
                saveCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.primary))
                database.newsDao().insertNews(newsEntity!!)
            } else {
                saveIv.setImageResource(R.drawable.ic_double_like_red)
                saveCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.white))
                database.newsDao().deleteNews(newsEntity!!)
            }
        }
    }

    private fun setUpBlurView() {
        val radius = 20f
        val decorView: View = (activity as MainActivity).window.decorView
        val rootView = decorView.findViewById<View>(android.R.id.content) as ViewGroup
        val windowBackground = decorView.background

        binding.blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(requireContext()))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(true)
    }

}