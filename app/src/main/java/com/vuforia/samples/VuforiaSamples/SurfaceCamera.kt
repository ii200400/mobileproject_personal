package com.vuforia.samples.VuforiaSamples

import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.camera_api.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by im on 2017-09-19.
 */
class SurfaceCamera : AppCompatActivity() {
    val PREVIEW_CODE = 100

    var uri : Uri? = null
    var bitmap : Bitmap? = null
    private var ImageSurfaceView: SurfaceClass? = null
    private var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_api)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        mCamera = getCameraInstance()

        if (mCamera != null && checkCameraHardware(this)) {
            ImageSurfaceView = SurfaceClass(this, mCamera!!)
            texture.addView(ImageSurfaceView)
        }

        btn_takepicture.setOnClickListener(){
            mCamera!!.takePicture(null, null, pictureCallback)
        }
    }

    var pictureCallback: Camera.PictureCallback = Camera.PictureCallback { data, camera ->
        if (data != null) {
            //TODO 이미지 변형
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inSampleSize = 2
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, null)


            saveImageToGallary()
            //Bitmap을 바로 넣어주면 크기가 커서 그런지 아예 intent가 바뀌지 않는다고 한다.
//            val preview_intent = Intent(applicationContext, PreviewImage::class.java)
//            preview_intent.putExtra("image", data)
//            startActivityForResult(preview_intent, PREVIEW_CODE)
        } else {
            Toast.makeText(this, "Captured image is empty", Toast.LENGTH_LONG).show()
        }
        mCamera!!.startPreview()
    }

    fun getCameraInstance(): Camera? {
        var c: Camera? = null
        try {
            // 카메라 객체를 가져옴
            c = Camera.open()
        } catch (e: Exception) {
            // 카메라 사용 불가
        }

        return c // returns null if camera is unavailable
    }

    private fun checkCameraHardware(context: Context): Boolean {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // 카메라 장치가 있음
            return true
        } else {
            Log.e("---------","카메라 장치가 없습니다.")
            return false
        }
    }

    private fun saveImageToGallary(){
        //파일에 저장
        val file : File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "camtest")
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.d("------", "failed to create directory")
                return
            }
        }

        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file_path : File = File(file.getPath() + File.separator + timeStamp + ".jpg")
        val imageOut : OutputStream = FileOutputStream(file_path)
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, imageOut)
        imageOut.flush()
        imageOut.close()

//        //갤러리에서 이미지 볼 수 있도록 하기
//        val values = ContentValues()
//        values.put(MediaStore.Images.Media.TITLE, "사진1")
//        values.put(MediaStore.Images.Media.DISPLAY_NAME, "사진2")
//        values.put(MediaStore.Images.Media.DESCRIPTION, "MobileProject")
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        values.put(MediaStore.Images.Media.ORIENTATION, 90)
//
//        //갤러리의 상단에 넣어주기
//        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
//        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
//
//        val fileUri : Uri = Uri.fromFile(file)
//        uri = contentResolver.insert(fileUri, values)
//        val gallary_imageOut : OutputStream = getContentResolver().openOutputStream(uri)
//        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, gallary_imageOut)
//        imageOut.flush()
//        imageOut.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            PREVIEW_CODE->{
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val confirm : Boolean = data.extras.get("confirm") as Boolean
                    if (confirm) {
                        saveImageToGallary()
                        mCamera!!.release()

                        val answer_intent : Intent = Intent()
                        answer_intent.putExtra("uri", uri)
                        setResult(Activity.RESULT_OK, answer_intent)
                        //bitmap!!.recycle()

                        finish()
                    }
                }else{
                    mCamera!!.startPreview()
                }
            }
        }
    }
}