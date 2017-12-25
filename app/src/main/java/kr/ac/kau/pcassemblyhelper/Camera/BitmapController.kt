package kr.ac.kau.pcassemblyhelper.Camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Created by im on 2017-12-26.
 */
class BitmapController {
    fun smallerBitmap(bytes: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val ONE_MEGABYTE : Int = 1024 * 1024
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bytes, 0, ONE_MEGABYTE, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bytes, 0, ONE_MEGABYTE, options)
    }

    fun calculateSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}