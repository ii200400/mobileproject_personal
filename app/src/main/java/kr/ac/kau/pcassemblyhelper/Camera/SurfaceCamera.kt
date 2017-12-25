package kr.ac.kau.pcassemblyhelper.Camera

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
import android.support.v4.graphics.BitmapCompat
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.camera_api.*
import kr.ac.kau.pcassemblyhelper.R
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
    val bitmapController : BitmapController = BitmapController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_api)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        mCamera = getCameraInstance()

        if (mCamera != null && checkCameraHardware(this)) {
            ImageSurfaceView = SurfaceClass(this, mCamera!!)
            texture.addView(ImageSurfaceView)
        }

        btn_takepicture.setOnClickListener{
            mCamera!!.takePicture(null, null, pictureCallback)
        }
    }

    var pictureCallback: Camera.PictureCallback = Camera.PictureCallback { data, camera ->
        if (data != null) {
            BitmapController()
            bitmap = bitmapController.smallerBitmap(data, 1400,1400)

            //사진 회전
            val matrix = Matrix()
            val windowManager : WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val rotation : Int = windowManager.getDefaultDisplay().getRotation()
            when (rotation){
                Surface.ROTATION_0 ->  matrix.postRotate(90f)
                Surface.ROTATION_270 ->  matrix.postRotate(180f)
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap!!.getWidth(), bitmap!!.getHeight(), matrix, true)

            saveImageToGallary()

            //Bitmap을 바로 넣어주면 크기가 커서 그런지 아예 intent가 바뀌지 않는다고 한다.
            val preview_intent = Intent(applicationContext, PreviewImage::class.java)
            preview_intent.putExtra("uri", uri)
            startActivityForResult(preview_intent, PREVIEW_CODE)
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
        uri = Uri.fromFile(file_path)
        val imageOut : OutputStream = FileOutputStream(file_path)
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, imageOut)
        imageOut.flush()
        imageOut.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            PREVIEW_CODE->{
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val confirm : Boolean = data.extras.get("confirm") as Boolean
                    if (confirm) {
                        mCamera!!.release()

                        val answer_intent : Intent = Intent()
                        answer_intent.putExtra("uri", uri)
                        setResult(Activity.RESULT_OK, answer_intent)
                        //bitmap!!.recycle()

                        finish()
                    }else{
                        val delete_file : File = File(uri!!.getPath())
                        if (delete_file.delete()) {
                            System.out.println("file Deleted :" + delete_file)
                        } else {
                            System.out.println("file not Deleted :" + delete_file)
                        }
                    }
                }else{
                    mCamera!!.startPreview()
                }
            }
        }
    }

    override fun onDestroy() {
        //SurfaceClass의 mCamera.stopPreview()가 불리고 나서 불러야 하므로 이 함수를 쓴다.
        //onBackPressed에 넣으면 relase()가 먼저 불려서 에러가 난다.
        mCamera!!.release()

        super.onDestroy()
    }
}