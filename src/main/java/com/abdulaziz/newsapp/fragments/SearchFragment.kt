package com.abdulaziz.newsapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.abdulaziz.newsapp.MainActivity
import com.abdulaziz.newsapp.R
import com.abdulaziz.newsapp.adapter.NewsAdapter
import com.abdulaziz.newsapp.database.AppDatabase
import com.abdulaziz.newsapp.database.ChannelEntity
import com.abdulaziz.newsapp.databinding.FragmentSearchBinding
import com.abdulaziz.newsapp.databinding.ItemBottomsheetBinding
import com.abdulaziz.newsapp.databinding.ItemTabBinding
import com.abdulaziz.newsapp.models.News
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class SearchFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).hideBottomNavBar()
        list = arrayListOf()
        filterList = arrayListOf()
        adapter = NewsAdapter(filterList, object : NewsAdapter.OnItemClickListener {
            override fun onItemClick(news: News) {
                val bundle = bundleOf(Pair("news", news))
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    NewsDetailFragment::class.java, bundle).addToBackStack("Detail").commit()
            }
        })

        setUpFireStoreAndFetchData()

    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var list: ArrayList<News>
    private lateinit var filterList: ArrayList<News>
    private lateinit var adapter: NewsAdapter
    private var follow = false
    private var latest = false
    private var most = false
    private lateinit var channelList: ArrayList<ChannelEntity>
    private lateinit var database: AppDatabase
    private var category = "Healthy"
    private var currentPosition = 1
    private var searchedText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        channelList = arrayListOf()
        database = AppDatabase.getInstance(requireContext())

        channelList.addAll(database.newsDao().getAllChannels())

        binding.rv.adapter = adapter

        setTabs(container)

        setAdView()
        setUpEditText()
        setOnClicks()

        return binding.root
    }

    private fun setAdView() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun setOnClicks() {
        binding.apply {
            closeIv.setOnClickListener {
                if (searchEt.text.isNotEmpty()) {
                    searchEt.text.clear()
                } else {
                    (activity as MainActivity).onBackPressed()
                }
            }
        }
    }

    private fun setUpFireStoreAndFetchData() {
        val db = FirebaseDatabase.getInstance()

        db.getReference("news").get().addOnCompleteListener {
            if (it.isSuccessful) {
                list.clear()
                it.result.children.forEach { ds->
                    val news = ds.getValue(News::class.java)
                    news?.let { it1 ->
                        list.add(it1)
                    }
                }
                sortListByCategory()
            }
        }

    }

    private fun setUpEditText() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.searchEt.text.toString().isEmpty()) {
                    if (category != "Filter") sortListByCategory()
                    else filterList()

                    binding.resultCountTv.text = "About ${filterList.size} results "
                    binding.searchedTextTv.text = ""
                }
                searchedText = binding.searchEt.text.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        binding.searchEt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            @SuppressLint("SetTextI18n")
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_SEARCH) {
                    searchedText = binding.searchEt.text.toString()
                    if (binding.searchEt.text.toString().isNotEmpty()) {
                        if (category != "Filter") {
                            sortListWithText()
                        } else {
                            filterList()
                        }
                    }
                    binding.resultCountTv.text = "About ${filterList.size} results "
                    binding.searchedTextTv.text = searchedText
                    view?.hideKeyboard()
                    return true
                }
                return false
            }

        })

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun sortListWithText() {
        filterList.clear()
       if (category != "Filter") {
            list.forEach {
                if (it.category == category && it.title!!.lowercase()
                        .contains(searchedText.lowercase())
                ) {
                    filterList.add(it)
                }
            }
        } else {
            list.forEach {
                if (it.title!!.lowercase()
                        .contains(binding.searchEt.text.toString().lowercase())
                ) {
                    filterList.add(it)
                }
            }
        }

        binding.resultCountTv.text = "About ${filterList.size} results "
        binding.searchedTextTv.text = searchedText

        adapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun sortListByCategory() {
        filterList.clear()
        list.forEach {
            if (it.category == category) {
                filterList.add(it)
            }
        }

        binding.resultCountTv.text = "About ${filterList.size} results "
        binding.searchedTextTv.text = searchedText

        adapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun filterSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = ItemBottomsheetBinding.inflate(layoutInflater)

        view.latestChb.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                view.mostChb.isChecked = false
            }
        }
        view.mostChb.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                view.latestChb.isChecked = false
            }
        }

        view.followChb.isChecked = follow
        view.latestChb.isChecked = latest
        view.mostChb.isChecked = most

        view.resetTv.setOnClickListener {
            view.followChb.isChecked = false
            view.latestChb.isChecked = false
            view.mostChb.isChecked = false
        }
        if (channelList.isEmpty()) {
            view.followChb.setOnClickListener {
                view.followChb.isChecked = false
                Toast.makeText(requireContext(),
                    "you do not follow any channel!",
                    Toast.LENGTH_SHORT).show()
            }
        }

        view.saveCard.setOnClickListener {

            follow = view.followChb.isChecked
            latest = view.latestChb.isChecked
            most = view.mostChb.isChecked

            if (view.followChb.isChecked || view.latestChb.isChecked || view.mostChb.isChecked) {
                binding.tablayout.selectTab(binding.tablayout.getTabAt(0))

                filterList()
            } else {
                sortListWithText()
            }

            binding.resultCountTv.text = "About ${filterList.size} results "
            binding.searchedTextTv.text = searchedText

            dialog.dismiss()
        }

        dialog.setContentView(view.root)
        dialog.show()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun filterList() {
        filterList.clear()

        if (follow) {
            list.forEach { news ->
                channelList.forEach { channel ->
                    if (news.author == channel.name)
                        filterList.add(news)
                }
            }
            if (searchedText.isNotEmpty()) {
                filterList.forEach {
                    if (!it.title?.lowercase()!!
                            .contains(searchedText.lowercase())
                    ) {
                        filterList.remove(it)
                    }
                }
            }
        } else {
            if (binding.searchEt.text.toString().isNotEmpty()) {
                list.forEach {
                    if (it.title?.lowercase()!!
                            .contains(searchedText.lowercase())
                    ) {
                        filterList.add(it)
                    }
                }
            } else {
                filterList.addAll(list)
            }
        }

        if (binding.searchEt.text.toString().isNotEmpty()) {
            val iterator = filterList.iterator()
            while (iterator.hasNext()) {
                val news = iterator.next()
                if (!news.title?.lowercase()!!
                        .contains(searchedText.lowercase())
                ) {
                    iterator.remove()
                }
            }
        }

        if (latest) {
            filterList.sortBy { it.timestamp }
        }

        if (most) {
            filterList.sortBy { it.view_count }
        }

        binding.resultCountTv.text = "About ${filterList.size} results "
        binding.searchedTextTv.text = binding.searchEt.text.toString()


        filterList.reverse()

        adapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun setTabs(container: ViewGroup?) {
        val pagesList = resources.getStringArray(R.array.category_list)
        val myPages = arrayListOf<String>()
        myPages.add("Filter")
        pagesList.forEach {
            myPages.add(it)
        }
        val count: Int = myPages.size
        for (i in 0 until count) {
            binding.tablayout.addTab(binding.tablayout.newTab(), i)
            val tabView: View = ItemTabBinding.inflate(layoutInflater, container, false).root
            val textView = tabView.findViewById<TextView>(R.id.tab_title)
            val cardView = tabView.findViewById<CardView>(R.id.card_view)
            textView.text = myPages[i]
            if (i == 0) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.primary))
                textView.setTextColor(Color.WHITE)
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_filter_white,
                    0,
                    0,
                    0)
                textView.compoundDrawablePadding = 8
                cardView.setOnClickListener {
                    filterSheet()
                }
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
                if (tab.position == 0) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_filter_white,
                        0,
                        0,
                        0)
                    textView.compoundDrawablePadding = 8
                    cardView.setOnClickListener {
                        filterSheet()
                    }
                }
                category = textView.text.toString()
                if (category != "Filter") {
                    if (searchedText.isEmpty()) {
                        sortListByCategory()
                    } else {
                        sortListWithText()
                    }
                } else {
                    filterList()
                }


                currentPosition = tab.position
                searchedText = binding.searchEt.text.toString()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabView = tab.customView!!
                val cardView = tabView.findViewById<CardView>(R.id.card_view)
                val textView = tabView.findViewById<TextView>(R.id.tab_title)
                cardView.setCardBackgroundColor(Color.WHITE)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_black))
                if (tab.position == 0) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_filter, 0, 0, 0)
                    textView.compoundDrawablePadding = 8
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        if (currentPosition != 0) {
            binding.tablayout.selectTab(binding.tablayout.getTabAt(currentPosition))
        } else {
            binding.resultCountTv.text = "About ${filterList.size} results "
            binding.searchedTextTv.text = searchedText
        }
    }


    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}