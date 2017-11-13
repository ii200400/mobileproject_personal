package com.vuforia.samples.VuforiaSamples.AR

/**
 * Created by J on 2017-10-17.
 */

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

// Support class for the Vuforia applications.
// Exposes functionality for loading a texture from the APK.
// 뷰포리아 동작을 지원하는 클래스
// 텍스쳐 로딩 기능을 구현한다.
class AppTexture
{
    var mWidth : Int = 0    // 텍스처 넓이
    var mHeight : Int = 0    // 텍스처 높이
    var mChannels : Int = 0  // 채널 수
    lateinit var mData : ByteBuffer     // 픽셀 데이터
    var mTextureID = IntArray(1)    // 배열 크기를 1로 해야할지 2로 해야할지 테스트 필요
    var mSuccess : Boolean = false

    /* Factory function to load a texture from the APK. */
    // apk에서 텍스처 로딩하는 함수
    // 파일을 읽어서 비트맵으로 변환시킨다.
    fun loadTextureFromApk(fileName: String, assets: AssetManager) : AppTexture?
    {
        var inputStream : InputStream? = null

        try {
            // assets 폴더에서 파일 오픈
            inputStream = assets.open(fileName, AssetManager.ACCESS_BUFFER)

            val bufferedStream = BufferedInputStream(inputStream!!)

            // 파일에서 읽은 버퍼를 디코딩해서 비트맵으로 변환
            val bitMap = BitmapFactory.decodeStream(bufferedStream)

            // 비트맵의 크기만큼의 Int 배열 생성
            val data = IntArray(bitMap.width * bitMap.height)

            // data 배열에 비트맵을 대입
            bitMap.getPixels(data, 0, bitMap.width, 0, 0, bitMap.width, bitMap.height)

            // 파일에서 읽은 비트맵을 파라미터로 아래 함수 호출하고 리턴
            return loadTextureFromIntBuffer(data, bitMap.width, bitMap.height)
        }
        // 오류 발생시 로그 출력
        catch (e: IOException) {
            Log.e("AppTexture", "Failed to log texture '$fileName' from APK")
            Log.i("AppTexture", e.message)

            return null
        }
    }

    // 파일에서 읽은 비트맵을 완성시키는 함수
    // 픽셀에 색을 입혀서 텍스쳐로 만든다.
    fun loadTextureFromIntBuffer(data: IntArray?, width: Int, height: Int): AppTexture
    {
        var data = data
        val numPixels = width * height  // 총 픽셀의 수

        // RGB와 알파값 때문에 픽셀당 4개의 byte가 필요하다.
        var dataBytes : ByteArray? = ByteArray(numPixels * 4)

        // 픽셀의 수가 0이 될때까지 반복
        for (p in 0 until numPixels)
        {
            // 각각 RGB와 알파값을 대입한다.
            val colour = data!![p]
            dataBytes!![p * 4] = colour.ushr(16).toByte() // R
            dataBytes!![p * 4 + 1] = colour.ushr(8).toByte() // G
            dataBytes!![p * 4 + 2] = colour.toByte() // B
            dataBytes!![p * 4 + 3] = colour.ushr(24).toByte() // A
        }

        // 텍스처의 넓이, 높이, 채널(RGB+Alpha 총 4개) 지정
        val texture = AppTexture()
        texture.mWidth = width
        texture.mHeight = height
        texture.mChannels = 4

        // 위에서 완성시킨 비트맵을 텍스처 데이터로 할당
        texture.mData = ByteBuffer.allocateDirect(dataBytes!!.size).order(ByteOrder.nativeOrder())

        // 할당한 비트맵에 픽셀값을 저장
        val rowSize = texture.mWidth * texture.mChannels
        for (r in 0 until texture.mHeight)
            texture.mData.put(dataBytes, rowSize * (texture.mHeight - 1 - r), rowSize)

        // 비트맵의 핸들링 포지션을 처음으로 되돌림
        texture.mData.rewind()

        // 변수 초기화
        // 각각 메모리 공간을 많이 차지하므로 초기화해줘야한다.
        dataBytes = null
        data = null

        // 텍스처 로딩이 성공했음
        texture.mSuccess = true

        // 로딩된 텍스처를 리턴
        return texture
    }
}