package com.example.peerstore

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
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

class PostInfoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityPostInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage
    @SuppressLint("SetTextI18n", "ResourceAsColor", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityPostInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        val postId = intent.getStringExtra("id")
        if (postId != null) {
            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val postData: HashMap<String, *> = document.data as HashMap<String, *>
                        if (postData["owner"] as String != user.uid) {
                            binding.PostChatButton.visibility = android.view.View.VISIBLE
                            binding.PostChatButton.setOnClickListener {
                                val intent = Intent(this, PostChatActivity::class.java)
                                intent.putExtra("id", postId)
                                startActivity(intent)
                            }
                        } else {
                            binding.textProfile.setTextColor(R.color.purple_700)
                            binding.textExplore.setTextColor(R.color.gray_600)
                            binding.textProfile.setCompoundDrawablesWithIntrinsicBounds(null, this.resources.getDrawable(R.drawable.baseline_account_circle_24_p), null, null)
                            binding.textExplore.setCompoundDrawablesWithIntrinsicBounds(null, this.resources.getDrawable(R.drawable.baseline_search_24), null, null)
                            binding.PostDeleteButton.visibility = android.view.View.VISIBLE
                            binding.PostDeleteButton.setOnClickListener {
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener {
                                        if (it != null) {
                                            val itData: HashMap<String, *> =
                                                it.data as HashMap<String, *>
                                            (itData["posts"] as ArrayList<String>).remove(postId)
                                            db.collection("users").document(user.uid)
                                                .update("posts", itData["posts"])
                                                .addOnSuccessListener {
                                                    db.collection("posts").document(postId).delete()
                                                        .addOnSuccessListener {
                                                            storage.reference.child(postData["imageName"].toString()).delete()
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                        this,
                                                                        "Post deleted",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    val intent = Intent(this, ProfileActivity::class.java)
                                                                    startActivity(intent)
                                                                    finish()
                                                                }
                                                                .addOnFailureListener { exception ->
                                                                    Log.e(
                                                                        this.toString(),
                                                                        "get failed with ",
                                                                        exception
                                                                    )
                                                                }
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            Log.e(
                                                                this.toString(),
                                                                "get failed with ",
                                                                exception
                                                            )
                                                        }
                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e(this.toString(), "get failed with ", exception)
                                                }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(this.toString(), "get failed with ", exception)
                                    }
                            }
                        }
                        binding.PostImage.rotation = 90f
                        Picasso.get()
                            .load(postData["image"] as String)
                            .placeholder(R.drawable.baseline_image_not_supported_24)
                            .error(R.drawable.baseline_image_not_supported_24)
                            .into(binding.PostImage)
                        binding.PostTitle.text = postData["title"] as String
                        binding.PostDescriptionAI.text = postData["descriptionAI"] as String
                        binding.PostDescriptionOwner.text =
                            postData["descriptionOwner"] as String
                        binding.textPostPrice.text =
                            binding.textPostPrice.text.toString() + " " + postData["price"].toString() + " $"
                        binding.textPostPosition.text =
                            binding.textPostPosition.text.toString() + " " + postData["latitude"].toString() + ", " + postData["longitude"].toString()
                        binding.textPostOwner.text =
                            binding.textPostOwner.text.toString() + " " + postData["owner"] as String
                        binding.textView.text = binding.textView.text.toString() + " " + postId
                    } else {
                        Log.d(this.toString(), "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(this.toString(), "get failed with ", exception)
                }
        }
    }
}