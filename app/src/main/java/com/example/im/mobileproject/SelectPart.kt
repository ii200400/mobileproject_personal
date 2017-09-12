package com.example.im.mobileproject

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class SelectPart:AppCompatActivity() {

    lateinit var spinner1: Spinner//신기해서 써봄

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        val spin1:Spinner = findViewById(R.id.spinner1)
        val spin2:Spinner = findViewById(R.id.spinner2)
        val spin3:Spinner = findViewById(R.id.spinner3)
        val spin4:Spinner = findViewById(R.id.spinner4)
        val spin5:Spinner = findViewById(R.id.spinner5)
        val spin6:Spinner = findViewById(R.id.spinner6)

        val mainBoard = arrayListOf("메","인","보","드")
        val cpu = arrayListOf("C","P","U")
        val ram = arrayListOf("R","A","M")
        val vga = arrayListOf("V","G","A")
        val hdd = arrayListOf("HDD","or","SDD")
        val etc = arrayListOf("ㄱ","ㅣ","ㅌ","ㅏ")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mainBoard)
        spin2.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,cpu)
        spin3.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,ram)
        spin4.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vga)
        spin5.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hdd)
        spin6.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,etc)
    }

}