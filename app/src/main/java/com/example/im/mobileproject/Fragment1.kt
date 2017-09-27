package com.example.im.mobileproject

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * Created by kitoha on 2017-09-19.
 */

class Fragment1 : Fragment() {

    internal var activity2: Assembly? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity2 = getActivity() as Assembly
    }

    override fun onDetach() {
        super.onDetach()
        activity2 = null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootview = inflater!!.inflate(R.layout.fragment, container,false) as ViewGroup

        val button = rootview.findViewById<View>(R.id.button) as Button
        button.setOnClickListener { activity2!!.onFragmentChange(1) }
        return rootview
    }
}
