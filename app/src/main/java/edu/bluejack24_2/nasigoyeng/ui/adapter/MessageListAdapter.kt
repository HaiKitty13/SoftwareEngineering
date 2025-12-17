package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.models.MessageList
import edu.bluejack24_2.nasigoyeng.databinding.ItemMessageListBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class MessageListAdapter(
    private val messagesList: List<MessageList>,
    private val onItemClick: (userId: String, username: String) -> Unit
) :
    RecyclerView.Adapter<MessageListAdapter.MessageListViewHolder>() {

    inner class MessageListViewHolder(val binding: ItemMessageListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: MessageList) {
            binding.tvName.text = message.name
            binding.tvMessage.text = if (message.message.isNotBlank()) message.message else ""
            binding.tvTime.text = formatTimestamp(message.timestamp)

            Glide.with(binding.root.context)
                .load(message.avatarUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.ivAvatar)

            binding.root.setOnClickListener {
                onItemClick(message.userId, message.name)
            }

        }

        private fun formatTimestamp(timestamp: Long): String {
            if (timestamp == 0L) return ""
            val date = Date(timestamp)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
        val binding = ItemMessageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
        holder.bind(messagesList[position])
    }

    override fun getItemCount(): Int = messagesList.size
}

interface OnMessageClickListener {
    fun onMessageClick(userId: String, username: String)
}

