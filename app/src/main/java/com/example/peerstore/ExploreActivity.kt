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

class ExploreActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityExploreBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        user = auth.currentUser!!
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        binding.textChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
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
                    if (postData["owner"] != user.uid) {
                        drawPost(postData, binding, document.id)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(this.toString(), "Error getting documents: ", exception)
            }
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun drawPost(postData: HashMap<String, *>, binding: ActivityExploreBinding, postId: String) {
        val post = LayoutInflater.from(applicationContext).inflate(R.layout.post, binding.ExplorePostsSpace, false)
        val distance = getDistance(postData["latitude"] as Number, postData["longitude"] as Number)

        Picasso.get()
            .load(postData["image"] as String)
            .placeholder(R.drawable.baseline_image_not_supported_24)
            .rotate(90f)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(post.findViewById<CircleImageView>(R.id.PostImage))
        post.findViewById<TextView>(R.id.PostTitle).text = postData["title"].toString()
        post.findViewById<TextView>(R.id.PostTextRow1).text = postData["price"].toString() + " $"
        post.findViewById<TextView>(R.id.PostTextRow2).text = "$distance km"
        post.findViewById<TextView>(R.id.PostId).text = postId

        post.setOnClickListener {
            val intent = Intent(this, PostInfoActivity::class.java)
            intent.putExtra("id", post.findViewById<TextView>(R.id.PostId).text as String)
            startActivity(intent)
        }
        binding.ExplorePostsSpace.addView(post)
    }

    private fun getDistance(objectLatitude: Number, objectLongitude: Number): Float {
        var distance = 0.0f
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return distance
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { userLocation ->
                if (userLocation != null) {
                    distance = computeDistance(userLocation.latitude, userLocation.longitude, objectLatitude.toDouble(), objectLongitude.toDouble())
                }
            }
            .addOnFailureListener { exception ->
                Log.e(this.toString(), "get failed with ", exception)
            }
        return distance
    }

    private fun computeDistance(userLatitude: Double, userLongitude: Double, objectLatitude: Double, objectLongitude: Double): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            userLatitude,
            userLongitude,
            objectLatitude,
            objectLongitude,
            result
        )
        return result[0]
    }
}