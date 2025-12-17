package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.Post
import edu.bluejack24_2.nasigoyeng.databinding.PostItemBinding

class PostAdapter (private val posts: List<Post>): RecyclerView.Adapter<PostAdapter.PostViewHolder>()  {
    class PostViewHolder(private val binding: PostItemBinding): RecyclerView.ViewHolder(binding.root){
        fun binding (post: Post) {
            binding.posts = post
            binding.userLayout.isVisible = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.binding(post)
    }
}