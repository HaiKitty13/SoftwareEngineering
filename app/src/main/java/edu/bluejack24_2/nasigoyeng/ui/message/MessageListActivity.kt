package edu.bluejack24_2.nasigoyeng.ui.message

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.databinding.ActivityMessageListBinding
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity

class MessageListActivity : BaseActivity() {

    private lateinit var binding: ActivityMessageListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
    }
}