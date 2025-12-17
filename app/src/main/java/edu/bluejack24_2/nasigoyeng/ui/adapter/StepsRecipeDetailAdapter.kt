package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.databinding.ItemStepDetailBinding

class StepsRecipeDetailAdapter(
    private val steps : List<String>
): RecyclerView.Adapter<StepsRecipeDetailAdapter.StepViewHolder>() {

    inner class StepViewHolder(
        private val binding: ItemStepDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(step: String) {
            binding.tvStepDescription.text = step
            binding.tvStepNumber.text = (adapterPosition + 1).toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = ItemStepDetailBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return StepViewHolder(view)
    }

    override fun getItemCount(): Int {
       return steps.size
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(steps[position])
    }
}