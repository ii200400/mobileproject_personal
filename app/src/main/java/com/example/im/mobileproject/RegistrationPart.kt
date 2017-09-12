package com.example.im.mobileproject

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

class RegistrationPart: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setTitle("부품 등록")

        val spin1: Spinner = findViewById(R.id.spinner1)
        val text1: TextView = findViewById(R.id.textView1)

        val value = arrayListOf("아아~","아~아아ㅏ","마이크 테스트")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,value)
    }
}