package com.example.peerstore

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.peerstore.databinding.ActivityChatBinding
import com.example.peerstore.databinding.ActivityExploreBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        binding.textExplore.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.textProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        db.collection("posts").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val postData: HashMap<String, *> = document.data as HashMap<String, *>
                    drawPost(postData, binding, document.id)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(this.toString(), "Error getting documents: ", exception)
            }
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun drawPost(postData: HashMap<String, *>, binding: ActivityChatBinding, postId: String) {
        val post = LayoutInflater.from(applicationContext).inflate(R.layout.post, binding.ChatPostsSpace, false)

        Picasso.get()
            .load(postData["image"] as String)
            .placeholder(R.drawable.baseline_image_not_supported_24)
            .rotate(90f)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(post.findViewById<CircleImageView>(R.id.PostImage))
        post.findViewById<TextView>(R.id.PostTitle).text = postData["title"].toString()
        post.findViewById<TextView>(R.id.PostTextRow1).text = "Owner: ${postData["owner"]}"
        post.findViewById<TextView>(R.id.PostId).text = postId
        post.findViewById<TextView>(R.id.PostTextRow2).visibility = android.view.View.GONE

        post.setOnClickListener {
            val intent = Intent(this, PostChatActivity::class.java)
            intent.putExtra("id", post.findViewById<TextView>(R.id.PostId).text as String)
            startActivity(intent)
        }
        binding.ChatPostsSpace.addView(post)
    }
}