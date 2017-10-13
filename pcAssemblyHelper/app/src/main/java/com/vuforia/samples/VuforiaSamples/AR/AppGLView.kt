package com.vuforia.samples.VuforiaSamples.AR

/**
 * Created by J on 2017-10-13.
 */

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.Log

// Support class for the Vuforia sample applications
// Responsible for setting up and configuring the OpenGL surface view.
// This class does not contain any Vuforia specific code.
// You can use your own OpenGL implementation.
// 이 클래스는 뷰포리아 관련 코드는 없다.
// openGL을 사용하기 위한 클래스이며 openGL의 surface 뷰를 사용한다.
class AppGLView(context : Context) : GLSurfaceView(context)
{

}