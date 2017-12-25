package kr.ac.kau.pcassemblyhelper.Camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

/**
 * Created by im on 2017-12-26.
 */
class BitmapController {
    fun smallerBitmap(bytes: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        //decoder가 bitmap에 대해 메모리를 할당하지 않도록 하는 옵션
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        // 사진 크기(화질)을 정하기
        options.inSampleSize = calculateSize(options, reqWidth, reqHeight)

        // 메모리 할당
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    // 제한된 크기에서의 최대 넓이 구하기
    fun calculateSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth

        var inSampleSize = 1
        while (height / inSampleSize >= reqHeight || width / inSampleSize >= reqWidth) {
            Log.e("-----------","2")
            inSampleSize *= 2
        }

        return inSampleSize
    }
}