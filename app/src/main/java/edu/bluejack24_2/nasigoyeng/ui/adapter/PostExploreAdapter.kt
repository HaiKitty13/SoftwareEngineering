package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.PostUser
import edu.bluejack24_2.nasigoyeng.databinding.PostExploreItemBinding

class PostExploreAdapter (private val posts: List<PostUser>): RecyclerView.Adapter<PostExploreAdapter.PostExploreViewHolder>() {
    class PostExploreViewHolder(private val binding: PostExploreItemBinding): RecyclerView.ViewHolder(binding.root){
        fun binding (post: PostUser) {
            binding.posts = post
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostExploreViewHolder {
        val view = PostExploreItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PostExploreViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostExploreViewHolder, position: Int) {
        val post = posts[position]
        holder.binding(post)
    }

}