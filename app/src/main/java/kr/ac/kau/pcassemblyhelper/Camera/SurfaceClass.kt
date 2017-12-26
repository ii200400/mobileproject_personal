package kr.ac.kau.pcassemblyhelper.Camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import java.io.IOException
import android.view.*


/**
 * Created by im on 2017-12-03.
 */
class SurfaceClass(context: Context, val mCamera: Camera) : SurfaceView(context), SurfaceHolder.Callback {
    private val surfaceHolder: SurfaceHolder

    init{
        // 카메라 상태(생성, 종료)에 대한 홀더와 콜백 초기화
        surfaceHolder = getHolder()
        surfaceHolder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        // 카메라 생성 한번만 불려오는 것
        try {
            mCamera.setPreviewDisplay(holder)
            mCamera.startPreview()
        } catch (e : IOException) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        //파라미터 지정
        val parameters = mCamera!!.parameters
        parameters.pictureFormat = ImageFormat.JPEG
        parameters.jpegQuality = 100

        //화면 상태에 따라 사진 돌리기
        val windowManager : WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation : Int = windowManager.getDefaultDisplay().getRotation()
        when (rotation){
            Surface.ROTATION_0 -> mCamera.setDisplayOrientation(90)
            Surface.ROTATION_270 -> mCamera.setDisplayOrientation(180)
        }
        mCamera.parameters = parameters
        mCamera.getParameters().getSupportedVideoSizes()
        mCamera.getParameters().getSupportedPictureSizes()
        mCamera.getParameters().getSupportedPreviewSizes()

        // preview 시작
        mCamera.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // preview 멈춤
        mCamera.stopPreview()
    }
}