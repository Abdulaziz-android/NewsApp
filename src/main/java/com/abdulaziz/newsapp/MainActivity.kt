package com.abdulaziz.newsapp

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.abdulaziz.newsapp.databinding.ActivityMainBinding
import com.abdulaziz.newsapp.fragments.FavoriteFragment
import com.abdulaziz.newsapp.fragments.HomeFragment
import com.abdulaziz.newsapp.fragments.ProfileFragment
import com.abdulaziz.newsapp.fragments.SignInFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sPref = getSharedPreferences("shared", MODE_PRIVATE)

        MobileAds.initialize(this) {}


        if (intent.getStringExtra("noti") != null) {
            setUpAds()
            val bundle = bundleOf(Pair("noti", "noti"))
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                HomeFragment::class.java, bundle).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                SignInFragment()).commit()
        }

        hideBottomNavBar()

        binding.bottomNavigation.setOnItemSelectedListener {
            val selected = when (it.itemId) {
                R.id.home_menu -> {
                    HomeFragment()
                }
                R.id.favorite_menu -> {
                    FavoriteFragment()
                }
                R.id.profile_menu -> {
                    ProfileFragment()
                }
                else -> {
                    HomeFragment()
                }
            }

            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                selected).commit()
            true
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setUpAds() {
        val timeString = sPref.getString("reward_ad_time", "00:00")
        val timeDate = SimpleDateFormat("HH:mm").parse(timeString)
        val realString = SimpleDateFormat("HH:mm").format(Date())
        val realDate = SimpleDateFormat("HH:mm").parse(realString)

        val different = realDate.time - timeDate.time
        val toMinutes = TimeUnit.MILLISECONDS.toMinutes(different)

        if (toMinutes > 15 || toMinutes < 0) {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(this,
                "ca-app-pub-3940256099942544/5224354917",
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {

                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        sPref.edit().putString("reward_ad_time", realString).apply()
                        rewardedAd.show(this@MainActivity
                        ) { rewardItem ->
                            /*var rewardAmount = rewardItem.amount
                            var rewardType = rewardItem.type*/
                        }
                    }
                })
        }
    }


    fun showBottomNavBar() {
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.lineIv.visibility = View.VISIBLE
    }

    fun hideBottomNavBar() {
        binding.bottomNavigation.visibility = View.GONE
        binding.lineIv.visibility = View.GONE
    }

}