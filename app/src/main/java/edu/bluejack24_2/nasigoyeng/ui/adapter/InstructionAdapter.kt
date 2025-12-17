package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.Step
import edu.bluejack24_2.nasigoyeng.databinding.ItemInstructionBinding

class InstructionAdapter(
    private val onTextChanged: (position: Int, text: String) -> Unit, // buat callback waktu user edit
    private val onDeleteClick: (Int) -> Unit // callback waktu pencet delete
) : RecyclerView.Adapter<InstructionAdapter.ViewHolder>() {

    private var instructions = mutableListOf<Step>()


    class ViewHolder(val binding: ItemInstructionBinding) : RecyclerView.ViewHolder(binding.root)


    // inflate layout XML jadi view dan bungkys dalam viewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInstructionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return instructions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]

        holder.binding.etInstruction.clearFocus()
        holder.binding.etInstruction.removeTextChangedListener(holder.binding.etInstruction.tag as? TextWatcher)

        val currentCursorPosition = holder.binding.etInstruction.selectionStart

        if (holder.binding.etInstruction.text.toString() != instruction.text) {
            holder.binding.etInstruction.setText(instruction.text)

            val newCursorPosition = if (currentCursorPosition <= instruction.text.length) {
                currentCursorPosition
            } else {
                instruction.text.length
            }

            holder.binding.etInstruction.setSelection(newCursorPosition)
        }
        // set default text
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < instructions.size) {
                    instructions[currentPosition].text = s.toString()
                    onTextChanged(currentPosition, s.toString())
                }
            }
        }

        holder.binding.etInstruction.addTextChangedListener(textWatcher)
        holder.binding.etInstruction.tag = textWatcher

        holder.binding.btnDeleteInstruction.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onDeleteClick(adapterPosition) // waktu tombol delete di klik kirim posisi ke callback onDeleteClick
            }
        }
    }

    fun updateInstructions(newInstructions: MutableList<Step>) {
        if (this.instructions.size == newInstructions.size) {
            for (i in newInstructions.indices) {
                if (this.instructions[i].text != newInstructions[i].text) {
                    this.instructions[i] = newInstructions[i]
                    notifyItemChanged(i)
                }
            }
        } else {
            this.instructions.clear()
            this.instructions.addAll(newInstructions)
            notifyDataSetChanged()
        }
    }
}