package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import edu.bluejack24_2.nasigoyeng.databinding.ActivityPreferenceBinding
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity

class PreferenceActivity : BaseActivity() {
    private lateinit var binding: ActivityPreferenceBinding
    private val selectedCuisines = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.itemSalad.setOnClickListener { toggleSelection(it, "Salad") }
        binding.itemSoup.setOnClickListener { toggleSelection(it, "Soup") }
        binding.itemEggs.setOnClickListener { toggleSelection(it, "Eggs") }
        binding.itemSeafood.setOnClickListener { toggleSelection(it, "Seafood") }
        binding.itemChicken.setOnClickListener { toggleSelection(it, "Chicken") }
        binding.itemMeat.setOnClickListener { toggleSelection(it, "Meat") }
        binding.itemBurger.setOnClickListener { toggleSelection(it, "Burger") }
        binding.itemPizza.setOnClickListener { toggleSelection(it, "Pizza") }
        binding.itemSushi.setOnClickListener { toggleSelection(it, "Sushi") }
        binding.itemRice.setOnClickListener { toggleSelection(it, "Rice") }
        binding.itemDessert.setOnClickListener { toggleSelection(it, "Dessert") }
        binding.itemBread.setOnClickListener { toggleSelection(it, "Bread") }

        binding.btnContinue.setOnClickListener {
            if (selectedCuisines.isEmpty()) {
                Toast.makeText(this, "Please select at least one cuisine preference", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, AllergiesActivity::class.java)
            intent.putStringArrayListExtra("SELECTED_CUISINES", ArrayList(selectedCuisines))
            startActivity(intent)
            finish()
        }

    }

    private fun toggleSelection(view: View, cuisineName: String) {
        if (selectedCuisines.contains(cuisineName)) {
            selectedCuisines.remove(cuisineName)
            view.isSelected = false
        } else {
            selectedCuisines.add(cuisineName)
            view.isSelected = true
        }
    }
}