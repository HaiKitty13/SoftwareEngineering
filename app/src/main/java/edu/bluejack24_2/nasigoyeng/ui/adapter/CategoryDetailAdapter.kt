package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.ItemRecipeCategoryBinding
import com.bumptech.glide.Glide

class CategoryDetailAdapter(
    private val recipes: List<Recipe>,
    private val currentUserId: String,
    private val listener: OnRecipeActionListener
) : RecyclerView.Adapter<CategoryDetailAdapter.CategoryDetailViewHolder>() {

    inner class CategoryDetailViewHolder(private val binding: ItemRecipeCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailViewHolder {
        val view = ItemRecipeCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryDetailViewHolder(view)
    }

    override fun getItemCount(): Int = recipes.size

    override fun onBindViewHolder(holder: CategoryDetailViewHolder, position: Int) {
        holder.bind(recipes[position])
    }
}
