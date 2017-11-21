package kr.ac.kau.pcassemblyhelper

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*

class SelectPart:AppCompatActivity() {

    lateinit var spinner1: Spinner//신기해서 써봄

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        setTitle("부품 선택")

        val spin1:Spinner = findViewById(R.id.spinner1)
        val spin2:Spinner = findViewById(R.id.spinner2)
        val spin3:Spinner = findViewById(R.id.spinner3)
        val spin4:Spinner = findViewById(R.id.spinner4)
        val spin5:Spinner = findViewById(R.id.spinner5)
        val spin6:Spinner = findViewById(R.id.spinner6)

        val mainBoard = arrayListOf("메인보드")
        val cpu = arrayListOf("CPU")
        val ram = arrayListOf("RAM")
        val vga = arrayListOf("VGA")
        val hdd = arrayListOf("HDD","SDD")
        val etc = arrayListOf("기타")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mainBoard) as SpinnerAdapter?
        spin2.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,cpu) as SpinnerAdapter?
        spin3.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,ram) as SpinnerAdapter?
        spin4.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vga) as SpinnerAdapter?
        spin5.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hdd) as SpinnerAdapter?
        spin6.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,etc) as SpinnerAdapter?

        val startbtn : Button = findViewById(R.id.button_startAR)

        startbtn.setOnClickListener{
            //val intent : Intent = Intent(this, ImageTargets::class.java)
            //startActivity(intent)
        }
    }
}
