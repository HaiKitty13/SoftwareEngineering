package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.data.models.User
import edu.bluejack24_2.nasigoyeng.databinding.ProfileItemBinding

class ProfileUserFollowersAdapter(
    private val profiles: List<User>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProfileUserFollowersAdapter.ProfileUserFollowersViewHolder>() {

    inner class ProfileUserFollowersViewHolder(
        private val binding: ProfileItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: User, onItemClick: (String) -> Unit) {
            binding.user = profile
            binding.button.text = "Delete"
            binding.executePendingBindings()

            binding.button.setOnClickListener {
                onItemClick(profile.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileUserFollowersViewHolder {
        val view = ProfileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileUserFollowersViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileUserFollowersViewHolder, position: Int) {
        holder.bind(profiles[position], onItemClick)
    }

    override fun getItemCount(): Int = profiles.size
}
