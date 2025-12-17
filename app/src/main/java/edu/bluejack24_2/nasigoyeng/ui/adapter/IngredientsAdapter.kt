package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.Ingredient
import edu.bluejack24_2.nasigoyeng.databinding.ItemIngredientBinding

class IngredientsAdapter (
    private val onTextChanged: (position: Int, text: String) -> Unit,
    private val onDeleteClick: (Int) -> Unit
): RecyclerView.Adapter<IngredientsAdapter.ViewHolder>(){

    private var ingredients = mutableListOf<Ingredient>()

    class ViewHolder(val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        Log.d("Adapter", "getItemCount: ${ingredients.size}")
        return ingredients.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]

        holder.binding.etIngredient.clearFocus()
        holder.binding.etIngredient.removeTextChangedListener(holder.binding.etIngredient.tag as? TextWatcher)

        Log.d("Adapter", "onBindViewHolder pos=$position, text=${ingredient.text}")

//        val exixtingWatcher = holder.binding
//            .etIngredient.tag as? TextWatcher
//
//        if(exixtingWatcher != null){
//            holder.binding.etIngredient.removeTextChangedListener(exixtingWatcher)
//        }

        val currentCursorPosition = holder.binding.etIngredient.selectionStart

        if (holder.binding.etIngredient.text.toString() != ingredient.text) {
            holder.binding.etIngredient.setText(ingredient.text)

            // RESTORE cursor position
            val newCursorPosition = if (currentCursorPosition <= ingredient.text.length) {
                currentCursorPosition
            } else {
                ingredient.text.length
            }

            holder.binding.etIngredient.setSelection(newCursorPosition)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < ingredients.size) {
                    ingredients[currentPosition].text = s.toString()
                    onTextChanged(currentPosition, s.toString())
                }
            }
        }

        holder.binding.etIngredient.addTextChangedListener(textWatcher)
        holder.binding.etIngredient.tag = textWatcher

        holder.binding.btnDeleteIngredient.setOnClickListener{
            val adapterPosition = holder.bindingAdapterPosition
            if(adapterPosition != RecyclerView.NO_POSITION){
                onDeleteClick(adapterPosition)
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.etIngredient.removeTextChangedListener(holder.binding.etIngredient.tag as? TextWatcher)
        holder.binding.etIngredient.tag = null
        holder.binding.etIngredient.clearFocus()
        Log.d("Adapter", "ViewHolder recycled")
    }

    fun updateIngredient(newIngredient: MutableList<Ingredient>){
        Log.d("Adapter", "updateIngredient called: ${newIngredient.size}")

        if (this.ingredients.size == newIngredient.size) {
            for (i in newIngredient.indices) {
                if (this.ingredients[i].text != newIngredient[i].text) {
                    this.ingredients[i] = newIngredient[i]
                    notifyItemChanged(i)
                }
            }
        } else {
            this.ingredients.clear()
            this.ingredients.addAll(newIngredient)
            notifyDataSetChanged()
        }
    }


}