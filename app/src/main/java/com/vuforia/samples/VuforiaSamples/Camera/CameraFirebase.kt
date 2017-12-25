package com.vuforia.samples.VuforiaSamples.Camera

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by im on 2017-12-24.
 */
class CameraFirebase(val context : Context) {
    private val mStorageRef : StorageReference = FirebaseStorage.getInstance().getReference()
    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("images")

    //파이어 베이스에 사진 올리기
    fun sendPicture(uri : Uri) : Boolean{
        var result : Boolean = false
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val riversRef : StorageReference = mStorageRef.child("images/" + timeStamp + ".jpg")
        riversRef.putFile(uri!!)
                .addOnSuccessListener({ taskSnapshot ->
                    // URL과 이름을 다운로드한 이미지로부터 가져온다.
                    val downloadUrl : Uri? = taskSnapshot.downloadUrl
                    val name : String? = taskSnapshot.metadata!!.getName()

                    //Realtime Database에 이름과 uri를 기록한다. (목록생성을 위함)
                    writeRealtimeDB(name, downloadUrl.toString())

                    result = true
                    Toast.makeText(context, "사진 전송에 성공했습니다.", Toast.LENGTH_SHORT).show()
                })
                .addOnFailureListener({
                    // Handle unsuccessful uploads
                    result=false
                    Toast.makeText(context, "사진 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                })
        //progress dialog사용하는 경우
//                    .addOnProgressListener({ taskSnapshot->
//                        // progress percentage
//                        val progress : Double = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount()
//
//                        // percentage in progress dialog
//                        progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")
//                    })

        return result
    }

    //Realtime DB에 Storage이미지의 사진과 uri저장하기
    fun writeRealtimeDB(name : String?, uri : String?){
        val info = UploadImageInfo(name, uri)
        val key = mDBRef.push().getKey()

        mDBRef.child(key).setValue(info)
    }

    mDBRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            for (snapshot in dataSnapshot.children) {
                Log.d("---------", "ValueEventListener : " + snapshot.key.toString())
                Log.d("---------", "ValueEventListener : " + snapshot.child("name").value.toString())
                Log.d("---------", "ValueEventListener : " + snapshot.child("uri").value.toString())
//                accounts.add(CheckingAccount(snapshot.key.toString(), snapshot.child("balance").value.toString().toInt(),
//                        snapshot.child("interest").value.toString().toDouble()) as Account)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    })
}