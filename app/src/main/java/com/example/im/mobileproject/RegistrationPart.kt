package com.example.im.mobileproject

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

class RegistrationPart: AppCompatActivity() {
    val CAMERA_REQUEST_MODE = 0
    var filePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setTitle("부품 등록")

        val spin1: Spinner = findViewById(R.id.spinner1)

        val value = arrayListOf("카테고리","메인보드","CPU","RAM","메모리")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,value)

        val btnPicture:Button=findViewById(R.id.button1)
        val btnUpload:Button=findViewById(R.id.button2)

        //사진 찍기
        btnPicture.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                //다른 앱을 불러와 구동
                startActivityForResult(intent, CAMERA_REQUEST_MODE)
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_MODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    filePath = data.getData()
                    imageview.setImageBitmap(data.extras.get("data") as Bitmap)

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