package com.vuforia.samples.VuforiaSamples.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vuforia.samples.VuforiaSamples.R

import com.vuforia.samples.VuforiaSamples.Firebase
import kotlinx.android.synthetic.main.test_firebasetest.*

/**
 * Created by J on 2017-11-12.
 */

class FirebaseTest : AppCompatActivity()
{
    lateinit var mStorageRef : StorageReference
    var fb : Firebase = Firebase();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_firebasetest)

        mStorageRef = FirebaseStorage.getInstance().reference

        var btn1 : Button = findViewById<Button>(R.id.upload)
        var btn2 : Button = findViewById<Button>(R.id.download)
        var img : ImageView = findViewById<ImageView>(R.id.testimg)

        var x : String = getExternalFilesDir(null).toString()
        Log.d("test12345",x)

        btn1.setOnClickListener{
            fb.uploadFile("/storage/emulated/0/Android/data/com.vuforia.samples.VuforiaSamples/files/test.jpg", mStorageRef);
        }
        btn2.setOnClickListener {
            var fname : String = fb.downloadFile(mStorageRef)

        }
    }
}