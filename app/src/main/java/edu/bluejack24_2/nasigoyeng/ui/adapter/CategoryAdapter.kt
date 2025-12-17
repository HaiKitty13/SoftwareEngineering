package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.models.Category
import edu.bluejack24_2.nasigoyeng.databinding.ItemCategoryBinding
import com.bumptech.glide.Glide

class CategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (category: Category) -> Unit
):RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.textTitle.text = category.name.toString()

            Glide.with(binding.root.context)
                .load(category.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.imageCategory)

            binding.root.setOnClickListener {
                onItemClick(category)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

}