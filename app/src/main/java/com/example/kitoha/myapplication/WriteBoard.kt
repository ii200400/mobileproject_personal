package com.example.kitoha.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_write_board.*
import java.util.*


class WriteBoard : AppCompatActivity() {

    val database:FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRef:DatabaseReference = database.getReference()
    val mStorageRef : StorageReference = FirebaseStorage.getInstance().getReference()
    val user: FirebaseUser = FirebaseAuth.getInstance().getCurrentUser() as FirebaseUser

    var names = arrayListOf<String>()
    var adapter : ArrayAdapter<String>? =  null
    var bitmap : Bitmap? = null

    lateinit var title:String
    lateinit var text:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_board)

        val titleinfo:EditText=findViewById(R.id.titleinfo)
        val textinfo:EditText=findViewById(R.id.textinfo)
        val up_btn:Button=findViewById(R.id.up_write)

        adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,names)
        adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        imageView.setImageBitmap(bitmap)
        imageNames.adapter = adapter

        up_btn.setOnClickListener {
            val intent: Intent = Intent(this,Board::class.java)
            title=titleinfo.getText().toString()
            text=textinfo.getText().toString()
            var info:BoardData= BoardData()
            info.title=title
            val cal:Calendar= Calendar.getInstance()
            val year:String=cal.get(Calendar.YEAR).toString()
            val month:String=(cal.get(Calendar.MONTH)+1).toString()
            val day:String=cal.get(Calendar.DAY_OF_MONTH).toString()
            val hour:String=cal.get(Calendar.HOUR_OF_DAY).toString()
            val min:String=cal.get(Calendar.MINUTE).toString()
            val sec:String=cal.get(Calendar.SECOND).toString()
            val now_data:String=year+"-"+month+"-"+day+" "+hour+":"+min+":"+sec
            info.date=now_data
            info.description=text
            info.user_id=user.uid
            info.imagename=imageNames.selectedItem.toString()

            Log.d("디버깅 중",title)
            myRef.child("BoardInform").push().setValue(info)

           // myRef.setValue(title)
           // myRef.child(title).setValue(text)
            intent.putExtra("title",title)
            startActivity(intent)
        }


        // Read from the database
        download_button.setOnClickListener{
            Toast.makeText(this, "사진을 다운로드 합니다.", Toast.LENGTH_SHORT).show()
            val imageNmae = imageNames.selectedItem.toString()
            val downloadRef : StorageReference = mStorageRef.child("images/" + imageNmae + ".jpg")

            val ONE_MEGABYTE : Long = 1024 * 1024
            downloadRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener({ bytes->
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                        imageView.setImageBitmap(bitmap)

                        Toast.makeText(this, "사진 다운로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    })
                    .addOnFailureListener({
                    })
        }

        myRef.addValueEventListener(object : ValueEventListener {  //바뀔때 마다 호출
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                names.clear()
                for (snapshot in dataSnapshot.children) {
                    names.add(snapshot.key.toString())
                }

                adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // 읽기에 실패
                Log.w("--------", "Failed to read value.", error.toException())
            }
        })
    }

}
