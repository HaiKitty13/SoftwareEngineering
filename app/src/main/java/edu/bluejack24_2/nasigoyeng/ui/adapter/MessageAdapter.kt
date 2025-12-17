package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.Message
import edu.bluejack24_2.nasigoyeng.databinding.ItemChatReceiveBinding
import edu.bluejack24_2.nasigoyeng.databinding.ItemChatSendBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SEND = 1
        private const val VIEW_TYPE_RECEIVE = 2
    }

    inner class SendMessageViewHolder(val binding: ItemChatSendBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceiveMessageViewHolder(val binding: ItemChatReceiveBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SEND
        } else {
            VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        Log.d("Message Adapter", "MessageAdapter initialized with ${messages.size} messages")
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEND -> {
                val binding = ItemChatSendBinding.inflate(inflater, parent, false)
                SendMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVE -> {
                val binding = ItemChatReceiveBinding.inflate(inflater, parent, false)
                ReceiveMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

        when (holder) {
            is SendMessageViewHolder -> {
                holder.binding.messageText.text = message.content
                holder.binding.messageTime.text = timeFormatted
            }
            is ReceiveMessageViewHolder -> {
                holder.binding.messageText.text = message.content
                holder.binding.messageTime.text = timeFormatted
            }
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

}