package com.example.im.mobileproject

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * Created by kitoha on 2017-09-15.
 */
class Fragment1:Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       var rootView=inflater?.inflate(R.layout.fragment,container,false)
        return rootView
    }

}