package com.vuforia.samples.VuforiaSamples

import android.graphics.Camera
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button

class Picture() : AppCompatActivity(), SurfaceHolder.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        setTitle("사진 찍기")
        var IsTrue:Boolean=false
        var camera:Camera?=null
        var surfaceview:SurfaceView=findViewById(R.id.surface1)
        var btn:Button=findViewById(R.id.button)
        var surfaceholder:SurfaceHolder?=null

        getWindow().setFormat(PixelFormat.UNKNOWN)
        surfaceholder=surfaceview.holder
        surfaceholder.addCallback(this)
        surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        btn.setOnClickListener {


        }

    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
