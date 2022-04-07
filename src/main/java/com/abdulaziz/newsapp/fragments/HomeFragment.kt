package com.abdulaziz.newsapp.fragments

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.abdulaziz.newsapp.MainActivity
import com.abdulaziz.newsapp.R
import com.abdulaziz.newsapp.adapter.NewsAdapter
import com.abdulaziz.newsapp.adapter.ViewPagerAdapter
import com.abdulaziz.newsapp.databinding.FragmentHomeBinding
import com.abdulaziz.newsapp.databinding.ItemTabBinding
import com.abdulaziz.newsapp.models.News
import com.abdulaziz.newsapp.notifications.Token
import com.abdulaziz.newsapp.transformer.CardTransformer
import com.abdulaziz.newsapp.transformer.HorizontalMarginItemDecoration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initListsAndAdapters()
        if (arguments?.getString("noti") != null) {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                MessageFragment()).addToBackStack("Message").commit()
        }
    }

    private fun initListsAndAdapters() {
        list = arrayListOf()
        oldList = arrayListOf()
        todayList = arrayListOf()
        filterList = arrayListOf()
        newsAdapter = NewsAdapter(filterList, object : NewsAdapter.OnItemClickListener {
            override fun onItemClick(news: News) {
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(requireActivity())
                    val realString = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Date())
                    sPref.edit().putString("inter_ad_time", realString).apply()
                }
                val bundle = bundleOf(Pair("news", news))
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    NewsDetailFragment::class.java, bundle).addToBackStack("Detail").commit()
            }
        })
        pagerAdapter = ViewPagerAdapter(todayList, object : ViewPagerAdapter.OnPageClickListener {
            override fun onPageClick(news: News) {
                val bundle = bundleOf(Pair("news", news))
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    NewsDetailFragment::class.java, bundle).addToBackStack("Detail").commit()
            }

        })
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var pagerAdapter: ViewPagerAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var list: ArrayList<News>
    private lateinit var oldList: ArrayList<News>
    private lateinit var filterList: ArrayList<News>
    private lateinit var todayList: ArrayList<News>
    private lateinit var sPref: SharedPreferences
    private var currentCategory = "Healthy"
    private var currentPosition = 0
    private var mInterstitialAd: InterstitialAd? = null
    private val TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        (activity as MainActivity).showBottomNavBar()
        setUpPager()
        setTabs(container)
        sPref = requireContext().getSharedPreferences("shared", Context.MODE_PRIVATE)


        setUpAds()


        binding.shimmerViewContainer.startShimmer()
        setUpFireStoreAndFetchData()
        setOnClicks()
        binding.tablayout.selectTab(binding.tablayout.getTabAt(currentPosition))

        instanceFirebase()


        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun setUpAds() {
        val timeString = sPref.getString("inter_ad_time", "00:00")
        val timeDate = SimpleDateFormat("HH:mm").parse(timeString)
        val realString = SimpleDateFormat("HH:mm").format(Date())
        val realDate = SimpleDateFormat("HH:mm").parse(realString)

        val different = realDate.time - timeDate.time
        val toMinutes = TimeUnit.MILLISECONDS.toMinutes(different)

        if (toMinutes > 15 || toMinutes < 0) {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(requireContext(),
                "ca-app-pub-3940256099942544/1033173712",
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                            mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                    }
                })
        } else mInterstitialAd = null
    }

    private fun setOnClicks() {
        binding.apply {
            bellCard.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MessageFragment())
                    .addToBackStack("Message").commit()
            }

            searchTv.setOnClickListener {
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    SearchFragment()).addToBackStack("Search").commit()
            }

            seeAllTv.setOnClickListener {
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    LatestNewsFragment()).addToBackStack("Latest").commit()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        val notificationManager =
            (activity as MainActivity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val size = notificationManager.activeNotifications.size
        if (size != 0) {
            binding.notiCard.visibility = View.VISIBLE
        }
    }

    private fun setUpFireStoreAndFetchData() {
        val db = FirebaseDatabase.getInstance()
        binding.rv.adapter = newsAdapter

        db.getReference("news").get().addOnCompleteListener {
            if (it.isSuccessful) {
                list.clear()
                it.result.children.forEach { dataSnapshot ->
                    val news = dataSnapshot.getValue(News::class.java)
                    news?.let { it1 -> list.add(it1) }
                }
                sortListAndSet(list)
            }
        }.addOnCanceledListener {
            Toast.makeText(binding.root.context, "cancelled!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(binding.root.context, "${it.message}", Toast.LENGTH_SHORT).show()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setList() {
        filterList.clear()
        oldList.forEach {
            if (it.category == currentCategory)
                filterList.add(it)
        }
        newsAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortListAndSet(list: ArrayList<News>) {
        todayList.clear()
        oldList.clear()
        val lastNews = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.max(list, Comparator.comparingLong { it.timestamp!! }) as News
        } else {
            var n = list[0]
            list.forEach { mn->
                if (mn.timestamp!! > n.timestamp!!){ n = mn }
            }
            n
        }
        list.forEach {
            if (lastNews.date==it.date) {
                todayList.add(it)
            } else {
                oldList.add(it)
            }
        }

        todayList.sortByDescending { it.timestamp }
        oldList.sortByDescending { it.timestamp }
        setList()
        Log.d(TAG, "sortListAndSet: ")
        binding.viewPager.visibility = View.VISIBLE
        binding.latestTv.visibility = View.VISIBLE
        binding.seeAllTv.visibility = View.VISIBLE
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        pagerAdapter.notifyDataSetChanged()
    }

    private fun setTabs(container: ViewGroup?) {
        val pagesList = resources.getStringArray(R.array.category_list)

        val count: Int = pagesList.size
        for (i in 0 until count) {
            binding.tablayout.addTab(binding.tablayout.newTab(), i)
            val tabView: View = ItemTabBinding.inflate(layoutInflater, container, false).root
            val textView = tabView.findViewById<TextView>(R.id.tab_title)
            val cardView = tabView.findViewById<CardView>(R.id.card_view)
            textView.text = pagesList[i]
            if (i == 0) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.primary))
                textView.setTextColor(Color.WHITE)
            }
            binding.tablayout.getTabAt(i)?.customView = tabView
        }
        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabView = tab.customView!!
                val cardView = tabView.findViewById<CardView>(R.id.card_view)
                val textView = tabView.findViewById<TextView>(R.id.tab_title)
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.primary))
                textView.setTextColor(Color.WHITE)
                currentCategory = textView.text.toString()
                currentPosition = tab.position
                setList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabView = tab.customView!!
                val cardView = tabView.findViewById<CardView>(R.id.card_view)
                val textView = tabView.findViewById<TextView>(R.id.tab_title)
                cardView.setCardBackgroundColor(Color.WHITE)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_black))
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }


    private fun setUpPager() {
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = 1

        binding.viewPager.setPageTransformer(CardTransformer(requireContext()))

        val itemDecoration = HorizontalMarginItemDecoration(requireContext(),
            R.dimen.viewpager_current_item_horizontal_margin)
        binding.viewPager.addItemDecoration(itemDecoration)

    }

    @SuppressLint("StringFormatInvalid")
    private fun instanceFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            if (token != null) {
                updateToken(token)
            }
        })
    }

    private fun updateToken(token: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token, FirebaseAuth.getInstance().currentUser!!.uid)
        reference.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(token1)
    }


    override fun onStop() {
        super.onStop()
        (activity as MainActivity).hideBottomNavBar()
    }
}