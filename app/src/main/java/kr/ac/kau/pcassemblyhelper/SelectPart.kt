package kr.ac.kau.pcassemblyhelper

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*

class SelectPart:AppCompatActivity() {

    lateinit var spinner1: Spinner//신기해서 써봄

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_select)
        setContentView(R.layout.test_mainactivity)
        setTitle("PC 조립 도우미")

        var help : ImageButton = findViewById<ImageButton>(R.id.help)

        help.setOnClickListener { view ->

            var alert = AlertDialog.Builder(this)
            var str : String = "사용법 : 아래 재생 버튼을 누르면 AR이 실행됩니다. 메인보드를 비추세요."
            //AlertDialog.Builder(this).setTitle("도움말").setMessage(str)
            alert.setTitle("도움말")
            alert.setMessage(str)
            alert.setNeutralButton("닫기", DialogInterface.OnClickListener(){ dialog, which ->

                // 여기에 닫기버튼 눌렀을때 할 행동 넣기
            })
            alert.show()
        }

        /*
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
        */
    }
}
