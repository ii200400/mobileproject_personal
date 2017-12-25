package kr.ac.kau.pcassemblyhelper.Camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import kotlinx.android.synthetic.main.activity_registration.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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

class RegistrationPart : AppCompatActivity() {
    private val PERMISSIONS_CAMERA_CODE = 100
    private val PERMISSIONS_INTERNET_CODE = 99
    private val CAMERA_REQUEST_MODE = 100
    private val GALLERY_REQUEST_MODE = 98

    private val connectFirebase : CameraFirebase = CameraFirebase(this)
    private val mStorageRef : StorageReference = FirebaseStorage.getInstance().getReference()
    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("images")

    var uri : Uri? = null
    var names = arrayListOf<String>()
    var adapter : ArrayAdapter<String>? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setTitle("부품 등록")

        //spinner에 값 넣기
        adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,names)
        adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        connectFirebase.initList(names, adapter!!)

        //TODO spinner가 슬라이드가 가능하도록
        spinner1.adapter = adapter

        //사진 찍기 및 퍼미션 요청
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

        //앨범에서 사진 가져오기 (커스텀 아님)
        button_callgallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_MODE)
        }

        //사진을 파이어 베이스에 등록하기 및 인터넷 퍼미션 요청
        button_upload.setOnClickListener {
            //TODO apk 23 이상 핸드폰에서 확인 필요
            val permissionList : Array<String> = arrayOf(Manifest.permission.INTERNET)
            val needpermission : Boolean = permissioncheck(permissionList, PERMISSIONS_INTERNET_CODE)
            if (needpermission){
                //인터넷 권한 요청
                ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_INTERNET_CODE)
            }else{
                Toast.makeText(this, "사진 전송을 시도합니다.", Toast.LENGTH_SHORT).show()
                prepareFirebase()
            }
        }

        //파이어 베이스에서 사진 로드하기
        button_firebase.setOnClickListener{
            Toast.makeText(this, "사진을 다운로드 합니다.", Toast.LENGTH_SHORT).show()
            val imageNmae = spinner1.selectedItem.toString()
            val downloadRef : StorageReference = mStorageRef.child("images/" + imageNmae + ".jpg")

            val ONE_MEGABYTE : Int = 1024 * 1024
            downloadRef.getBytes(ONE_MEGABYTE.toLong())
                    .addOnSuccessListener({ bytes->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                        pickedImage.setImageBitmap(bitmap)

                        Toast.makeText(this, "사진 다운로드에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    })
                    .addOnFailureListener({
                        Toast.makeText(this, "사진 다운로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    })
        }
    }

    //파이어 베이스 동기화 싫다.
    fun sendPicture(){
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val uploadRef : StorageReference = mStorageRef.child("images/" + timeStamp + ".jpg")
        uploadRef.putFile(uri!!)
                .addOnSuccessListener({ taskSnapshot ->
                    // URL과 이름을 다운로드한 이미지로부터 가져온다.
                    val downloadUrl : Uri? = taskSnapshot.downloadUrl
                    val name : String? = taskSnapshot.metadata!!.getName()!!.split(".")[0]

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
                    val intent_picture: Intent = Intent(this@RegistrationPart, SurfaceCamera::class.java)
                    startActivityForResult(intent_picture, CAMERA_REQUEST_MODE)
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