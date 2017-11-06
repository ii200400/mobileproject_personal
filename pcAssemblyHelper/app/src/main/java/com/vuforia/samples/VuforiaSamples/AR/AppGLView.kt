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
        var error : Int = egl.eglGetError()

        // 에러를 확인하고 해당하는 에러를 로그로 출력한다.
        if (error != EGL10.EGL_SUCCESS)
        {
            Log.e("AppGLView", String.format("%s: EGL error: 0x%x", prompt, error))
        }
    }

    // Creates OpenGL contexts.
    // AppGLView 클래스의 함수를 사용하기 편리하게 inner 클래스로 선언
    // openGL의 context를 생성한다.
    inner class ContextFactory : GLSurfaceView.EGLContextFactory
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

    // The config chooser.
    // openGL 사용을 위해 설정을 선택하는 클래스
    // 생성자로 RGB와 알파값, depth값, 개체들간의 구분을 해주는 stencil 크기를 입력받는다.
    inner class ConfigChooser(var mRedSize : Int, var mGreenSize : Int, var mBlueSize : Int,
                              var mAlphaSize : Int, var mDepthSize : Int, var mStencilSize : Int)
        : GLSurfaceView.EGLConfigChooser
    {
        var mValue = IntArray(1)

        // 반드시 오버라이드 해야하는 함수
        // 설정을 선택하는 함수
        override fun chooseConfig(egl: EGL10?, display: EGLDisplay?): EGLConfig?
        {
            // This EGL config specification is used to specify 2.0
            // rendering. We use a minimum size of 4 bits for
            // red/green/blue, but will perform actual matching in
            // chooseConfig() below.
            val EGL_OPENGL_ES2_BIT = 0x0004

            // egl 설정을 위한 배열 설정
            val s_configAttribs_gl20 = intArrayOf(EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4,
                    EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE)

            // 위에서 만든 배열을 egl 설정에 맞게 포멧을 맞춰주는 함수를 리턴
            return getMatchingConfig(egl, display, s_configAttribs_gl20)
        }

        // 위 함수에서 인자만 다른 오버로딩된 함수
        fun chooseConfig(egl: EGL10?, display: EGLDisplay?,
                         configs: Array<EGLConfig?>): EGLConfig?
        {
            // egl 설정이 담긴 배열의 원소를 하나씩 반복
            for (config in configs)
            {
                val d = findConfigAttrib(egl!!, display!!, config!!,
                        EGL10.EGL_DEPTH_SIZE, 0)
                val s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0)

                // We need at least mDepthSize and mStencilSize bits
                // depth와 stencil 크기가 최소인 값을 찾는다.
                if (d < mDepthSize || s < mStencilSize)
                    continue

                // We want an *exact* match for red/green/blue/alpha
                // 정확한 RGB와 알파값을 찾는다.
                val r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0)
                val g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0)
                val b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0)
                val a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0)

                // 정확한 값이면 설정 리턴
                if (r == mRedSize && g == mGreenSize && b == mBlueSize
                        && a == mAlphaSize)
                    return config
            }

            // 해당하는 설정 값이 없으면 null 리턴
            return null
        }

        // 배열을 egl 설정 포맷에 맞게 맞춰주는 함수
        fun getMatchingConfig(egl: EGL10?, display: EGLDisplay?,
                                      configAttribs: IntArray) : EGLConfig?
        {
            // Get the number of minimally matching EGL configurations
            // egl 설정과 맞는 숫자를 얻는다.
            val num_config = IntArray(1)
            egl!!.eglChooseConfig(display, configAttribs, null, 0, num_config)

            // egl 설정과 맞는 설정이 없으면 오류 메세지 출력
            val numConfigs = num_config[0]
            if (numConfigs <= 0)
                throw IllegalArgumentException("No matching EGL configs")

            // Allocate then read the array of minimally matching EGL configs
            // 맞는 egl 설정을 배열에 넣고 읽는다.
            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            egl.eglChooseConfig(display, configAttribs, configs, numConfigs,
                    num_config)

            // Now return the "best" one
            // 포맷을 맞춘 egl 설정을 리턴한다.
            return chooseConfig(egl, display, configs)
        }

        // 인자로 넘겨받은 설정 중 맞는 걸 찾아서 리턴해주는 함수
        fun findConfigAttrib(egl: EGL10, display: EGLDisplay,
                                     config: EGLConfig, attribute: Int, defaultValue: Int): Int
        {
            return if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) mValue[0] else defaultValue
        }
    }
}