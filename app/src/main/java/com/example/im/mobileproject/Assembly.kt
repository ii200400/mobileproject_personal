package com.example.im.mobileproject

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.widget.AdapterView
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
        //val bnt:Button=findViewById(R.id.button)
       // val bnt2:Button=findViewById(R.id.button2)
        val category= arrayOf("카테고리","메인보드","CPU","RAM","메모리")

        //spin2.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,category)
       // val adapter: ArrayAdapter<String> =  ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,category)
        val adapter1: ArrayAdapter<CharSequence> =ArrayAdapter.createFromResource(this,R.array.category,android.R.layout.simple_spinner_dropdown_item)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spin2.setAdapter(adapter1)

        var fragmentmanager: FragmentManager=this.supportFragmentManager
        var transaction: FragmentTransaction
        var fragment1 : Fragment


        fragment1 =Fragment1()
        transaction = fragmentmanager.beginTransaction()
        transaction.add(R.id.container,fragment1 ).addToBackStack(null).commit()

        spin2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    val adapter2: ArrayAdapter<CharSequence> =ArrayAdapter.createFromResource(this@Assembly,R.array.name,android.R.layout.simple_list_item_1)
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spin1.setAdapter(adapter2)
                    Fragment1.imageveiw_data.change_imageveiw(p2)
                    //fragment1 = getFragmentManager().findFragmentById(R.layout.fragment1) as Fragment
                   // fragment1.changed
                }
                else if(p2==1){
                    val adapter2: ArrayAdapter<CharSequence> =ArrayAdapter.createFromResource(this@Assembly,R.array.mb_name,android.R.layout.simple_list_item_1)
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spin1.setAdapter(adapter2)
                    Fragment1.imageveiw_data.change_imageveiw(p2)
                }
                else if(p2==2){
                    val adapter2: ArrayAdapter<CharSequence> =ArrayAdapter.createFromResource(this@Assembly,R.array.ram_name,android.R.layout.simple_list_item_1)
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spin1.setAdapter(adapter2)
                    Fragment1.imageveiw_data.change_imageveiw(p2)
                }
                else if(p2==3){
                    val adapter2: ArrayAdapter<CharSequence> =ArrayAdapter.createFromResource(this@Assembly,R.array.graphics_name,android.R.layout.simple_list_item_1)
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spin1.setAdapter(adapter2)
                    Fragment1.imageveiw_data.change_imageveiw(p2)
                }

            }
        }

       /* bnt.setOnClickListener {

        }

        bnt2.setOnClickListener {
           /// val intent1:Intent=Intent(this@Assembly,Assembly_Register::class.java)
            //startActivity(intent1)
        }*/

    }

}
