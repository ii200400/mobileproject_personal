package kr.ac.kau.pcassemblyhelper.test

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ProgressBar
import com.felipecsl.gifimageview.library.GifImageView
import kr.ac.kau.pcassemblyhelper.R
import kr.ac.kau.pcassemblyhelper.MainActivity
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.InputStream
import kr.ac.kau.pcassemblyhelper.R.id.gifImageView

class Splash : AppCompatActivity()
{
    lateinit var gifImageView : GifImageView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        gifImageView = findViewById<GifImageView>(R.id.gifImageView)

        try
        {
            var inputStream : InputStream = assets.open("kau.gif")
            val bytes = IOUtils.toByteArray(inputStream)
            gifImageView.setBytes(bytes)
            gifImageView.startAnimation()
        }
        catch (e : IOException)
        {

        }

        Handler().postDelayed({
            val intent : Intent = Intent(this@Splash, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
