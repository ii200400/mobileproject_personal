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
    fun init(translucent : Boolean, depth : Int, stencil : Int)
    {
        // By default GLSurfaceView tries to find a surface that is as close
        // as possible to a 16-bit RGB frame buffer with a 16-bit depth buffer.
        // This function can override the default values and set custom values.

        // By default, GLSurfaceView() creates a RGB_565 opaque surface.
        // If we want a translucent one, we should change the surface's
        // format here, using PixelFormat.TRANSLUCENT for GL Surfaces
        // is interpreted as any 32-bit surface with alpha by SurfaceFlinger.

        // If required set translucent format to allow camera image to
        // show through in the background
        // 카메라 화면을 사용하기 위한 포멧을 설정한다.
        if (translucent)
        {
            this.holder.setFormat(PixelFormat.TRANSLUCENT)
        }

        // Setup the context factory for 2.0 rendering
        // egl 2.0 렌더링을 위해 context factory를 설정한다.
        setEGLContextFactory(ContextFactory())

        // We need to choose an EGLConfig that matches the format of
        // our surface exactly. This is going to be done in our
        // custom config chooser. See ConfigChooser class definition
        // below.
        // surface 포멧에 맞는 egl 설정을 한다.
        if (translucent)
        {
            setEGLConfigChooser(ConfigChooser(8, 8, 8, 8, depth, stencil))
        }
        else
        {
            setEGLConfigChooser(ConfigChooser(5, 6, 5, 0, depth, stencil))
        }
    }

    // Checks the OpenGL error.
    // openGL 에러 체크하는 함수
    fun checkEglError(prompt : String, egl : EGL10)
    {
        var error : Int

        // 에러를 확인하고 해당하는 에러를 로그로 출력한다.
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS)
        {
            Log.e("AppGLView", String.format("%s: EGL error: 0x%x", prompt, error))
        }
    }

    // Creates OpenGL contexts.
    // AppGLView 클래스의 함수를 사용하기 편리하게 inner 클래스로 선언
    // openGL의 context를 생성한다.
    private inner class ContextFactory : GLSurfaceView.EGLContextFactory
    {
        // EGL context의 클라이언트 버전
        private val EGL_CONTEXT_CLIENT_VERSION = 0x3098

        // context 생성하는 함수
        override fun createContext(egl: EGL10, display: EGLDisplay,
                                   eglConfig: EGLConfig): EGLContext
        {
            val context: EGLContext

            // 디버깅 편의를 위한 로그 출력
            Log.i("AppGLView", "Creating OpenGL ES 2.0 context")

            // egl 에러가 있는지 체크
            checkEglError("Before eglCreateContext", egl)

            // context 생성에 필요한 파라미터
            val attrib_list_gl20 = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)

            // context 생성
            context = egl.eglCreateContext(display, eglConfig,
                    EGL10.EGL_NO_CONTEXT, attrib_list_gl20)

            // egl 에러가 있는지 체크
            checkEglError("After eglCreateContext", egl)

            // 생성된 context 리턴
            return context
        }

        // context를 파괴(삭제)하는 함수
        override fun destroyContext(egl: EGL10, display: EGLDisplay,
                                    context: EGLContext)
        {
            egl.eglDestroyContext(display, context)
        }
    }

}