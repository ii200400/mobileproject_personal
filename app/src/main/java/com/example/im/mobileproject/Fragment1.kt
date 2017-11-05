package com.example.im.mobileproject

import android.os.Bundle
import android.os.TransactionTooLargeException
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView

/**
 * Created by kitoha on 2017-10-15.
 */

class Fragment1 : Fragment() {
    internal var activity: Assembly = Assembly()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment1, container, false)
        var fragmentmanager: FragmentManager
        var transaction: FragmentTransaction;
        val btn: Button = view.findViewById(R.id.button)
        var fragment: Fragment

        val imageview1 : ImageView = view.findViewById(R.id.imageView)
        fragmentmanager = getActivity().supportFragmentManager
        transaction = fragmentmanager.beginTransaction()
        imageview1.setImageResource(R.drawable.intel_i7)
        btn.setOnClickListener {
            fragment = Fragment2()
            transaction.replace(R.id.container, fragment).addToBackStack(null).commit()
        }

        return view
    }

    object imageveiw_data {
        fun change_imageveiw(pos: Int) {
            if(pos==0){

            }
        }
    }


}
