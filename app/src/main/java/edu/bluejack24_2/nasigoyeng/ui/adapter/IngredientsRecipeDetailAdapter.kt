package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.databinding.ItemIngrdientsDetailBinding

class IngredientsRecipeDetailAdapter(
    private val ingredients : List<String>
): RecyclerView.Adapter<IngredientsRecipeDetailAdapter.IngredientViewHolder>()  {

    inner class IngredientViewHolder(
        private val binding: ItemIngrdientsDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredient: String) {
            binding.tvIngredient.text = ingredient
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = ItemIngrdientsDetailBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return IngredientViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }


}