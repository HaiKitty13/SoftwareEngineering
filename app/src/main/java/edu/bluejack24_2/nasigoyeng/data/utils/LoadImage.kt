package edu.bluejack24_2.nasigoyeng.data.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("profileImage")
fun loadImage(imageView: ImageView, url: String) {
    Glide.with(imageView).load(url).into(imageView)
}

@BindingAdapter("postImage")
fun loadImagePost(imageView: ImageView, url: String) {
    Glide.with(imageView).load(url).into(imageView)
}