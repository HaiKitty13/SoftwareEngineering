package edu.bluejack24_2.nasigoyeng.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.databinding.FragmentExploreBinding
import edu.bluejack24_2.nasigoyeng.ui.AddPostActivity
import com.google.android.material.tabs.TabLayout

class ExploreFragment : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var binding : FragmentExploreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = binding.tabLayout

        switchFragment(ForYouFragment())

        tabLayout.addTab(tabLayout.newTab().setText("For You"))
        tabLayout.addTab(tabLayout.newTab().setText("Following"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> switchFragment(ForYouFragment())
                    1 -> switchFragment(FollowingPostFragment())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.searchButton.setOnClickListener {
            val intent = Intent(requireContext(), AddPostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun switchFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}