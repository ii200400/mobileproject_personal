package com.vuforia.samples.VuforiaSamples

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import kotlinx.android.synthetic.main.activity_registration.*
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class RegistrationPart : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    val CAMERA_REQUEST_MODE = 100

    var image_bitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setTitle("부품 등록")

        val spin1: Spinner = findViewById(R.id.spinner1)
        val value = arrayListOf("카테고리","메인보드","CPU","RAM","메모리")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,value)

        //사진 찍기
        button1.setOnClickListener {
            /*
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                //다른 앱을 불러와 구동
                startActivityForResult(intent, CAMERA_REQUEST_MODE)
            }
            */

            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                    // 런타임 권한 처리 필요
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        val intent_picture: Intent = Intent(this@RegistrationPart, SurfaceCamera::class.java)
                        startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
                    }else{
                        //카메라, 저장소 이미지 권한 요청
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                    }
                }
            }else {
                Toast.makeText(this, "Camera not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                val intent_picture: Intent = Intent(this@RegistrationPart, SurfaceCamera::class.java)
                startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
            }else{
                Toast.makeText(this, "카메라와 저장소 권한이 필요한데 없군요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_MODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    image_bitmap = data.extras.get("image") as Bitmap
                    pickedImage.setImageBitmap(image_bitmap)
                }
            }
            else -> {
                Toast.makeText(this, "unrecognized request", Toast.LENGTH_SHORT).show()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}