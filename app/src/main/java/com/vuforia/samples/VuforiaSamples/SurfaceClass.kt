package com.vuforia.samples.VuforiaSamples

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

/**
 * Created by im on 2017-12-03.
 */
class SurfaceClass(context: Context, val mCamera: Camera) : SurfaceView(context), SurfaceHolder.Callback {
    private val surfaceHolder: SurfaceHolder

    init{
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder()
        surfaceHolder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera!!.setPreviewDisplay(holder)
            mCamera!!.startPreview()
        } catch (e : IOException) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        val parameters = mCamera!!.parameters
        parameters.pictureFormat = ImageFormat.JPEG
        parameters.jpegQuality = 100
        parameters.setRotation(90)
        parameters.setPreviewSize(640, 480)
        mCamera!!.parameters = parameters

        // Important: Call startPreview() to start updating the preview surface.
        // Preview must be started before you can take a picture.
        mCamera!!.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // empty. Take care of releasing the Camera preview in your activity.
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview()
    }
}