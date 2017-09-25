package com.vuforia.samples.VuforiaSamples

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

// 처음 어플 실행시 뜨는 스플래시 구현할 클래스입니다.
class MainSplash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainsplash)

        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }
}
