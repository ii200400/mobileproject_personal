package com.vuforia.samples.VuforiaSamples

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import kotlinx.android.synthetic.main.activity_registration.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import java.io.IOException



class RegistrationPart : AppCompatActivity() {
    private val PERMISSIONS_CAMERA_CODE = 100
    private val PERMISSIONS_INTERNET_CODE = 99
    private val CAMERA_REQUEST_MODE = 100
    private val GALLERY_REQUEST_MODE = 98

    private val mStorageRef : StorageReference = FirebaseStorage.getInstance().getReference()
    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("images")
    var uri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setTitle("부품 등록")

        val spin1: Spinner = findViewById(R.id.spinner1)
        val value = arrayListOf("카테고리","메인보드","CPU","RAM","메모리")

        spin1.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,value)

        //사진 찍기
        button_camera.setOnClickListener {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                    val permissionList : Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    val needpermission : Boolean = permissioncheck(permissionList, PERMISSIONS_CAMERA_CODE)
                    if (needpermission){
                        val intent_picture: Intent = Intent(this@RegistrationPart, SurfaceCamera::class.java)
                        startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
                    }else{
                        //카메라, 저장소 이미지 권한 요청
                        ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_CAMERA_CODE)
                    }
                }else{
                    val intent_picture: Intent = Intent(this@RegistrationPart, SurfaceCamera::class.java)
                    startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
                }
            }else {
                Toast.makeText(this, "카메라 장치가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        //앨범에서 사진 가져오기
        button_callgallery.setOnClickListener(){
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT//
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_MODE)
        }

        //사진 등록하기
        button_upload.setOnClickListener {
            //TODO apk 23 이상 핸드폰에서 확인 필요
            val permissionList : Array<String> = arrayOf(Manifest.permission.INTERNET)
            val needpermission : Boolean = permissioncheck(permissionList, PERMISSIONS_INTERNET_CODE)
            if (needpermission){
                //인터넷 권한 요청
                ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_INTERNET_CODE)
            }else{
                Toast.makeText(this, "사진 전송을 시도합니다.", Toast.LENGTH_SHORT).show()
                sendPicture()
            }
        }
    }

    //파이어 베이스에 사진 올리기
    fun sendPicture(){
        if (uri!=null){
            val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val riversRef : StorageReference = mStorageRef.child("images/" + timeStamp + ".jpg")
            riversRef.putFile(uri!!)
                    .addOnSuccessListener({ taskSnapshot ->
                        // URL과 이름을 다운로드한 이미지로부터 가져온다.
                        val downloadUrl : Uri? = taskSnapshot.downloadUrl
                        val name : String? = taskSnapshot.metadata!!.getName()
                        //Realtime Database에 이름과 uri를 기록한다. (목록생성을 위함)
                        writeRealtimeDB(name, downloadUrl.toString())

                        uri = null
                        pickedImage.setImageBitmap(null)
                        Toast.makeText(this, "사진 전송에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    })
                    .addOnFailureListener({
                        // Handle unsuccessful uploads
                        Toast.makeText(this, "사진 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    })
            //progress dialog사용하는 경우
//                    .addOnProgressListener({ taskSnapshot->
//                        // progress percentage
//                        val progress : Double = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount()
//
//                        // percentage in progress dialog
//                        progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")
//                    })
        }else{
            Toast.makeText(this, "사진을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    //Realtime DB에 Storage이미지의 사진과 uri저장하기
    fun writeRealtimeDB(name : String?, uri : String?){
        val info = UploadImageInfo(name, uri)
        val key = mDBRef.push().getKey()

        mDBRef.child(key).setValue(info)
    }

    // 런타임 권한 확인
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

    //권한 거부, 동의에 따른 코드
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            //카메라
            PERMISSIONS_CAMERA_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    val intent_picture: Intent = Intent(this@RegistrationPart, SurfaceCamera::class.java)
                    startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
                } else {
                    Toast.makeText(this, "카메라와 저장소 권한이 있어야 실행 가능합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            //인터넷
            PERMISSIONS_INTERNET_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendPicture()
                } else {
                    Toast.makeText(this, "인터넷 권한을 해주셔야 사진을 보낼 수 있습니다.", Toast.LENGTH_SHORT).show()
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
                        uri = data.data;
                        var bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
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
}