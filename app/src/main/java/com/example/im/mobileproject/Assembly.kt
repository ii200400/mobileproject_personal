package com.example.im.mobileproject

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

class Assembly : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assembly)
        setTitle("조립 팁")

        val spin1: Spinner = findViewById(R.id.spinner)
        val spin2: Spinner = findViewById(R.id.spinner2)
        val bnt:Button=findViewById(R.id.button)
        val bnt2:Button=findViewById(R.id.button2)
        val category= arrayOf("카테고리","메인보드","CPU","RAM","메모리")

        spin2.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,category)

        bnt.setOnClickListener {

        }

        bnt2.setOnClickListener {
            val intent1:Intent=Intent(this@Assembly,Assembly_Register::class.java)
            startActivity(intent1)
        }

    }

}
