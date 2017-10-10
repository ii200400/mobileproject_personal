package com.vuforia.samples.VuforiaSamples

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_registration.*
import android.widget.Toast
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.os.Build

class RegistrationPart: AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    val CAMERA_REQUEST_MODE = 0
    var filePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setTitle("부품 등록")

        val spin1: Spinner = findViewById(R.id.spinner1)

        val value = arrayListOf("카테고리","메인보드","CPU","RAM","메모리")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,value)

        val btnPicture : Button = findViewById(R.id.button1)
        val btnUpload : Button = findViewById(R.id.button2)

        //사진 찍기
        btnPicture.setOnClickListener {
            /*
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                //다른 앱을 불러와 구동
                startActivityForResult(intent, CAMERA_REQUEST_MODE)
            }
            */
            val intent_picture : Intent = Intent(this@RegistrationPart,RegistrationPart_SP::class.java)
            startActivity(intent_picture)
        }

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                // 런타임 퍼미션 처리 필요

                val hasCameraPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                val hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)

                if (hasCameraPermission == PackageManager.PERMISSION_GRANTED && hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                }//이미 퍼미션을 가지고 있음
                else {
                    //퍼미션 요청
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSIONS_REQUEST_CODE)
                }
            } else {
            }
            */

        } else {
            Toast.makeText(this, "Camera not supported",
                    Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_MODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    filePath = data.getData()
                    pickedImage.setImageBitmap(data.extras.get("data") as Bitmap)

                    //val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath)
                    //iv_preview.setImageBitmap(bitmap);
                }
            }
            else -> {
                Toast.makeText(this, "unrecognized request", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
