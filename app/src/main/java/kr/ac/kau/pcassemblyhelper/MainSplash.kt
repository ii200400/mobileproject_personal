package kr.ac.kau.pcassemblyhelper

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

// 처음 어플 실행시 뜨는 스플래시 구현할 클래스입니다.
class MainSplash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_mainsplash)

        // 1초 동안 정지하고 스플래시 이미지 띄우기
        try {
            Thread.sleep(1000);
        }
        catch (e : Exception)
        {
            print("스플래시 에러")
        }

        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }
}
