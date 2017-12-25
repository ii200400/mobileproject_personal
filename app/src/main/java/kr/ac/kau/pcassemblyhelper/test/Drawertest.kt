package kr.ac.kau.pcassemblyhelper.test

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView

import kr.ac.kau.pcassemblyhelper.R

/**
 * Created by J on 2017-11-17.
 */

class Drawertest : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_mainactivity)
/*
        var items = arrayListOf<String>("menu1", "menu2", "menu3")
        var adapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

        var listview : ListView = findViewById<ListView>(R.id.drawer_menu)
        listview.adapter = adapter

        listview.setOnItemClickListener{ parent, view, position, id ->

            var content : TextView = findViewById<TextView>(R.id.drawer_main)

            if (position == 0)
            {
                content.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF))
                content.setTextColor(Color.rgb(0x00, 0x00, 0x00))
                content.setText("menu1")

                //var intent : Intent = Intent(this@Drawertest, SelectPart::class.java)
                //startActivity(intent)
            }
            else if (position == 1)
            {
                content.setBackgroundColor(Color.rgb(0xFF, 0x00, 0x00))
                content.setTextColor(Color.rgb(0xFF, 0xFF, 0xff))
                content.setText("menu2")

                //var intent : Intent = Intent(this@Drawertest, RegistrationPart::class.java)
                //startActivity(intent)
            }
            else if (position == 2)
            {
                content.setBackgroundColor(Color.rgb(0x00, 0xFF, 0x00))
                content.setTextColor(Color.rgb(0x00, 0x00, 0x00))
                content.setText("menu3")

                //var intent : Intent = Intent(this@Drawertest, Assembly::class.java)
                //startActivity(intent)
            }

            var drawer : DrawerLayout = findViewById<DrawerLayout>(R.id.drawer)
            drawer.closeDrawer(Gravity.LEFT)
        }

        var help : ImageButton = findViewById<ImageButton>(R.id.help)

        help.setOnClickListener { view ->

            var alert = AlertDialog.Builder(this)
            var str : String = "사용법 : ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ"
            //AlertDialog.Builder(this).setTitle("도움말").setMessage(str)
            alert.setTitle("도움말")
            alert.setMessage(str)
            alert.setNeutralButton("닫기", DialogInterface.OnClickListener(){dialog, which ->

                // 여기에 닫기버튼 눌렀을때 할 행동 넣기
            })
            alert.show()
        }

        var img_btn : ImageButton = findViewById<ImageButton>(R.id.img_btn)

        img_btn.setOnClickListener { view ->

            //var intent : Intent = Intent(this@Drawertest, ImageTargets::class.java)
            //startActivity(intent)
        }
        */
    }
}
