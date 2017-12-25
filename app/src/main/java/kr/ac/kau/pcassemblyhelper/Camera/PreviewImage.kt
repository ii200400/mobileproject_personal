package kr.ac.kau.pcassemblyhelper.Camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.vuforia.samples.VuforiaSamples.R
import kotlinx.android.synthetic.main.camera_preview.*

/**
 * Created by im on 2017-10-31.
 */
class PreviewImage : AppCompatActivity() {
    val answer_intent : Intent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_preview)

        val uri = intent.extras.get("uri") as Uri
        val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
        previewImage.setImageBitmap(bitmap)

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