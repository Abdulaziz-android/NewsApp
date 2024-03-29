package com.abdulaziz.newsapp.transformer

import android.content.Context
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.abdulaziz.newsapp.R

class CardTransformer(context: Context): ViewPager2.PageTransformer {
    private val nextItemVisiblePx = context.resources.getDimension(R.dimen.viewpager_next_item_visible)
    private val currentItemHorizontalMarginPx = context.resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
    private val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
    override fun transformPage(page: View, position: Float) {

        page.translationX = -pageTranslationX * position
        // Next line scales the item's height. You can remove it if you don't want this effect
        page.scaleY = 1 - (0.12f * kotlin.math.abs(position))
        // If you want a fading effect uncomment the next line:
        page.alpha = 0.25f + (1 - kotlin.math.abs(position))
    }

}