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

/**
 * Created by kitoha on 2017-10-15.
 */

class Fragment1 : Fragment(){
    internal var activity: Assembly = Assembly()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment1,container, false)

        var fragmentmanager:FragmentManager
        var transaction:FragmentTransaction;
        val btn: Button =  view.findViewById(R.id.button)
        var fragment:Fragment

        fragmentmanager = getActivity().supportFragmentManager
        transaction = fragmentmanager.beginTransaction()

        btn.setOnClickListener {
            fragment=Fragment2()
            transaction.replace(R.id.container,fragment ).addToBackStack(null).commit()
        }

        return view
    }


}
