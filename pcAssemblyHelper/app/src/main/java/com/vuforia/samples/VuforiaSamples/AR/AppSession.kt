package com.vuforia.samples.VuforiaSamples.AR

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.os.AsyncTask
import android.os.AsyncTask.Status.FINISHED
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.OrientationEventListener
import android.view.WindowManager

import com.vuforia.CameraCalibration
import com.vuforia.CameraDevice
import com.vuforia.Device
import com.vuforia.INIT_ERRORCODE
import com.vuforia.INIT_FLAGS
import com.vuforia.Matrix44F
import com.vuforia.Renderer
import com.vuforia.State
import com.vuforia.Tool
import com.vuforia.Vec2I
import com.vuforia.VideoBackgroundConfig
import com.vuforia.VideoMode
import com.vuforia.Vuforia
import com.vuforia.Vuforia.UpdateCallbackInterface

// 나중에 삭제해야함
import com.vuforia.samples.SampleApplication.SampleApplicationControl
import com.vuforia.samples.SampleApplication.SampleApplicationException
import com.vuforia.samples.VuforiaSamples.R

class AppSession(var mSessionControl: SampleApplicationControl) : UpdateCallbackInterface
{
    // 로그 출력을 위한 변수
    // 디버깅 끝나면 다 지울 예정
    val LOGTAG = "SampleAppSession"

    // Reference to the current activity
    // 현재 액티비티를 가르킬 레퍼런스
    // 현재 액티비티는 ARmain 이다.
    lateinit var mActivity : Activity

    //var mSessionControl : SampleApplicationControl // 생성자로 편입

    // Flags
    var mStarted = false
    var mCameraRunning = false

    // The async tasks to initialize the Vuforia SDK:
    // 뷰포리아 SDK 초기화를 위한 비동기 작업을 위한 변수
    private var mInitVuforiaTask : InitVuforiaTask? = null
    private var mLoadTrackerTask : LoadTrackerTask? = null

    // An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    // 뷰포리아 동기화에 쓰이는 object 클래스의 인스턴스.
    // 어플리케이션 종료시 호출되는 onDestroy() 함수에서 사용된다.
    // 어플이 종료될때 뷰포리아와 데이터셋 간의 동기를 맞추는데 사용한다.
    val mShutdownLock = Object()

    // Vuforia initialization flags:
    // 뷰포리아 초기화에 사용할 변수
    var mVuforiaFlags : Int = 0

    // Holds the camera configuration to use upon resuming
    // AR 동작 중에 사용할 카메라 설정을 지정
    var mCamera : Int = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT

    // 반드시 오버라이드 해야하는 함수
    override fun Vuforia_onUpdate(s : State?)
    {
        mSessionControl.onVuforiaUpdate(s)
    }

    // Initializes Vuforia and sets up preferences.
    // 뷰포리아 초기화, 우선권 설정
    fun initAR(activity: Activity, sco: Int)
    {
        var screenOrientation : Int = sco
        var vuforiaException: SampleApplicationException? = null
        mActivity = activity

        // 화면 방향 설정
        // sdk 버전과 안드로이드 버전에 따라 변경
        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        // Use an OrientationChangeListener here to capture all orientation changes.  Android
        // will not send an Activity.onConfigurationChanged() callback on a 180 degree rotation,
        // ie: Left Landscape to Right Landscape.  Vuforia needs to react to this change and the
        // SampleApplicationSession needs to update the Projection Matrix.

        // 화면 방향 전환을 위한 Listener
        val orientationEventListener = object : OrientationEventListener(mActivity)
        {
            var mLastRotation = -1

            // 화면 방향이 바꼈을때 호출되는 함수
            override fun onOrientationChanged(i : Int)
            {
                val activityRotation : Int = mActivity.windowManager.defaultDisplay.rotation

                if (mLastRotation != activityRotation)
                {
                    mLastRotation = activityRotation
                }
            }
        }

        // 화면 회전 기능이 켜져있다면
        // 방향 전환 Listener 사용 가능
        if (orientationEventListener.canDetectOrientation())
            orientationEventListener.enable()

        // Apply screen orientation
        // 화면 방향을 액티비티에 적용
        mActivity.requestedOrientation = screenOrientation

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        // AR이 켜져있는 동안 화면이 계속 켜져있도록 설정
        mActivity.window.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mVuforiaFlags = INIT_FLAGS.GL_20

        // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!

        // 뷰포리아 sdk 초기화가 실패했으면
        // 아래 로그 출력
        if (mInitVuforiaTask != null)
        {
            val logMessage = "Cannot initialize SDK twice"
            vuforiaException = SampleApplicationException(
                    SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED, logMessage)
            Log.e(LOGTAG, logMessage)
        }

        // 뷰포리아 오류 발생시 아래 로그 출력
        if (vuforiaException == null)
        {
            try {
                mInitVuforiaTask = InitVuforiaTask()
                mInitVuforiaTask!!.execute()
            } catch (e: Exception) {
                val logMessage = "Initializing Vuforia SDK failed"
                vuforiaException = SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage)
                Log.e(LOGTAG, logMessage)
            }

        }

        // 추후 SampleApplicationControl 클래스 재설계 후에
        // 다시 정리
        if (vuforiaException != null)
            mSessionControl.onInitARDone(vuforiaException)
    }

    // Starts Vuforia, initialize and starts the camera and start the trackers
    // AR 시작시 호출되는 함수
    // 뷰포리아를 실행한다.
    // 카메라를 초기화하고 실행한다. 그리고 트래커를 실행한다.
    @Throws(SampleApplicationException::class)
    fun startAR(camera : Int) //throws SampleApplicationException
    {
        var error: String

        // 아래 오류 메세지는 삭제할 예정
        if (mCameraRunning)
        {
            error = "Camera already running, unable to open again"
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        mCamera = camera

        // 카메라가 초기화되지 않았으면
        // 오류 메세지 출력
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        // 카메라의 비디오모드로 설정되지 않는다면
        // 오류 메세지 출력
        if (!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode"
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        // 카메라가 실행되지 않는다면
        // 오류 메세지 출력
        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        // 트래커 실행
        mSessionControl.doStartTrackers()

        // 카메라 동작중이라고 표시
        mCameraRunning = true

        // 카메라를 포커스모드로 설정
        // 실패시 포커스모드 옵션을 달리해서 포커스모드로 설정
        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL)
        }
    }

    // Stops any ongoing initialization, stops Vuforia
    // AR 중지시 호출되는 함수
    // 뷰포리아를 중지시킨다.
    @Throws(SampleApplicationException::class)
    fun stopAR()
    {
        /*
        // 자바의 InitVuforiaTask.Status 가 제대로 안나와서 빨간줄이 뜬다.

        // Cancel potentially running tasks
        // 뷰포리아의 동작이 끝났는지를 체크
        // 끝나지 않았으면 종료
        if (mInitVuforiaTask != null && mInitVuforiaTask!!.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            mInitVuforiaTask!!.cancel(true)
            mInitVuforiaTask = null
        }

        if (mLoadTrackerTask != null && mLoadTrackerTask!!.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask!!.cancel(true)
            mLoadTrackerTask = null
        }
        */

        mInitVuforiaTask = null
        mLoadTrackerTask = null

        mStarted = false

        // 카메라를 중지시키는 함수 호출
        stopCamera()

        // Ensure that all asynchronous operations to initialize Vuforia
        // and loading the tracker datasets do not overlap:
        // 동기화 작업
        // 실행중일때 다른 스레드에서 접근 방지
        synchronized(mShutdownLock)
        {
            val unloadTrackersResult: Boolean
            val deinitTrackersResult: Boolean

            // Destroy the tracking data set:
            // 트래킹 데이터셋을 삭제
            unloadTrackersResult = mSessionControl.doUnloadTrackersData()

            // Deinitialize the trackers:
            // 트래커 자원 할당 해제
            deinitTrackersResult = mSessionControl.doDeinitTrackers()

            // Deinitialize Vuforia SDK:
            // 뷰포리아 sdk 자원 할당 해제
            Vuforia.deinit()

            // 트래킹 데이터셋 삭제 실패시
            // 오류 메세지 출력
            if (!unloadTrackersResult)
                throw SampleApplicationException(
                        SampleApplicationException.UNLOADING_TRACKERS_FAILURE,
                        "Failed to unload trackers\' data")

            // 트래커 자원 할당 해제 실패시
            // 오류 메세지 출력
            if (!deinitTrackersResult)
                throw SampleApplicationException(
                        SampleApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
                        "Failed to deinitialize trackers")

        }
    }

    // Resumes Vuforia, restarts the trackers and the camera
    // 처음, 중지되었다가 다시 AR이 실행되었을때 호출하는 함수
    // 트래커와 카메라를 다시 실행한다.
    @Throws(SampleApplicationException::class)
    fun resumeAR()
    {
        // Vuforia-specific resume operation
        // 뷰포리아 resume 함수 호출
        Vuforia.onResume()

        // 이미 AR이 시작되었다면 mStarted는 true
        // 처음 or 중지되었다가 다시 실행되면 mStarted는 false
        if (mStarted)
        {
            startAR(mCamera)
        }
    }

    // Pauses Vuforia and stops the camera
    // 뷰포리아와 카메라를 멈추는 함수
    @Throws(SampleApplicationException::class)
    fun pauseAR()
    {
        // AR이 실행되고있다면 카메라 중지
        if (mStarted)
        {
            stopCamera()
        }

        // 뷰포리아 pause 함수 호출
        Vuforia.onPause()
    }

    // Manages the configuration changes
    // 설정 변경 관리
    fun onConfigurationChanged()
    {
        // 현재 디바이스를 인스턴스로 리턴받아 설정 변경
        Device.getInstance().setConfigurationChanged()
    }

    // Methods to be called to handle lifecycle
    // 생명주기 관리를 위한 onResume 함수
    // 액티비티에서 오버라이드한 onResume 함수가 아니라
    // 뷰포리아 자체적으로 생명주기를 맞추기 위해서 뷰포리아.onResume 함수를 호출
    fun onResume()
    {
        Vuforia.onResume()
    }

    // 위의 onResume 함수와 동일
    fun onPause()
    {
        Vuforia.onPause()
    }

    // 카메라마다 해상도가 다르기 때문에 맞춰준다.
    fun onSurfaceChanged(width: Int, height: Int)
    {
        Vuforia.onSurfaceChanged(width, height)
    }

    // 카메라를 생성(오픈)한다.
    // 오픈된 카메라를 통해 투영시킨다.
    fun onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated()
    }

    // An async task to initialize Vuforia asynchronously.
    // 뷰포리아 초기화를 비동기적으로 하기 위한 작업
    // AppSession 클래스의 멤버변수를 이용하기 편리하게 inner 클래스로 선언
    private inner class InitVuforiaTask : AsyncTask<Void, Int, Boolean>()
    {
        // Initialize with invalid value:
        // 초기값으로 진행 안되고있다는 의미로 -1 대입
        var mProgressValue : Int = -1

        // 반드시 오버라이드 해야하는 함수
        // 뷰포리아에 파라미터를 넘겨주고 초기화하는 함수다.
        // 초기화 성공여부가 리턴된다.
        protected override fun doInBackground(vararg params: Void): Boolean
        {
            // Prevent the onDestroy() method to overlap with initialization:
            // 초기화하는 도중에 액티비티가 종료되지 않게 방지한다.
            // 액티비티 종료시 onDestroy 함수가 호출되므로 이를 방지하기 위해
            // 다른 스레드가 작동되지 못하도록 락을 건다.
            synchronized(mShutdownLock)
            {
                // 뷰포리아에 액티비티와 플래그, 라이센스키를 셋팅한다.
                // 해당 라이센스키는 '박재한' 계정으로 발급받은 키이다.
                Vuforia.setInitParameters(mActivity, mVuforiaFlags, "AW0IwM3/////AAAAGSFlz9euJEN2tDRjCc5hrsUOLCOEYERKT8ECJEeHssicOqSFVF7g+lMBFQb9eqiRKnFZYt+lNhyf+x1FMd9k0SL5d6/+Xm4HiAKqzIVPySe4BAfARZhCVmroVqzmgUeUaVoZOh81/Gs7GbsW0epxzWPkGU8wFJPxMC/vZ69ziB8a7jaqqKRmjORMnThV7QmiPVaBAerHjls73RQ30cFEFeAvWnoJiuCERHHiYgjKNRUBp+pyN9CcvsSGWD1h2mDEyPM+ckWWRZ9Rtob7RabN3YGlOHj7eFXYOSvbmXu2MhSwKrvPZC0bJ0+9VCnOyA3uQgWy3q6cKsMCLgzYOUe1jW1pfTcU+2hJ9CH9cd2GnWd9")

                // 뷰포리아 초기화 작업을 진행한다.
                // 초기화가 완전히 (100%) 완료되었을때까지 반복한다.
                do {
                    // Vuforia.init() blocks until an initialization step is
                    // complete, then it proceeds to the next step and reports
                    // progress in percents (0 ... 100%).
                    // If Vuforia.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.

                    // 뷰포리아를 초기화하고 성공여부를 리턴받아 저장한다.
                    mProgressValue = Vuforia.init()

                    // Publish the progress value:
                    // 초기화 진행상태를 publish 하는건데 반복문 밖에 쓰는게 나을듯
                    publishProgress(mProgressValue)

                    // We check whether the task has been canceled in the
                    // meantime (by calling AsyncTask.cancel(true)).
                    // and bail out if it has, thus stopping this thread.
                    // This is necessary as the AsyncTask will run to completion
                    // regardless of the status of the component that
                    // started is.
                } while (!isCancelled && mProgressValue >= 0
                        && mProgressValue < 100)

                return mProgressValue > 0
            }
        }

        // 스플래쉬 이미지나 progress 바를 업데이트하는 함수
        protected fun onProgressUpdate(vararg values: Int)
        {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }

        // 초기화 작업이 끝나고나서 다음 단계로 보내는 함수
        override fun onPostExecute(result: Boolean?)
        {
            // Done initializing Vuforia, proceed to next application
            // initialization status:

            var vuforiaException: SampleApplicationException? = null

            // 다음 단계로 무사히 넘어갔을 경우
            if (result!!)
            {
                // 디버깅 편의를 위해 로그 출력
                Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia " + "initialization successful")

                // 트래커 초기화하고 성공여부 리턴
                val initTrackersResult: Boolean = mSessionControl.doInitTrackers()

                // 트래커 초기화 성공시
                if (initTrackersResult)
                {
                    try {
                        mLoadTrackerTask = LoadTrackerTask()
                        mLoadTrackerTask!!.execute()
                    } catch (e: Exception) {
                        val logMessage = "Loading tracking data set failed"
                        vuforiaException = SampleApplicationException(
                                SampleApplicationException.LOADING_TRACKERS_FAILURE,
                                logMessage)
                        Log.e(LOGTAG, logMessage)
                        mSessionControl.onInitARDone(vuforiaException)
                    }

                }
                // 트래커 초기화 실패시 로그 출력
                else
                {
                    vuforiaException = SampleApplicationException(
                            SampleApplicationException.TRACKERS_INITIALIZATION_FAILURE,
                            "Failed to initialize trackers")
                    mSessionControl.onInitARDone(vuforiaException)
                }
            }
            // 뷰포리아 초기화 끝나고 다음 단계로 넘어가지 못했을 경우
            // 에러 메세지 출력
            else
            {
                val logMessage: String

                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                logMessage = getInitializationErrorString(mProgressValue)

                // Log error:
                Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage
                        + " Exiting.")

                // Send Vuforia Exception to the application and call initDone
                // to stop initialization process
                vuforiaException = SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage)
                mSessionControl.onInitARDone(vuforiaException)
            }
        }
    }

    // An async task to load the tracker data asynchronously.
    // 트래커 데이터를 비동기적으로 로드하는 작업을 위한 클래스
    // 멤버 변수 이용의 편의를 위해 inner 클래스로 선언
    private inner class LoadTrackerTask : AsyncTask<Void, Int, Boolean>()
    {
        // 반드시 오버라이드 해야하는 함수
        // 트래커 데이터를 로드하는 함수
        protected override fun doInBackground(vararg params: Void?): Boolean
        {
            // Prevent the onDestroy() method to overlap:
            // 트래커를 로드하는 도중에 액티비티 종료로 인한
            // onDestory 함수가 호출되지 못하도록 스레드 제어
            synchronized(mShutdownLock)
            {
                // Load the tracker data set:
                return mSessionControl.doLoadTrackersData()
            }
        }

        // 트래커 로드하고 나서 다음 작업을 실행하는 함수
        protected override fun onPostExecute(result: Boolean?)
        {
            var vuforiaException: SampleApplicationException? = null

            // 디버깅 편의를 위해 로그 출력
            Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution "
                    + if (result!!) "successful" else "failed")

            // 트래커 로드에 실패했을 경우
            // 오류 메세지 출력
            if ((!result)!!)
            {
                val logMessage = "Failed to load tracker data."
                // Error loading dataset
                Log.e(LOGTAG, logMessage)
                vuforiaException = SampleApplicationException(
                        SampleApplicationException.LOADING_TRACKERS_FAILURE,
                        logMessage)
            }
            // 트래커 로드에 성공하고 다음 단계로 넘어가는 경우
            else
            {
                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector:
                //
                // NOTE: This is only a hint. There is no guarantee that the
                // garbage collector will actually be run.

                // 자바의 가비지콜렉터를 호출
                System.gc()

                Vuforia.registerCallback(this@AppSession)

                mStarted = true
            }

            // Done loading the tracker, update application status, send the
            // exception to check errors
            // 트래커 로딩이 끝나고 예외 체크를 한다.
            mSessionControl.onInitARDone(vuforiaException)
        }
    }

    // Returns the error message for each error code
    // AR 초기화 중에서 일어나는 에러 메세지를 리턴해주는 함수
    private fun getInitializationErrorString(code: Int): String
    {
        if (code == INIT_ERRORCODE.INIT_DEVICE_NOT_SUPPORTED)
            return mActivity.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED)
        if (code == INIT_ERRORCODE.INIT_NO_CAMERA_ACCESS)
            return mActivity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_MISSING_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_INVALID_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_CANCELED_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY)

        return if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
            mActivity.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
        else {
            mActivity.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR)
        }
    }

    // 카메라를 중지시키는 함수
    fun stopCamera()
    {
        // 카메라가 작동중이면
        // 트래커를 중지시키고 디바이스에 중지 및 기록 삭제시킨다.
        if (mCameraRunning)
        {
            mSessionControl.doStopTrackers()
            mCameraRunning = false
            CameraDevice.getInstance().stop()
            CameraDevice.getInstance().deinit()
        }
    }

    // Returns true if Vuforia is initialized, the trackers started and the
    // tracker data loaded
    // AR이 실행되고있는지 리턴해주는 함수
    // mStarted는 뷰포리아 초기화, 트래커 실행, 트래커 데이터 로드가 모두
    // 완료되어야 true
    private fun isARRunning(): Boolean
    {
        return mStarted
    }
}
