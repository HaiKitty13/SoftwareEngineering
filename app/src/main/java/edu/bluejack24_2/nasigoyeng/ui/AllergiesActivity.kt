package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import edu.bluejack24_2.nasigoyeng.databinding.ActivityAllergiesBinding
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity

class AllergiesActivity : BaseActivity() {
    private lateinit var binding: ActivityAllergiesBinding
    private val selectedAllergies = mutableSetOf<String>()
    private lateinit var selectedCuisines: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllergiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedCuisines = intent.getStringArrayListExtra("SELECTED_CUISINES") ?: arrayListOf()

        if (selectedCuisines.isEmpty()) {
            Log.w("AllergiesActivity", "No cuisines received from PreferenceActivity")
        } else {
            Log.d("AllergiesActivity", "Received cuisines: $selectedCuisines")
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.itemBanana.setOnClickListener { toggleSelection(it, "Banana") }
        binding.itemMeat.setOnClickListener { toggleSelection(it, "Meat") }
        binding.itemKiwi.setOnClickListener { toggleSelection(it, "Kiwi") }
        binding.itemAlmonds.setOnClickListener { toggleSelection(it, "Almonds") }
        binding.itemMilk.setOnClickListener { toggleSelection(it, "Milk") }
        binding.itemEggs.setOnClickListener { toggleSelection(it, "Eggs") }
        binding.itemPeanuts.setOnClickListener { toggleSelection(it, "Peanuts") }
        binding.itemWheat.setOnClickListener { toggleSelection(it, "Wheat") }
        binding.itemShrimp.setOnClickListener { toggleSelection(it, "Shrimp") }
        binding.itemTreeNuts.setOnClickListener { toggleSelection(it, "Tree Nuts") }
        binding.itemShellfish.setOnClickListener { toggleSelection(it, "Shellfish") }
        binding.itemFish.setOnClickListener { toggleSelection(it, "Fish") }
        binding.itemSoy.setOnClickListener { toggleSelection(it, "Soy") }
        binding.itemSesame.setOnClickListener { toggleSelection(it, "Sesame") }
        binding.itemMustard.setOnClickListener { toggleSelection(it, "Mustard") }

        binding.buttonContinue.setOnClickListener {
            if (selectedAllergies.isEmpty()) {
                Toast.makeText(this, "Please select at least one allergy or choose 'None'", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, RegisterActivity::class.java)
            intent.putStringArrayListExtra("SELECTED_CUISINES", selectedCuisines)
            intent.putStringArrayListExtra("SELECTED_ALLERGIES", ArrayList(selectedAllergies))
            startActivity(intent)
            finish()
        }
    }

    private fun toggleSelection(view: View, allergyName: String) {
        if (selectedAllergies.contains(allergyName)) {
            selectedAllergies.remove(allergyName)
            view.isSelected = false
        } else {
            selectedAllergies.add(allergyName)
            view.isSelected = true
        }
    }
}