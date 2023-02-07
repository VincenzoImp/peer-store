package com.example.peerstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.peerstore.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        binding.ForgotPasswordButton.setOnClickListener {
            if (checkAllField()) {
                val email = binding.ForgotPasswordEmail.text.toString()
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Email sent!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, SingInActivity::class.java)
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
        binding.ForgotPasswordSingIn.setOnClickListener {
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun checkAllField(): Boolean {
        if (binding.ForgotPasswordEmail.text.toString() == "") {
            binding.ForgotPasswordEmail.error = "This is required field"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.ForgotPasswordEmail.text.toString()).matches()) {
            binding.ForgotPasswordEmail.error = "Check email format"
            return false
        }
        return true
    }
}