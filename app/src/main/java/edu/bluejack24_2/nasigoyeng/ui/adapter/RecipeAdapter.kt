package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.RecipeItemBinding

class RecipeAdapter (private val recipes: List<Recipe>): RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>()  {
    class RecipeViewHolder(private val binding: RecipeItemBinding): RecyclerView.ViewHolder(binding.root){
        fun binding (recipe: Recipe) {
            binding.recipe = recipe
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val profile = recipes[position]
        holder.binding(profile)
    }

}