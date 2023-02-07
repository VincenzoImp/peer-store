package com.example.peerstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.peerstore.databinding.ActivitySingUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SingUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySingUpBinding
    private lateinit var user: FirebaseUser
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        binding.SingUpButton.setOnClickListener {
            if (checkAllField()) {
                val email: String = binding.SingUpEmail.text.toString()
                val password: String = binding.SingUpPassword1.text.toString()
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        user = auth.currentUser!!
                        val userData = hashMapOf(
                            "posts" to listOf<String>()
                        )
                        db.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener(this) {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, SingInActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener(this) { e ->
                                Log.e("error: ", e.toString())
                                Toast.makeText(this, e.toString().substringAfter(":"), Toast.LENGTH_SHORT).show()
                            }
                    }
                    else {
                        Log.e("error: ", it.exception.toString())
                        Toast.makeText(this, it.exception.toString().substringAfter(":"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.SingUpSingIn.setOnClickListener {
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkAllField(): Boolean {
        if (binding.SingUpEmail.text.toString() == "") {
            binding.SingUpEmail.error = "This is required field"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.SingUpEmail.text.toString()).matches()) {
            binding.SingUpEmail.error = "Check email format"
            return false
        }
        if (binding.SingUpPassword1.text.toString() == "") {
            binding.SingUpPassword1.error = "This is required field"
            return false
        }
        if (binding.SingUpPassword2.text.toString() == "") {
            binding.SingUpPassword2.error = "This is required field"
            return false
        }
        if (binding.SingUpPassword1.length() < 6) {
            binding.SingUpPassword1.error = "Password should be at least 6 characters long"
            return false
        }
        if (binding.SingUpPassword1.text.toString() != binding.SingUpPassword2.text.toString()) {
            binding.SingUpPassword2.error = "Password do not match"
            return false
        }
        return true
    }
}