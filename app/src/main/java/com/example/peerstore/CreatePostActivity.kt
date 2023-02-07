package com.example.peerstore

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.peerstore.databinding.ActivityCreatePostBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CreatePostActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage

    val REQUEST_TAKE_PHOTO = 1
    private var imageName: String = ""
    private lateinit var imagePath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
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
            finish()
        }
        binding.NewPostImage.setOnClickListener {
            takePicture()
        }
        binding.CreatePostButton.setOnClickListener {
            if (checkAllField()) {
                val file = Uri.fromFile(File(imagePath))
                storage.reference.child(imageName).putFile(file)
                    .addOnSuccessListener {
                        storage.reference.child(imageName).downloadUrl
                            .addOnSuccessListener {
                                val image = it.toString()
                                val title: String = binding.NewPostTitle.text.toString()
                                val descriptionOwner: String = binding.NewPostDescription.text.toString()
                                val price: String = binding.editTextNumberDecimal.text.toString()
                                val owner = user.uid



                                var descriptionAI = "| "
                                val imageUri = Uri.fromFile(File(imagePath))
                                val options = ObjectDetectorOptions.Builder()
                                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                                    .enableMultipleObjects()
                                    .enableClassification()  // Optional
                                    .build()
                                val objectDetector = ObjectDetection.getClient(options)
                                try {
                                    val imageInput: InputImage = InputImage.fromFilePath(this, imageUri)
                                    imageInput.rotationDegrees.rotateLeft(90)
                                    objectDetector.process(imageInput)
                                        .addOnSuccessListener { detectedObjects ->
                                            for (detectedObject in detectedObjects) {
                                                for (label in detectedObject.labels) {
                                                    val text = label.text
                                                    val index = label.index
                                                    val confidence = label.confidence
                                                    descriptionAI =
                                                        "$descriptionAI${index} - $text - confidence $confidence | "
                                                }
                                            }



                                            val postData: HashMap<String,*> = hashMapOf(
                                                "title" to title,
                                                "image" to image,
                                                "imageName" to imageName,
                                                "descriptionAI" to descriptionAI,
                                                "descriptionOwner" to descriptionOwner,
                                                "price" to price,
                                                "owner" to owner,
                                                "latitude" to getLatitude(),
                                                "longitude" to getLongitude(),
                                                "globalChat" to listOf<String>(),
                                            )
                                            db.collection("posts").add(postData)
                                                .addOnSuccessListener(this) { post ->
                                                    db.collection("users").document(user.uid).get()
                                                        .addOnSuccessListener { it ->
                                                            if (it != null) {
                                                                val itData: HashMap<String, *> =
                                                                    it.data as HashMap<String, *>
                                                                (itData["posts"] as ArrayList<String>).add(post.id)
                                                                db.collection("users").document(user.uid)
                                                                    .update("posts", itData["posts"])
                                                                    .addOnSuccessListener {
                                                                        Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()
                                                                        val intent = Intent(this, ProfileActivity::class.java)
                                                                        startActivity(intent)
                                                                        finish()
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
                                                        .addOnFailureListener(this) { e ->
                                                            Log.e("error: ", e.toString())
                                                            Toast.makeText(this, e.toString().substringAfter(":"), Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                                .addOnFailureListener(this) { e ->
                                                    Log.e("error: ", e.toString())
                                                    Toast.makeText(this, e.toString().substringAfter(":"), Toast.LENGTH_SHORT).show()
                                                }




                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("error: ", e.toString())
                                            Toast.makeText(this, e.toString().substringAfter(":"), Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("error: ", e.toString())
                                Toast.makeText(this, e.toString().substringAfter(":"), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener(this) { e ->
                        Log.e("error: ", e.toString())
                        Toast.makeText(this, e.toString().substringAfter(":"), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            binding.NewPostImage.rotation = 90f
            binding.NewPostImage.setImageURI(Uri.fromFile(File(imagePath)))
        }
    }

    private fun createImageFile(): File? {
        val imgName = "${user.uid}_" + UUID.randomUUID().toString()
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imgName, ".jpg", storageDir)
        imageName = "$imgName.jpg"
        imagePath = imageFile.absolutePath
        return imageFile
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException){}
            if (photoFile != null){
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.android.provider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    private fun checkAllField(): Boolean {
        if (imageName == "") {
            Toast.makeText(this, "Photo is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.NewPostTitle.text.toString() == "") {
            binding.NewPostTitle.error = "This is required field"
            return false
        }
        if (binding.editTextNumberDecimal.text.toString() == "") {
            binding.editTextNumberDecimal.error = "This is required field"
            return false
        }
        return true
    }

    private fun getLatitude(): Double {
        val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var latitude = 0.0
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return latitude
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { userLocation ->
                if (userLocation != null) {
                    latitude = userLocation.latitude
                }
            }
            .addOnFailureListener { exception ->
                Log.e(this.toString(), "get failed with ", exception)
            }
        return latitude
    }

    private fun getLongitude(): Double {
        val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var longitude = 0.0
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return longitude
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { userLocation ->
                if (userLocation != null) {
                    longitude = userLocation.longitude
                }
            }
            .addOnFailureListener { exception ->
                Log.e(this.toString(), "get failed with ", exception)
            }
        return longitude
    }

}