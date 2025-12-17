package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.ItemRecomendationBinding
import com.bumptech.glide.Glide
import edu.bluejack24_2.nasigoyeng.R

class RecommendationAdapter(
    private var recommendations: List<Recipe>,
    private val currentUserId: String,
    private val listener: OnRecipeActionListener
):RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    inner class RecommendationViewHolder(
        private val binding: ItemRecomendationBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            binding.textTitle.text = recipe.title
            binding.textDescription.text = recipe.description
            binding.textTime.text = recipe.time.toString()

            val isAdmin = recipe.is_official ?: false

            binding.ivOfficialIcon.visibility = if (isAdmin) View.VISIBLE else View.GONE

            Glide.with(binding.root.context)
                .load(recipe.image_url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.imageFood)

            val isLiked = recipe.liked_by.contains(currentUserId)
            binding.imageFavorite.setBackgroundResource(
                if (isLiked) R.drawable.circle_pink_background
                else R.drawable.circle_unclick_background
            )

            val isBookmarked = recipe.bookmark_by.contains(currentUserId)
            binding.imageBookmark.setBackgroundResource(
                if (isBookmarked) R.drawable.circle_pink_background
                else R.drawable.circle_unclick_background
            )

            binding.imageFavorite.setOnClickListener {
                listener.onLikeClicked(recipe)
            }

            binding.imageBookmark.setOnClickListener {
                listener.onBookmarkClicked(recipe)
            }

            binding.root.setOnClickListener {
                listener.onRecipeClicked(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemRecomendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecommendationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recommendations.size
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    fun updateList(newList: List<Recipe>) {
        recommendations = newList
        notifyDataSetChanged()
    }


}