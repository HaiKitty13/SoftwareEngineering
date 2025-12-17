package edu.bluejack24_2.nasigoyeng.ui.message

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.MessageList
import edu.bluejack24_2.nasigoyeng.databinding.FragmentMessageListBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.MessageListAdapter
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.atomic.AtomicInteger
import edu.bluejack24_2.nasigoyeng.R
import com.google.firebase.auth.FirebaseAuth

class MessageListFragment : Fragment() {

    private var _binding: FragmentMessageListBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore

    private val messageList = mutableListOf<MessageList>()
    private lateinit var adapter: MessageListAdapter

    private val messageListeners = mutableMapOf<String, ValueEventListener>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchMessages()
    }

    private fun setupRecyclerView() {
        adapter = MessageListAdapter(messageList) { userId, username ->
            openChatFragment(userId, username)
        }
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMessages.adapter = adapter
    }

    private fun openChatFragment(userId: String, username: String) {
        val bundle = Bundle().apply {
            putString("userId", userId)
            putString("username", username)
        }

        val chatFragment = edu.bluejack24_2.nasigoyeng.ui.message.ChatFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, chatFragment)
            .addToBackStack(null)
            .commit()
    }



    private fun fetchMessages() {
        showLoading(true)

        firestore.collection("users").get()
            .addOnSuccessListener { documents ->
                messageList.clear()

                if (documents.isEmpty) {
                    showLoading(false)
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                val totalUsers = documents.size()
                val completedRequests = AtomicInteger(0)

                for (document in documents) {
                    val userId = document.id
                    val name = document.getString("username") ?: "Unknown"
                    val profilePicture = document.getString("profile_picture") ?: ""

                    val emptyMsg = MessageList(
                        userId = userId,
                        name = name,
                        message = "",
                        timestamp = 0L,
                        id = userId,
                        avatarUrl = profilePicture,
                        isOnline = false
                    )

                    if(userId != FirebaseAuth.getInstance().currentUser?.uid) {
                        messageList.add(emptyMsg)
                    }


                    // Real-time listener
                    val messageListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                val message = snapshot.child("message").getValue(String::class.java) ?: ""
                                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                                val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false

                                val index = messageList.indexOfFirst { it.id == userId }
                                if (index != -1) {
                                    messageList[index] = messageList[index].copy(
                                        message = message,
                                        timestamp = timestamp,
                                        isOnline = isOnline
                                    )
                                }

                                messageList.sortByDescending { it.timestamp }

                                if (isAdded && !isDetached) {
                                    adapter.notifyDataSetChanged()
                                }

                            } catch (e: Exception) {
                                Log.e("MessageListFragment", "Error processing data", e)
                            } finally {
                                val done = completedRequests.incrementAndGet()
                                Log.d("MessageListFragment", "Loaded $done of $totalUsers")
                                if (done == totalUsers) {
                                    showLoading(false)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MessageListFragment", "Database error: ${error.message}")
                            val done = completedRequests.incrementAndGet()
                            if (done == totalUsers) {
                                showLoading(false)
                            }
                        }
                    }

                    messageListeners[userId] = messageListener
                    database.child("messageList").child(userId).addValueEventListener(messageListener)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("MessageListFragment", "Failed to fetch users", e)
                showLoading(false)
                showError("Failed to load messages")
            }
    }

    private fun showLoading(show: Boolean) {

    }

    private fun showError(message: String) {
        Log.e("MessageListFragment", message)
    }

    private fun removeAllListeners() {
        messageListeners.forEach { (userId, listener) ->
            database.child("messageList").child(userId).removeEventListener(listener)
        }
        messageListeners.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeAllListeners()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        removeAllListeners()
    }
}
