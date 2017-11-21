package com.vuforia.samples.VuforiaSamples

import android.app.Activity
import android.content.*
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.TextureView
import android.widget.Button
import android.util.SparseIntArray
import android.view.Surface
import android.os.HandlerThread
import android.media.ImageReader
import android.os.Handler
import android.util.Size
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.*
import java.nio.ByteBuffer
import java.util.*


/**
 * Created by im on 2017-09-19.
 */
class SurfaceCamera : AppCompatActivity() {
    private val TAG = "AndroidCameraApi"
    private val PREVIEW_CODE = 1

    val answer_intent : Intent = Intent()
    var thumb : Bitmap? = null
    var textureView : TextureView? = null
    var takePictureButton : Button? = null
    private var cameraId : String = "" //openCamera에서 정의
    private var ORIENTATIONS : SparseIntArray = object : SparseIntArray(){} //카메라 각도 설정(?)
    protected var cameraDevice : CameraDevice? = null
    protected var cameraCaptureSessions: CameraCaptureSession? = null
    //protected var captureRequest: CaptureRequest? = null 변수가 안쓰였다.
    protected var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private var map : StreamConfigurationMap? = null
    //private val mFlashSupported: Boolean = false ..?왜 있을까;
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_api)

        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)

        textureView = findViewById(R.id.texture)
        takePictureButton = findViewById(R.id.btn_takepicture)

        textureView!!.setSurfaceTextureListener(textureListener)

        takePictureButton!!.setOnClickListener{
            takePicture()
        }
    }
    //textureView에 카메라 화면 stream
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

    val stateCallback : CameraDevice.StateCallback = object : CameraDevice.StateCallback(){
        //카메라 화면 stream
        override fun onOpened(camera : CameraDevice){
            Log.e(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }
        //에러가 나거나 카메라에서 나가면 카메라 장치 종료
        override fun onDisconnected(p0: CameraDevice?) {
            cameraDevice!!.close()
        }
        override fun onError(p0: CameraDevice?, p1: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }
    /*//TODO 언제 호출되는 것인지?
    val captureCallbackListener : CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback(){
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            super.onCaptureCompleted(session, request, result)
            Toast.makeText(this@RegistrationPart_SP, "Saved:" + file, Toast.LENGTH_SHORT).show()
            createCameraPreview()
        }
    }*/
    //thread관련 함수
    protected fun startBackgroundThread(){
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.getLooper())
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
    //사진찍기의 중심함수
    protected fun takePicture(){
        if (null == cameraDevice){
            //카메라 장치가 꺼져있는 경우
            Log.e(TAG,"cameraDevice is null")
            return
        }
        val manager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try{
            //카메라 장치의 기능을 query한다는데..?
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
                Log.e("---------", width.toString() + " " + height.toString())
            }
            //이미지의 크기와 형식을 묘사(?)한다. Create a new reader for images of the desired size and format. PixelFormat.RGBX_8888
            var num : Int = 1
            var loop : Boolean = true
            var reader : ImageReader? = null
            while (loop) {
                try {
                    when (num) {
                        1 -> reader = ImageReader.newInstance(width, height, 0x1, 2)
                        2 -> reader = ImageReader.newInstance(width, height, 0x2, 2)
                        3 -> reader = ImageReader.newInstance(width, height, 0x3, 2)
                        4 -> reader = ImageReader.newInstance(width, height, 0x4, 2)
                        else ->{
                            Toast.makeText(this@SurfaceCamera,"format이 이상함", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    loop = false

                } catch (e: Exception) {
                    e.printStackTrace()
                    num += 1
                }
            }
            val outputSurfaces : ArrayList<Surface> = ArrayList<Surface>(2)
            outputSurfaces.add(reader!!.surface)
            outputSurfaces.add(Surface(textureView!!.surfaceTexture))

            val captureBuilder : CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            //TODO 카메라 회전을 고려하여 이미지 스트림
            val rotation = (this@SurfaceCamera as Activity).windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))

            //외부 저장소에 읽고 쓰는 것이 가능한지 확인
            val state : String = Environment.getExternalStorageState()
            if(!Environment.MEDIA_MOUNTED.equals(state)) {
                Log.e(TAG, "Storage Permission need")
                return
            }

            //http://myandroidarchive.tistory.com/6
            val readerListener : ImageReader.OnImageAvailableListener  = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader : ImageReader) {
                    var image : Image? = null
                    var bitmap : Bitmap? = null
                    try{
                        //put으로 key : value로 값을 values에 넣는다.
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, "사진1")
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, "사진2")
                        values.put(MediaStore.Images.Media.DESCRIPTION, "제발..")
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

                        //갤러리의 상단에 넣어주기
                        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

                        //갤러리 permission이 있으면 갤러리에서 values값을 가지는 이미지 볼 수 있도록 하기
                        val url = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        image = reader.acquireLatestImage()

                        if(image != null) {
                            val buffer: ByteBuffer = image.planes[0].buffer
                            val imageOut : OutputStream = contentResolver.openOutputStream(url)

                            bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                            val resize : Bitmap = Bitmap.createScaledBitmap(bitmap, 300, 400, true);

                            bitmap.copyPixelsFromBuffer(buffer)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOut)

                            answer_intent.putExtra("image", bitmap)
                            imageOut.flush()
                            imageOut.close()

                            val id : Long = ContentUris.parseId(url)
                            val miniThumb : Bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null)

                            val matrix = Matrix()  //보여주는 방법을 지정(이미지를 늘리거나 크기 제한, 비율을 지정 가능)
                            matrix.setScale(0.5f, 0.5f)

                            //저장하는 방법을 지정(이미지를 자르고 어떤 형식(png)으로 저장할지)
                            thumb = Bitmap.createBitmap(miniThumb, miniThumb.width / 4, miniThumb.height / 4, miniThumb.width / 2, miniThumb.height / 2, matrix, true)
                            val values2 = ContentValues(4)
                            values2.put(MediaStore.Images.Thumbnails.KIND, MediaStore.Images.Thumbnails.MICRO_KIND)
                            values2.put(MediaStore.Images.Thumbnails.IMAGE_ID, id.toInt())
                            values2.put(MediaStore.Images.Thumbnails.HEIGHT, thumb!!.height)
                            values2.put(MediaStore.Images.Thumbnails.WIDTH, thumb!!.width)

                            //val url2 = contentResolver.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values2)
                            val url2 = contentResolver.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values2)
                            val thumbOut : OutputStream = contentResolver.openOutputStream(url2)
                            thumb!!.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut)
                            imageOut.flush()
                            thumbOut.close()

                            //val input : InputStream = contentResolver.openInputStream(url)

                            //pickedImage.setImageBitmap(thumb)
                        }
                    }catch (e : FileNotFoundException){
                        e.printStackTrace()
                    }catch (e : IOException){
                        e.printStackTrace()
                    }finally {
                        if (bitmap != null) {
                            bitmap.recycle()
                        }

                        if (image != null){
                            image.close()
                        }
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            //사진을 찍으면 불리는 함수
            val captureListener : CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback(){
                override fun onCaptureCompleted(session : CameraCaptureSession, request: CaptureRequest, result : TotalCaptureResult){
                    super.onCaptureCompleted(session, request, result)

                    createCameraPreview()
                    if (thumb != null){
                        val preview_intent = Intent(this@SurfaceCamera, PreviewImage::class.java)
                        preview_intent.putExtra("image", thumb)
                        startActivityForResult(preview_intent, PREVIEW_CODE)
                    }
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
        return
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
                    Toast.makeText(this@SurfaceCamera,"Configuration change", Toast.LENGTH_SHORT).show()
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
            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            imageDimension = map!!.getOutputSizes(SurfaceTexture::class.java)[0]

            manager.openCamera(cameraId, stateCallback, null)
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }catch (e : SecurityException){
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
    //카메라 종료 구현만하고 쓰지는 않는다.
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

    override fun onPause() {
        Log.e(TAG, "멈춤")
        //closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            PREVIEW_CODE->{
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val confirm : Boolean = data.extras.get("confirm") as Boolean
                    if (confirm){
                        setResult(Activity.RESULT_OK, answer_intent)
                        finish()
                    }
                }
            }
        }
    }
}