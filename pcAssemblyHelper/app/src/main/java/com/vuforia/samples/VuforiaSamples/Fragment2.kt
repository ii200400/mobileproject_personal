package com.vuforia.samples.VuforiaSamples

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class Fragment2 : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView=inflater?.inflate(R.layout.fragment,container,false)
        return rootView
    }
}
