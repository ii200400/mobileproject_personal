package com.example.im.mobileproject

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Vufo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vufo)

        val btnVufo: Button = findViewById(R.id.button_vufo);

        btnVufo.setOnClickListener{

            var mClassToLaunchPackage = packageName
            var mClassToLaunch = mClassToLaunchPackage + ".app.ImageTargets.ImageTargets"

            val i = Intent()
            i.setClassName(mClassToLaunchPackage, mClassToLaunch)
            startActivity(i)

        }
    }
}
