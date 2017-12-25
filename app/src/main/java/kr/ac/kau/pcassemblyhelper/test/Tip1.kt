package kr.ac.kau.pcassemblyhelper.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

import kr.ac.kau.pcassemblyhelper.R

class Tip1 : AppCompatActivity()
{
    lateinit var spin_text1 : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip1)

        var items1 : Array<String> = arrayOf("Mainboard", "CPU", "VGA", "RAM")

        var spin1 : Spinner = findViewById(R.id.spinner_category)

        var adapter1 : ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items1)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spin1.adapter = adapter1

        /*
        spin1.setOnClickListener {


        }
        */
    }
}
