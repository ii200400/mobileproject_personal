package kr.ac.kau.pcassemblyhelper

/*
import android.net.Uri
import java.io.File
import android.support.annotation.NonNull
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FileDownloadTask

/**
 * Created by J on 2017-11-12.
 */

// 사용할 엑티비티의 멤버변수로
// lateinit var mStorageRef : StorageReference
// onCreate 함수에
// mStorageRef = FirebaseStorage.getInstance().reference
// 함수호출시 인자로 넘겨주면 됨
class Firebase
{
    // 파이어베이스 storage
    // 파일 업로드하는 함수
    public fun uploadFile(fname : String, mStorageRef : StorageReference)
    {
        var file : Uri = Uri.fromFile(File(fname))
        val riversRef = mStorageRef.child("images/rivers.jpg")
        var testRef = mStorageRef.child(fname)

        testRef.putFile(file)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    // Get a URL to the uploaded content
                    val downloadUrl = taskSnapshot.downloadUrl

                    Log.d("test12345", "upload success")
                })
                .addOnFailureListener(OnFailureListener {
                    // Handle unsuccessful uploads
                    // ...

                    Log.d("test12345", "upload fail")
                })
    }

    // 파이어베이스 storage
    // 파일 다운로드하는 함수
    fun downloadFile(mStorageRef : StorageReference) : String
    {
        val riversRef = mStorageRef.child("images/rivers.jpg")
        val localFile = File.createTempFile("images", "jpg")

        var fname : String = ""

        riversRef.getFile(localFile)
                .addOnSuccessListener(OnSuccessListener<FileDownloadTask.TaskSnapshot> {
                    // Successfully downloaded data to local file
                    // ...
                    Log.d("test12345", "download success")

                }).addOnFailureListener(OnFailureListener {
            // Handle failed download
            // ...

            Log.d("test12345", "download fail")
        })

        return fname;
    }
}
*/