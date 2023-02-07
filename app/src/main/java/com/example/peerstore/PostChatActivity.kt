package com.example.peerstore

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.peerstore.databinding.ActivityPostChatBinding
import com.example.peerstore.databinding.ActivityPostInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityPostChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage
    @SuppressLint("SetTextI18n", "ResourceAsColor", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityPostChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val postId = intent.getStringExtra("id")
        if (postId != null) {
            auth = Firebase.auth
            db = Firebase.firestore
            user = auth.currentUser!!
            storage = Firebase.storage
            binding.textChat.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
                finish()
            }
            binding.textExplore.setOnClickListener {
                val intent = Intent(this, ExploreActivity::class.java)
                startActivity(intent)
                finish()
            }
            binding.backArrow.setOnClickListener {
                finish()
            }
            binding.textProfile.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            binding.button.setOnClickListener {
                if (checkAllField()) {
                    db.collection("posts").document(postId).get()
                        .addOnSuccessListener { it ->
                            if (it != null) {
                                val itData: HashMap<String, *> =
                                    it.data as HashMap<String, *>
                                val message = "${user.uid}_${binding.textInputEditText.text}"
                                (itData["globalChat"] as ArrayList<String>).add(message)
                                db.collection("posts").document(postId)
                                    .update("globalChat", itData["globalChat"])
                                    .addOnSuccessListener {
                                        val intent = Intent(this, PostChatActivity::class.java)
                                        intent.putExtra("id", postId)
                                        startActivity(intent)
                                        overridePendingTransition(0, 0)
                                        finish()
                                        overridePendingTransition(0, 0)
                                    }
                                    .addOnFailureListener(this) { e ->
                                        Log.e("error: ", e.toString())
                                        Toast.makeText(
                                            this,
                                            e.toString().substringAfter(":"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                }
            }

            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val postData: HashMap<String, *> = document.data as HashMap<String, *>
                        for (message in postData["globalChat"] as ArrayList<String>) {
                            drawMessage(message, binding)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(this.toString(), "get failed with ", exception)
                }
            binding.textView.text = binding.textView.text.toString() + " " + postId
        }
    }

    private fun drawMessage(message: String, binding: ActivityPostChatBinding) {
        val msg = LayoutInflater.from(applicationContext).inflate(R.layout.message,binding.MessageSpace, false)
        val from = message.substringBefore("_")
        val text = message.substringAfter("_")
        msg.findViewById<TextView>(R.id.messageFrom).text = from
        msg.findViewById<TextView>(R.id.messageText).text = text
        binding.MessageSpace.addView(msg)
    }

    private fun checkAllField(): Boolean {
        if (binding.textInputEditText.text.toString() == "") {
            binding.textInputEditText.error = "This is required field"
            return false
        }
        return true
    }
}