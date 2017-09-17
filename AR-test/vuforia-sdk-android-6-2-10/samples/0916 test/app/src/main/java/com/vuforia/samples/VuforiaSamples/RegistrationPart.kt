package com.vuforia.samples.VuforiaSamples

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*

class RegistrationPart: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setTitle("부품 등록")

        val spin1: Spinner = findViewById(R.id.spinner1) as Spinner
        val text1: TextView = findViewById(R.id.textView1) as TextView

        val value = arrayListOf("카테고리","메인보드","CPU","RAM","메모리")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,value)

        val btn : Button = findViewById(R.id.button1) as Button

        btn.setOnClickListener {
            val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
