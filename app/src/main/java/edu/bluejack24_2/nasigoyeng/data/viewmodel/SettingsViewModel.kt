package edu.bluejack24_2.nasigoyeng.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.bluejack24_2.nasigoyeng.data.utils.NavigationEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsViewModel : ViewModel(){

    private val _isDarkTheme = MutableLiveData<Boolean>()
    val isDarkTheme: LiveData<Boolean> = _isDarkTheme

    private val _selectedLanguage = MutableLiveData<String>()

    private val _navigationEvent = MutableLiveData<NavigationEvent>()

    private val _deleteAccountResult = MutableLiveData<Result<Unit>>()

    private val _privacyUpdateResult = MutableLiveData<Result<Unit>>()
    val privacyUpdateResult: LiveData<Result<Unit>> = _privacyUpdateResult


    init {
        _isDarkTheme.value = false
        _selectedLanguage.value = "English"
    }

    fun onLogoutClicked() {
        _navigationEvent.value = NavigationEvent.LOGOUT
    }

    fun onDeleteAccountClicked() {
        _navigationEvent.value = NavigationEvent.DELETE_ACCOUNT
    }

    fun deleteAccount() {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            _deleteAccountResult.value = Result.failure(Exception("No logged in user"))
            return
        }

        val userId = currentUser.uid

        currentUser.delete()
            .addOnSuccessListener {
                firestore.collection("users").document(userId).delete()
                    .addOnSuccessListener {
                        _deleteAccountResult.value = Result.success(Unit)
                    }
                    .addOnFailureListener { e ->
                        _deleteAccountResult.value = Result.failure(e)
                    }
            }
            .addOnFailureListener { e ->
                _deleteAccountResult.value = Result.failure(e)
            }
    }

    fun onPrivacyChanged(isPrivate: Boolean) {
        Log.d("SettingsViewModel", "Privacy changed to: $isPrivate")
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        Log.d("SettingsViewModel", "User ID: $userId")

        if (userId == null) {
            _privacyUpdateResult.value = Result.failure(Exception("No user logged in"))
            Log.e("SettingsViewModel", "No user logged in")
            return
        }

        firestore.collection("users").document(userId)
            .update("_private", isPrivate)
            .addOnSuccessListener {
                _privacyUpdateResult.value = Result.success(Unit)

                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val updatedPrivacy = document.getBoolean("is_private")
                            Log.d("SettingsViewModel", "Updated is_private from Firestore: $updatedPrivacy")
                        } else {
                            Log.w("SettingsViewModel", "User document does not exist after update.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("SettingsViewModel", "Failed to fetch updated document: ${e.message}", e)
                    }

            }
            .addOnFailureListener { e ->
                _privacyUpdateResult.value = Result.failure(e)
            }
    }
}