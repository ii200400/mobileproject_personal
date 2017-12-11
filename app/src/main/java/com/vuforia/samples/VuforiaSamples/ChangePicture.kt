package com.vuforia.samples.VuforiaSamples

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.Surface
import android.view.WindowManager

/**
 * Created by im on 2017-12-11.
 */
class ChangePicture(val context : Context) {
    fun LowerQuality(bitmap : Bitmap) : Bitmap {
        //앨범 이미지의 회전을 알 수 없어서 등록화면에서 화면을 돌리는 것으로 대체
        return Bitmap.createScaledBitmap(bitmap, 1080, 1920, true)
    }
}