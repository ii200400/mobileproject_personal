package kr.ac.kau.pcassemblyhelper.test

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Button

import kotlinx.android.synthetic.main.activity_drawer_sample.*
import kotlinx.android.synthetic.main.app_bar_drawer_sample.*
import kr.ac.kau.pcassemblyhelper.*

class drawerSample : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_sample)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        if (Build.VERSION.SDK_INT >= 21)
        {
            window.statusBarColor = Color.BLACK
        }
/*
        val btn1 : Button = findViewById(R.id.button_1)
        val btn2 : Button = findViewById(R.id.button_2)
        val btn3 : Button = findViewById(R.id.button_3)
        val btn4 : Button = findViewById(R.id.button_4)

        btn1.setOnClickListener{
            val intent1:Intent = Intent(this, SelectPart::class.java)
            startActivity(intent1)
        }
        btn2.setOnClickListener{
            val intent2:Intent = Intent(this, RegistrationPart::class.java)
            startActivity(intent2)
        }
        btn3.setOnClickListener{
            val intent3:Intent = Intent(this, Assembly::class.java)
            startActivity(intent3)
        }
        btn4.setOnClickListener{
            //val intent4:Intent = Intent(this@MainActivity,ActivityLauncher::class.java)
            val intent4:Intent = Intent(this, drawerSample::class.java)
            startActivity(intent4)
            finish()
        }
        */
    }

    override fun onBackPressed()
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
        {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else
        {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem) : Boolean
    {
        // Handle navigation view item clicks here.
        when (item.itemId)
        {
            R.id.nav_camera -> {
                val intent:Intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
