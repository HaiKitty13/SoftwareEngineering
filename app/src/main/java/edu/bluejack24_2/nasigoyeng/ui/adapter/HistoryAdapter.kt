package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.ItemHistoryBinding
import com.bumptech.glide.Glide

class HistoryAdapter(
    private var history: List<Recipe>,
    private val listener: OnRecipeActionListener
): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            binding.textTitle.text = recipe.title
            binding.textDescription.text = recipe.description
            binding.textTime.text = recipe.time.toString()

            Glide.with(binding.root.context)
                .load(recipe.image_url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.imageFood)

            binding.root.setOnClickListener {
                listener.onRecipeClicked(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(history[position])
    }

    fun updateList(newList: List<Recipe>) {
        history = newList
        notifyDataSetChanged()
    }


}