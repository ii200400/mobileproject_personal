package kr.ac.kau.pcassemblyhelper

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.camera_preview.*

/**
 * Created by im on 2017-10-31.
 */
class PreviewImage : AppCompatActivity() {
    val answer_intent : Intent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_preview)

        previewImage.setImageBitmap(intent.extras.get("image") as Bitmap)

        confirm_button.setOnClickListener(){
            answer_intent.putExtra("confirm", true)
            setResult(RESULT_OK, answer_intent)
            finish()
        }
        cancel_button.setOnClickListener(){
            answer_intent.putExtra("confirm", false)
            setResult(RESULT_OK, answer_intent)
            finish()
        }
    }

    override fun onBackPressed() {
        answer_intent.putExtra("confirm", false)
        setResult(RESULT_OK, answer_intent)
        super.onBackPressed()
    }
}
