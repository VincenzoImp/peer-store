package com.example.peerstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.peerstore.databinding.ActivitySingInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SingInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySingInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        binding.SingInButton.setOnClickListener {
            if (checkAllField()) {
                val email: String = binding.SingInEmail.text.toString()
                val password: String = binding.SingInPassword.text.toString()
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Successfully sign in", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ExploreActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        Log.e("error: ", it.exception.toString())
                        Toast.makeText(this, it.exception.toString().substringAfter(":"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.SingInForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.SingInSingUp.setOnClickListener {
            val intent = Intent(this, SingUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkAllField(): Boolean {
        if (binding.SingInEmail.text.toString() == "") {
            binding.SingInEmail.error = "This is required field"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.SingInEmail.text.toString()).matches()) {
            binding.SingInEmail.error = "Check email format"
            return false
        }
        if (binding.SingInPassword.text.toString() == "") {
            binding.SingInPassword.error = "This is required field"
            return false
        }
        if (binding.SingInPassword.length() < 6) {
            binding.SingInPassword.error = "Password should be at least 6 characters long"
            return false
        }
        return true
    }
}