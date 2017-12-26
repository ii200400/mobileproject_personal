package kr.ac.kau.pcassemblyhelper.Camera

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import java.io.IOException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kr.ac.kau.pcassemblyhelper.R
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.ui_registration.*

class RegistrationPart : AppCompatActivity() {
    private val PERMISSIONS_CAMERA_CODE = 100
    private val PERMISSIONS_INTERNET_CODE = 99
    private val PERMISSIONS_GALLERY_CODE = 98
    private val CAMERA_REQUEST_MODE = 100
    private val GALLERY_REQUEST_MODE = 98

    private val mStorageRef : StorageReference = FirebaseStorage.getInstance().getReference()
    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("images")

    val bitmapController : BitmapController = BitmapController()
    var uri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_registration)
        setTitle("부품 등록")

        //사진 찍기 및 퍼미션 요청
        button_camera.setOnClickListener {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                    val permissionList : Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    val needpermission : Boolean = permissioncheck(permissionList, PERMISSIONS_CAMERA_CODE)
                    if (needpermission){
                        //카메라, 저장소 이미지 권한 요청
                        ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_CAMERA_CODE)
                    }else{
                        callPreview()
                    }
                }else{
                    callPreview()
                }
            }else {
                Toast.makeText(this, "카메라 장치가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        //인터넷 퍼미션 요청
        button_upload.setOnClickListener {
            val permissionList: Array<String> = arrayOf(Manifest.permission.INTERNET)
            val needpermission: Boolean = permissioncheck(permissionList, PERMISSIONS_INTERNET_CODE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                if (needpermission) {
                    //인터넷 권한 요청
                    ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_INTERNET_CODE)
                } else {
                    //사진을 파이어 베이스에 등록 시도
                    Toast.makeText(this, "사진 전송을 시도합니다.", Toast.LENGTH_SHORT).show()
                    prepareFirebase()
                }
            }else{
                ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_INTERNET_CODE)
            }
        }

        //앨범에서 사진 가져오기 (커스텀 아님)
        button_callgallery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                val permissionList : Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val needpermission : Boolean = permissioncheck(permissionList, PERMISSIONS_GALLERY_CODE)
                if (needpermission){
                    //카메라, 저장소 이미지 권한 요청
                    ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_GALLERY_CODE)
                }else{
                    callAlbum()
                }
            }else{
                callAlbum()
            }
        }
    }

    //앨범 intent부르기
    fun callAlbum(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_MODE)
    }
    //
    fun callPreview(){
        val intent_picture: Intent = Intent(this, SurfaceCamera::class.java)
        startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
    }

    //파이어 베이스 동기화 싫다.
    fun sendPicture(){
        val progressDialog : ProgressDialog = ProgressDialog(this)
        var imagename : String = imageName.text.toString()
        //아무것도 없거나 공백만 있는 경우
        if(imagename == null || imagename.replace(" ","") == ""){
            imagename = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        }
        val uploadRef : StorageReference = mStorageRef.child("images/" + imagename + ".jpg")
        uploadRef.putFile(uri!!)
                //사진 전송이 성공한 경우
                .addOnSuccessListener({ taskSnapshot ->
                    // URL과 이름을 다운로드한 이미지로부터 가져온다.
                    val downloadUrl : Uri? = taskSnapshot.downloadUrl
                    val name : String? = taskSnapshot.metadata!!.getName()!!.split(".")[0]

                    //Realtime Database에 이름과 uri를 기록한다. (목록생성을 위함)
                    writeRealtimeDB(name, downloadUrl.toString())

                    imageName.text = null
                    uri = null
                    pickedImage.setImageBitmap(null)
                    progressDialog.dismiss()
                    Toast.makeText(this, "사진 전송에 성공했습니다.", Toast.LENGTH_SHORT).show()
                })
                //사진 전송이 실패한 경우
                .addOnFailureListener({
                    // Handle unsuccessful uploads
                    Toast.makeText(this, "사진 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                })
                //progress dialog로 진행상태 보여주기
                .addOnProgressListener({ taskSnapshot->
                    progressDialog.show()
                    // progress percentage
                    val progress : Double = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount()

                    // percentage in progress dialog
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")
                })
    }

    //Realtime DB에 Storage이미지의 사진과 uri저장하기
    fun writeRealtimeDB(name : String?, uri : String?){
        mDBRef.child(name).setValue(uri)
    }

    //퍼미션 권한 거부, 동의에 따른 코드
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            //카메라
            PERMISSIONS_CAMERA_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    callPreview()
                } else {
                    Toast.makeText(this, "카메라와 저장소 권한이 있어야 실행 가능합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            //인터넷
            PERMISSIONS_INTERNET_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareFirebase()
                } else {
                    Toast.makeText(this, "인터넷 권한을 해주셔야 사진을 보낼 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
            //저장소
            PERMISSIONS_GALLERY_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callAlbum()
                } else {
                    Toast.makeText(this, "앨범 접근 권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //액티비티에서 돌아오고 난후
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            //카메라로 사진을 찍은 후
            CAMERA_REQUEST_MODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    uri = data.extras.get("uri") as Uri
                    val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
                    pickedImage.setImageBitmap(bitmap)
                }
            }
            //갤러리에서 사진을 가져온 후
            GALLERY_REQUEST_MODE->{
                if (resultCode == Activity.RESULT_OK && data != null) {
                    try {
                        uri = data.data
                        val bytes : ByteArray =  getContentResolver().openInputStream(uri).readBytes()
                        val bitmap = bitmapController.smallerBitmap(bytes, 1400, 1400)

                        pickedImage.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            else -> {
                Toast.makeText(this, "unrecognized request", Toast.LENGTH_SHORT).show()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    // 런타임 권한 확인(줄이 길어져서 for문을 이용한 함수로 대체)
    fun permissioncheck(permissionList : Array<String>, requestCode : Int) : Boolean {
        var needpermission = false
        for (i in permissionList) {
            if (ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED) {
                needpermission = true
                break
            }
        }
        return needpermission
    }

    //uri가 null 여부 확인 후 firebase접속
    fun prepareFirebase(){
        if (uri!=null){
            sendPicture()
        }else{
            Toast.makeText(this, "사진을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}