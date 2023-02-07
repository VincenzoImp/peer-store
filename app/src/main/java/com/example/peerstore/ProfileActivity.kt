package com.example.peerstore

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import com.example.peerstore.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("NAME_SHADOWING", "UNCHECKED_CAST")
class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        user = auth.currentUser!!
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
        binding.SingOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.floatingActionButtonProfile.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }
        binding.ProfileId.append(" " + user.uid)
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val postIds:List<String> = document.data?.get("posts") as List<String>
                    for (postId in postIds) {
                        db.collection("posts").document(postId).get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val postData:HashMap<String,*> = document.data as HashMap<String,*>
                                    drawPost(postData, binding, document.id)
                                } else {
                                    Log.d(this.toString(), "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(this.toString(), "get failed with ", exception)
                            }
                    }
                } else {
                    Log.d(this.toString(), "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(this.toString(), "get failed with ", exception)
            }

    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun drawPost(postData: HashMap<String, *>, binding: ActivityProfileBinding, postId: String) {
        val post = LayoutInflater.from(applicationContext).inflate(R.layout.post, binding.ProfilePostsSpace, false)
        Picasso.get()
            .load(postData["image"] as String)
            .placeholder(R.drawable.baseline_image_not_supported_24)
            .rotate(90f)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(post.findViewById<CircleImageView>(R.id.PostImage))
        post.findViewById<TextView>(R.id.PostTitle).text = postData["title"].toString()
        post.findViewById<TextView>(R.id.PostTextRow1).text = postData["price"].toString() + " $"
        post.findViewById<TextView>(R.id.PostId).text = postId
        post.setOnClickListener {
            val intent = Intent(this, PostInfoActivity::class.java)
            intent.putExtra("id", post.findViewById<TextView>(R.id.PostId).text as String)
            startActivity(intent)
        }
        binding.ProfilePostsSpace.addView(post)
    }
}
