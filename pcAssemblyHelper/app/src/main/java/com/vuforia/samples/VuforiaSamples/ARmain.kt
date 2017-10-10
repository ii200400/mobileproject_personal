package com.vuforia.samples.VuforiaSamples

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.support.v7.app.AppCompatActivity
import android.view.GestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import android.widget.RelativeLayout
import android.widget.CheckBox
import android.widget.Switch
import java.util.*

import com.vuforia.CameraDevice.FOCUS_MODE
import com.vuforia.CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO
import com.vuforia.CameraDevice
import com.vuforia.State

// 나중에 지워야함
import com.vuforia.samples.SampleApplication.SampleApplicationControl
import com.vuforia.samples.SampleApplication.SampleApplicationSession
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler
import com.vuforia.samples.SampleApplication.utils.Texture
import com.vuforia.samples.SampleApplication.SampleApplicationException
import com.vuforia.samples.SampleApplication.utils.SampleApplicationGLView
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenu


// AR 구현할 클래스
class ARmain : AppCompatActivity(), SampleApplicationControl
{
    lateinit var vuforiaAppSession : SampleApplicationSession

    lateinit var mGestureDetector : GestureDetector

    // 마커를 인식하면 띄울 텍스쳐를 담을 벡터
    var mTextures : Vector<Texture>? = Vector<Texture>()

    // 특정 기기에서의 동작을 위해 판별하는 변수
    var mIsDroidDevice : Boolean = false

    // openGL을 사용하기 위한 인스턴스
    lateinit var mGlView: SampleApplicationGLView

    lateinit var mFlashOptionView : View
    var mFlash : Boolean = false

    var mSampleAppMenu: SampleAppMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_armain)

        // SampleApplicationControl을 implement해야지 빨간줄 사라진다.
        // 추후 재설계 예정
        vuforiaAppSession = SampleApplicationSession(this)

        // AR에 사용할 카메라용 레이아웃 설정
        var mUILayout = View.inflate(this, R.layout.camera_overlay,null) as RelativeLayout

        // 레이아웃의 visibility on
        mUILayout.setVisibility(View.VISIBLE)

        // 여기부터 카메라를 띄우기 전 로딩화면 애니메이션 구현

        // 레이아웃의 배경색을 검정색으로 설정
        mUILayout.setBackgroundColor(Color.BLACK)

        var loadingDialogHandler = LoadingDialogHandler(this)

        // Gets a reference to the loading dialog
        // 카메라 띄우는데 로딩 상태를 띄우기 위한 참조
        // ProgressBar 뷰를 이용
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator)

        // Shows the loading indicator at start
        // 프로그레스바에 시작한다는 메세지 전달
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG)

        // Adds the inflated layout to the view
        // 카메라 레이아웃에 파라미터 지정
        addContentView(mUILayout,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT))

        // 여기까지가 로딩 애니메이션 부분


        // 마커로 사용할 xml 파일을 담을 배열
        var mDatasetStrings = arrayListOf<String>()

        // 사용할 마커를 배열에 추가
        // 추후 마커를 새로 만들면 이곳에다가 추가하면 된다.
        mDatasetStrings.add("StonesAndChips.xml")
        mDatasetStrings.add("Tarmac.xml")

        // 추후 재설계해야할 initAR 함수
        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        // 추후 재설계해야할 클래스 GestureListener
        mGestureDetector = GestureDetector(this, GestureListener())

        // Load any sample specific textures:
        // 마커를 인식하면 띄울 텍스쳐를 저장할 벡터
        //var mTextures : Vector<Texture> = Vector<Texture>()

        // 마커 인식하면 띄울 텍스쳐 추가
        // 추후 띄울 텍스쳐를 만들고 여기에 추가할 예정
        mTextures!!.add(Texture.loadTextureFromApk("TextureTeapotBrass.png", assets))
        mTextures!!.add(Texture.loadTextureFromApk("TextureTeapotBlue.png", assets))
        mTextures!!.add(Texture.loadTextureFromApk("TextureTeapotRed.png", assets))
        mTextures!!.add(Texture.loadTextureFromApk("ImageTargets/Buildings.jpeg", assets))

        // 안드로이드 기기 모델을 구분해주는 boolean 타입 변수
        // 특정 기기에서 작동하도록 설정해줄 필요가 있어서 사용하는 변수
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid")
    }

    // Called when the activity will start interacting with the user.
    // 액티비티가 처음으로, 또는 중지되었다가 다시 시작되었을때 호출되는 함수
    override fun onResume() {
        Log.d("ARmain", "onResume") // 디버깅의 편의성을 위해 로그 출력
        super.onResume()

        // This is needed for some Droid devices to force portrait
        // 특정 기기에서 작동할 수 있도록 설정
        if (mIsDroidDevice)
        {
            // 액티비티의 화면 방향을 설정
            // 카메라 화면에서 작동하는 AR이기 때문에 설정
            // 2번 설정하면 덮어씌워질텐데 이렇게 하는 이유는 아마도
            // 특정 기기에서는 이중 하나만 적용되기 때문이라고 생각된다.
            // SCREEN_ORIENTATION_LANDSCAPE 는 풍경화방향 (디스플레이가 높이보다 넓이가 더 넓은 상태)
            // SCREEN_ORIENTATION_PORTRAIT 는 초상화방향 (디스플레이가 넓이보다 높이가 더 넓은 상태)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // AppSession 클래스의 resumeAR() 함수 호출
        // 디버깅 편의를 위해 에러 발생시 로그 출력
        try {
            vuforiaAppSession.resumeAR()
        } catch (e: SampleApplicationException) {
            Log.e("ARmain", e.string)
        }

        // Resume the GL view:
        // openGL 을 사용하기 위해
        // 처음 실행했을때 visible로 설정
        // 액티비티가 중지되었다가 다시 실행되었을 때는 실행 x
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE)
            mGlView.onResume()
        }

    }

    // Callback for configuration changes the activity handles itself
    // 설정 변했을때 호출되는 함수
    // AppSession 클래스에 정의된 onConfigurationChanged() 함수도 호출한다.
    override fun onConfigurationChanged(config: Configuration)
    {
        // 디버깅 편의를 위해 로그 출력
        Log.d("ARmain", "onConfigurationChanged")

        super.onConfigurationChanged(config)

        vuforiaAppSession.onConfigurationChanged()
    }

    // Called when the system is about to start resuming a previous activity.
    // 액티비티가 중지될때 호출되는 함수
    // 상태 저장 등을 한다.
    override fun onPause()
    {
        // 디버깅 편의를 위해 로그 출력
        Log.d("ARmain", "onPause")

        super.onPause()

        // openGL이 사용되고 있다면
        // 보이지 않게 invisible로 설정
        if (mGlView != null)
        {
            mGlView.visibility = View.INVISIBLE
            mGlView.onPause()
        }

        // Turn off the flash
        // 플래쉬가 켜져있다면 끄기
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            // sdk 버전과 안드로이드 버전에 따라 다르게 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                (mFlashOptionView as Switch).isChecked = false
            }
            else
            {
                (mFlashOptionView as CheckBox).isChecked = false
            }
        }

        // AR 일시정지 함수 호출
        // 디버깅 편의를 위해 try-catch
        try {
            vuforiaAppSession.pauseAR()
        } catch (e: SampleApplicationException) {
            Log.e("ARmain", e.string)
        }
    }

    // The final call you receive before your activity is destroyed.
    // 액티비티가 종료되기 직전 호출되는 함수
    override fun onDestroy()
    {
        // 디버깅 편의를 위해 로그 출력
        Log.d("ARmain", "onDestroy")

        super.onDestroy()

        // AR 중지(종료) 함수 호출
        // 디버깅 편의를 위해 로그 출력
        try {
            vuforiaAppSession.stopAR()
        } catch (e: SampleApplicationException) {
            Log.e("ARmain", e.string)
        }

        // Unload texture:
        // openGL 을 이용한 텍스쳐 사용 종료로 인한 초기화
        mTextures!!.clear()
        mTextures = null

        // 가비지 콜렉터 요청
        // 메모리 사용이 큰 동작이라 추가된 코드
        System.gc()
    }

    // 터치 동작시 이벤트 처리 함수
    // AR 화면에서는 터치 동작이 필요없기 때문에 해당 함수는 간단히 구현
    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        // Process the Gestures
        if (mSampleAppMenu != null && mSampleAppMenu!!.processEvent(event))
            return true

        return mGestureDetector.onTouchEvent(event);
    }


    // 아래 함수들은 SampleApplicationControl 클래스 구현
    // SampleApplicationControl 클래스 재설계 후 구현할 예정

    override fun doDeinitTrackers(): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doInitTrackers(): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doLoadTrackersData(): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doStartTrackers(): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doStopTrackers(): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doUnloadTrackersData(): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInitARDone(e: SampleApplicationException?)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onVuforiaUpdate(state: State?)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// 추후 재설계해야할 클래스
// Process Single Tap event to trigger autofocus
private class GestureListener : GestureDetector.SimpleOnGestureListener() {
    // Used to set autofocus one second after a manual focus is triggered
    private val autofocusHandler = Handler()

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        // Generates a Handler to trigger autofocus
        // after 1 second
        autofocusHandler.postDelayed(Runnable {
            val result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)

            if (!result)
                Log.e("SingleTapUp", "Unable to trigger focus")
        }, 1000L)

        return true
    }
}
