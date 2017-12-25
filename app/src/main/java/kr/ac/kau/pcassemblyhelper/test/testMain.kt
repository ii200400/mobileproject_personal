package kr.ac.kau.pcassemblyhelper.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import kr.ac.kau.pcassemblyhelper.R

class testMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.test_main)
        setContentView(R.layout.test_mainactivity)

        var ab : ActionBar? = supportActionBar
        ab!!.title = "Main"
    }
}
