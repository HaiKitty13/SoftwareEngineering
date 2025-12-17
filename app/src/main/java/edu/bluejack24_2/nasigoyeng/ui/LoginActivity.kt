package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.registerBtn.setOnClickListener {
            val intent = Intent(this, PreferenceActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.signupBtn.setOnClickListener {
            val intent = Intent(this, PreferenceActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.loginBtn.setOnClickListener {
            loginAction()
        }
    }

    private fun loginAction(){
        val email = binding.emailTb.text.toString()
        val password = binding.passwordTb.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length <= 8) {
            Toast.makeText(this, "Password must more than 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isAdmin { adminStatus ->
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("is_admin", adminStatus)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun isAdmin(callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return callback(false)

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val isAdmin = document.getBoolean("is_admin") ?: false
                callback(isAdmin)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}