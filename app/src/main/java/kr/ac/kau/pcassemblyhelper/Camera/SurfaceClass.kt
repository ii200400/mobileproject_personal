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
        // preview시작
        val parameters = mCamera!!.parameters
        parameters.pictureFormat = ImageFormat.JPEG
        parameters.jpegQuality = 100
        //TODO 기종마다 문제가 되는 경우가 있다.
        parameters.setPreviewSize(width, height)

        val windowManager : WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation : Int = windowManager.getDefaultDisplay().getRotation()
        when (rotation){
            Surface.ROTATION_0 -> mCamera.setDisplayOrientation(90)
            Surface.ROTATION_270 -> mCamera.setDisplayOrientation(180)
        }
        mCamera.parameters = parameters

        // Important: Call startPreview() to start updating the preview surface.
        // Preview must be started before you can take a picture.
        mCamera.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // empty. Take care of releasing the Camera preview in your activity.
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview()
    }
}