//https://inducesmile.com/android/android-camera2-api-example-tutorial/
package com.example.im.mobileproject

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.TextureView
import android.widget.Button
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_90
import android.view.Surface.ROTATION_0
import android.util.SparseIntArray
import android.view.Surface
import android.os.HandlerThread
import android.media.ImageReader
import android.os.Handler
import android.util.Size
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import java.util.jar.Manifest


/**
 * Created by im on 2017-09-19.
 */
class RegistrationPart_SP : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private val TAG = "AndroidCameraApi"
    var textureView : TextureView? = null
    var takePictureButton : Button? = null
    private var cameraId : String = ""; //openCamera에서 정의
    private var ORIENTATIONS : SparseIntArray = object : SparseIntArray(){} //카메라 각도 설정(?)
    protected var cameraDevice : CameraDevice? = null
    protected var cameraCaptureSessions: CameraCaptureSession? = null
    //protected var captureRequest: CaptureRequest? = null 변수가 안쓰였다.
    protected var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private var file: File? = null
    private val REQUEST_CAMERA_PERMISSION = 200
    //private val mFlashSupported: Boolean = false ..?왜 있을까;
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null


    override fun onCreate(savedInstanceState: Bundle?) {//모두 구현
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_api)

        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)

        textureView = findViewById(R.id.texture)
        takePictureButton = findViewById(R.id.btn_takepicture)

        textureView!!.setSurfaceTextureListener(textureListener);

        takePictureButton!!.setOnClickListener{
            v->{
                takePicture()
            }
        }


    }

    val textureListener : TextureView.SurfaceTextureListener =  object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureAvailable (surface:SurfaceTexture, width:Int, height:Int){
            openCamera()
        }
        override fun onSurfaceTextureSizeChanged(surface:SurfaceTexture, width:Int, height:Int){
            //너비와 높이에 따라 이미지를 캡쳐 한 크기로 변환.. 할 수 있다는데..?
        }
        override fun onSurfaceTextureDestroyed(surface:SurfaceTexture) : Boolean{
            return false
        }
        override fun onSurfaceTextureUpdated(surface:SurfaceTexture){

        }
    }
    //위와 마찬가지로 val로 취급한다
    val stateCallback : CameraDevice.StateCallback = object : CameraDevice.StateCallback(){
        override fun onOpened(camera : CameraDevice){//카메라 화면이 실행됨에 따라 불러짐
            Log.e(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }
        override fun onDisconnected(p0: CameraDevice?) {
            cameraDevice!!.close()
        }

        override fun onError(p0: CameraDevice?, p1: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    val captureCallbackListener : CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback(){
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            super.onCaptureCompleted(session, request, result)
            //왠지 모르겠지만 RegistrationPart_SP.this가 context에 들어가면 오류가 난다.
            Toast.makeText(applicationContext, "Saved:" + file, Toast.LENGTH_SHORT).show()
            createCameraPreview()
        }
    }
    //완전 일반 함수들인데 중복으로 쓰이지 않으면 빼겠다.
    protected fun startBackgroundThread(){
        mBackgroundThread = HandlerThread("Camera Background");
        mBackgroundThread!!.start()
    }
    protected fun stopBackgroundThread(){
        mBackgroundThread!!.quitSafely()
        try{
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        }catch (e : InterruptedException){
            e.printStackTrace()
        }
    }

    protected fun takePicture(){
        if (null == cameraDevice){
            Log.e(TAG,"cameraDevice is null")
            return;
        }
        val manager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try{
            val characteristics : CameraCharacteristics = manager.getCameraCharacteristics(cameraDevice!!.getId())
            var jpegSizes : Array<Size>? = null
            if (characteristics != null){
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG)
            }
            var width : Int = 640
            var height : Int = 480
            if (jpegSizes != null && 0 < jpegSizes.size){
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader : ImageReader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1)
            val outputSurfaces : List<Surface> = ArrayList<Surface>(2)
            outputSurfaces.plus(reader.surface)
            outputSurfaces.plus(textureView!!.surfaceTexture)

            val captureBuilder : CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
            val file : File = File(Environment.getExternalStorageDirectory().toString() + "pic.jpg")
            val readerListener : ImageReader.OnImageAvailableListener  = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader : ImageReader) {
                    var image : Image? = null
                    try{
                        image = reader.acquireLatestImage()
                        val buffer : ByteBuffer = image.planes[0].buffer
                        var bytes : ByteArray = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        save(bytes)
                    }catch (e : FileNotFoundException){
                        e.printStackTrace()
                    }catch (e : IOException){
                        e.printStackTrace()
                    }finally {
                        if (image != null){
                            image.close()
                        }
                    }
                }
                //https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-throws/index.html
                @Throws(IOException::class)
                private fun save (bytes : ByteArray) {
                    var output : OutputStream? = null
                    try{
                        output = FileOutputStream(file)
                        output.write(bytes)
                    }finally {
                        if (output != null){
                            output.close()
                        }
                    }
                }
            }

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener : CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback(){
                override fun onCaptureCompleted(session : CameraCaptureSession, request: CaptureRequest, result : TotalCaptureResult){
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(this@RegistrationPart_SP, "Saved:" + file, Toast.LENGTH_SHORT).show()
                    createCameraPreview()
                }
            }
            //매개변수 3개인 함수 불러온 것
            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback(){
                override fun onConfigured(session : CameraCaptureSession){
                    try{
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    }catch (e : CameraAccessException){
                        e.printStackTrace()
                    }
                }
                override fun onConfigureFailed(session : CameraCaptureSession){
                }
            }, mBackgroundHandler)

        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
    }

    protected fun createCameraPreview(){
        try{
            val texture : SurfaceTexture = textureView!!.surfaceTexture
            //imageDimension의 값은 null을 가질 수도 있다.
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface : Surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback(){
                override fun onConfigured(cameraCaptureSession : CameraCaptureSession) {
                    //카메라가 closed되어 있으면
                    if (cameraDevice == null){
                        return
                    }
                    //session이 준비가 되어있으면 preview를 보여준다.
                    cameraCaptureSessions = cameraCaptureSession
                    updatePreview()
                }
                override fun onConfigureFailed(p0: CameraCaptureSession?) {
                    Toast.makeText(this@RegistrationPart_SP,"Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, null)
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
    }

    private fun openCamera(){
        val manager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(TAG,"is camera open")

        try{
            cameraId = manager.cameraIdList[0]
            val characteristics : CameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val map : StreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            //카메라 권한 확인
            val hasCameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            val hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (hasCameraPermission == PackageManager.PERMISSION_GRANTED && hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
            }//이미 권한을 가지고 있음
            else {
                //권한 요청
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE)
            }

            manager.openCamera(cameraId, stateCallback, null)
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
        Log.e(TAG, "openCamera X")
    }

    protected  fun updatePreview(){
        if (cameraDevice == null){
            Log.e(TAG, "updatePreview error, return")
        }
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try{
            cameraCaptureSessions!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, mBackgroundHandler)
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
    }

    private fun closeCamera(){
        if (cameraDevice != null){
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (imageReader != null){
            imageReader!!.close()
            imageReader = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                //앱을 꺼버린다.
                //Toast.makeText(this@RegistrationPart_SP, "부에엥 권한줭", Toast.LENGTH_LONG).show();
                //finish();
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        startBackgroundThread()
        if (textureView!!.isAvailable){
            openCamera()
        }else{
            //setSurfaceTextureListener와 surfaceTextureListener가 같은지 테스트
            textureView!!.surfaceTextureListener
        }
    }

    protected override fun onPause() {
        Log.e(TAG, "멈춤")
        //closeCamera()
        stopBackgroundThread()
        super.onPause()
    }
}