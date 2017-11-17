package com.vuforia.samples.VuforiaSamples.test

import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.vuforia.samples.VuforiaSamples.R

/**
 * Created by J on 2017-11-17.
 */

class Drawertest : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_mainactivity)

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
            }
            else if (position == 1)
            {
                content.setBackgroundColor(Color.rgb(0xFF, 0x00, 0x00))
                content.setTextColor(Color.rgb(0xFF, 0xFF, 0xff))
                content.setText("menu2")
            }
            else if (position == 2)
            {
                content.setBackgroundColor(Color.rgb(0x00, 0xFF, 0x00))
                content.setTextColor(Color.rgb(0x00, 0x00, 0x00))
                content.setText("menu3")
            }

            var drawer : DrawerLayout = findViewById<DrawerLayout>(R.id.drawer)
            drawer.closeDrawer(Gravity.LEFT)
        }
    }
}