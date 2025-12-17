package edu.bluejack24_2.nasigoyeng.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.models.User
import edu.bluejack24_2.nasigoyeng.databinding.ProfileItemBinding
import com.bumptech.glide.Glide

class ProfileUserAdapter(
    private val profiles: List<User>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProfileUserAdapter.ProfileUserViewHolder>() {

    inner class ProfileUserViewHolder(
        private val binding: ProfileItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: User, onItemClick: (String) -> Unit) {
            binding.user = profile
            binding.button.isVisible = false
            binding.executePendingBindings()

            Glide.with(binding.root.context)
                .load(profile.profile_picture)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.profilePicture)

            binding.root.setOnClickListener {
                onItemClick(profile.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileUserViewHolder {
        val view = ProfileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileUserViewHolder, position: Int) {
        holder.bind(profiles[position], onItemClick)
    }

    override fun getItemCount(): Int = profiles.size
}