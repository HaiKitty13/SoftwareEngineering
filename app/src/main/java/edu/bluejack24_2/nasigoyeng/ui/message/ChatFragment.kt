package edu.bluejack24_2.nasigoyeng.ui.message

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.Message
import edu.bluejack24_2.nasigoyeng.databinding.FragmentChatBinding
import edu.bluejack24_2.nasigoyeng.ui.OtherProfileActivity
import edu.bluejack24_2.nasigoyeng.ui.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MessageAdapter
    private lateinit var currentUserId: String

    private val messages = mutableListOf<Message>()

    private lateinit var chatId: String
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://nasigoyeng-5c083-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    private lateinit var otherUserId: String
    private lateinit var otherUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            otherUserId = it.getString("userId") ?: ""
            otherUserName = it.getString("username") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = auth.currentUser?.uid ?: ""

        val chatParticipants = listOf(currentUserId, otherUserId).sorted()
        chatId = "chat_${chatParticipants[0]}_${chatParticipants[1]}"

        Log.d("ChatFragment", "Chat ID: $chatId")

        val chatRef = database.child("message").child(chatId)

        val usersMap = mapOf(
            "user1" to currentUserId,
            "user2" to otherUserId
        )
        chatRef.child("users").setValue(usersMap)

        binding.tvUsername.text = otherUserName

        setupRecyclerView()
        listenForMessages()

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            Log.d("ChatFragment", "Sending message: $text")
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.etMessage.setText("")
            }
        }

        binding.tvUsername.setOnClickListener {
            val intent = Intent(requireContext(), OtherProfileActivity::class.java)
            intent.putExtra("USER", otherUserId)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(messages, currentUserId)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter
    }

    private fun sendMessage(content: String) {
        val senderId = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()

        val messageRef = database.child("message").child(chatId).child("messages").push()
        val messageKey = messageRef.key ?: return

        Log.d("FirebaseChat", "Sending message with key: $messageKey")
        val message = Message(messageKey, senderId, content, timestamp)

        messageRef.setValue(message)
            .addOnSuccessListener {
                Log.d("FirebaseChat", "Message sent successfully with ID: $messageKey")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseChat", "Failed to send message: ${e.message}", e)
            }

        Log.d("FirebaseChat", "messageRef path: ${messageRef.key}")


        val lastMessage = mapOf(
            "content" to content,
            "sender" to senderId,
            "timestamp" to timestamp
        )
        database.child("message").child(chatId).child("last_message").setValue(lastMessage)
            .addOnSuccessListener {
                Log.d("FirebaseChat", "Last message updated")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseChat", "Failed to update last message: ${e.message}", e)
            }
    }

    private fun listenForMessages() {
        val usersRef = database.child("message").child(chatId).child("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user1 = snapshot.child("user1").getValue(String::class.java)
                val user2 = snapshot.child("user2").getValue(String::class.java)

                if (user1 == null || user2 == null) {
                    Log.e("ChatFragment", "User1 or User2 is null")
                    return
                }

                Log.d("ChatFragment", "user1: $user1, user2: $user2, currentUserId: $currentUserId")

                val messageRef = database.child("message").child(chatId).child("messages")
                messageRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messages.clear()
                        for (child in snapshot.children) {
                            val message = child.getValue(Message::class.java)
                            if (message != null) {
                                if (user1 == currentUserId || user2 == currentUserId) {
                                    messages.add(message)
                                }
                            }
                        }
                        adapter.notifyDataSetChanged()
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatFragment", "listenForMessages cancelled: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Failed to read users: ${error.message}")
            }
        })
    }

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USER_NAME = "userName"

        fun newInstance(userId: String, userName: String): edu.bluejack24_2.nasigoyeng.ui.message.ChatFragment {
            val fragment = edu.bluejack24_2.nasigoyeng.ui.message.ChatFragment()
            val args = Bundle()
            args.putString(edu.bluejack24_2.nasigoyeng.ui.message.ChatFragment.Companion.ARG_USER_ID, userId)
            args.putString(edu.bluejack24_2.nasigoyeng.ui.message.ChatFragment.Companion.ARG_USER_NAME, userName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}