package com.vuforia.samples.VuforiaSamples

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import kotlinx.android.synthetic.main.activity_registration.*
import android.widget.Toast
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.support.annotation.NonNull
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import java.text.SimpleDateFormat
import java.util.*


class RegistrationPart : AppCompatActivity() {
    private val PERMISSIONS_CAMERA_CODE = 100
    private val PERMISSIONS_INTERNET_CODE = 99
    private val CAMERA_REQUEST_MODE = 100


    private val mStorageRef : StorageReference = FirebaseStorage.getInstance().getReference()
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

        //사진 등록하기
        button_upload.setOnClickListener {
            //TODO apk 최소사양 확인해보기
            val permissionList : Array<String> = arrayOf(Manifest.permission.INTERNET)
            val needpermission : Boolean = permissioncheck(permissionList, PERMISSIONS_INTERNET_CODE)
            if (needpermission){
                sendPicture()
            }else{
                //인터넷 권한 요청
                ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_INTERNET_CODE)
            }
        }
    }

    //파이어 베이스에 사진 올리기
    fun sendPicture(){
        if (uri!=null){
            val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val riversRef : StorageReference = mStorageRef.child("images/" + timeStamp + ".jpg")
            riversRef.putFile(uri!!)
                    .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        // Get a URL to the uploaded content
                        val downloadUrl = taskSnapshot.downloadUrl

                        uri = null
                        pickedImage.setImageBitmap(null)
                        Toast.makeText(this, "사진 전송에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    })
                    .addOnFailureListener(OnFailureListener {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(this, "사진 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    })
        }else{
            Toast.makeText(this, "사진을 먼저 찍어주세요", Toast.LENGTH_SHORT).show()
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_CAMERA_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                val intent_picture: Intent = Intent(this@RegistrationPart, SurfaceCamera::class.java)
                startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
            }else{
                Toast.makeText(this, "카메라와 저장소 권한이 필요한데 없군요.", Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode == PERMISSIONS_INTERNET_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendPicture()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_MODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    uri = data.extras.get("uri") as Uri
                    val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
                    pickedImage.setImageBitmap(bitmap)
                }
            }
            else -> {
                Toast.makeText(this, "unrecognized request", Toast.LENGTH_SHORT).show()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}