package kr.ac.kau.pcassemblyhelper.test

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kr.ac.kau.pcassemblyhelper.R
import kr.ac.kau.pcassemblyhelper.MainActivity

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        try
        {
            Thread.sleep(1000);
        }
        catch (e : InterruptedException)
        {
            Log.d("splash", e.toString())
        }

        val intent : Intent = Intent(this@Splash, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
