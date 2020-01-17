package com.flyppcorp.Helper

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Picasso

class PagerAdapterImage (val context : Context, val imagesUrl : ArrayList<String>) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return  imagesUrl.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        if (imagesUrl != null){
            Picasso.get()
                .load(imagesUrl[position])
                .fit()
                .centerCrop()
                .into(imageView)


        }
        container.addView(imageView)
        return  imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}