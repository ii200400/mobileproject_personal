package com.vuforia.samples.VuforiaSamples.Camera

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import com.google.firebase.database.*
import java.util.*

/**
 * Created by im on 2017-12-24.
 */
class CameraFirebase(val context : Context) {
    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("images")

    fun initList(names : ArrayList<String>, adapter : ArrayAdapter<String>) {
        mDBRef.addValueEventListener(object : ValueEventListener {  //바뀔때 마다 호출
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                names.clear()
                for (snapshot in dataSnapshot.children) {
                    names.add(snapshot.child("name").value.toString())
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // 읽기에 실패
                Log.w("--------", "Failed to read value.", error.toException())
            }
        })
    }
}