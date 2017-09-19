package com.example.im.mobileproject

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

class Assembly : AppCompatActivity() {

    var fragment1 : Fragment1=Fragment1()
    var fragment2 : Fragment2=Fragment2()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assembly)
        setTitle("조립 팁")

        val spin1: Spinner = findViewById(R.id.spinner)
        val spin2: Spinner = findViewById(R.id.spinner2)

        val category= arrayOf("카테고리","메인보드","CPU","RAM","메모리")

        spin2.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,category)
        fragmentManager.beginTransaction().replace(R.id.container,fragment1).commit()

    }
}
