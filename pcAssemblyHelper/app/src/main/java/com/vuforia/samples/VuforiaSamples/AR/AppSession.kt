package com.vuforia.samples.VuforiaSamples.AR

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.os.AsyncTask
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
    lateinit var mActivity : Activity
    //var mSessionControl : SampleApplicationControl // 생성자로 편입

    // Flags
    var mStarted = false
    var mCameraRunning = false

    // The async tasks to initialize the Vuforia SDK:
    lateinit var mInitVuforiaTask : InitVuforiaTask
    lateinit var mLoadTrackerTask : LoadTrackerTask

    // An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    val mShutdownLock = Object()

    // Vuforia initialization flags:
    var mVuforiaFlags : Int = 0

    // Holds the camera configuration to use upon resuming
    var mCamera : Int = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT

    // 반드시 오버라이드 해야하는 함수
    override fun Vuforia_onUpdate(s : State?)
    {
        mSessionControl.onVuforiaUpdate(s)
    }

    // Initializes Vuforia and sets up preferences.
    fun initAR(activity: Activity, sco: Int)
    {
        var screenOrientation : Int = sco
        var vuforiaException: SampleApplicationException? = null
        mActivity = activity

        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        // Use an OrientationChangeListener here to capture all orientation changes.  Android
        // will not send an Activity.onConfigurationChanged() callback on a 180 degree rotation,
        // ie: Left Landscape to Right Landscape.  Vuforia needs to react to this change and the
        // SampleApplicationSession needs to update the Projection Matrix.

        val orientationEventListener = object : OrientationEventListener(mActivity)
        {
            var mLastRotation = -1

            override fun onOrientationChanged(i : Int)
            {
                val activityRotation : Int = mActivity.windowManager.defaultDisplay.rotation

                if (mLastRotation != activityRotation)
                {
                    mLastRotation = activityRotation
                }
            }
        }

        if (orientationEventListener.canDetectOrientation())
            orientationEventListener.enable()

        // Apply screen orientation
        mActivity.requestedOrientation = screenOrientation

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        mActivity.window.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mVuforiaFlags = INIT_FLAGS.GL_20

        // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!
        if (mInitVuforiaTask != null)
        {
            val logMessage = "Cannot initialize SDK twice"
            vuforiaException = SampleApplicationException(
                    SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED, logMessage)
            Log.e(LOGTAG, logMessage)
        }

        if (vuforiaException == null)
        {
            try {
                mInitVuforiaTask = InitVuforiaTask()
                mInitVuforiaTask.execute()
            } catch (e: Exception) {
                val logMessage = "Initializing Vuforia SDK failed"
                vuforiaException = SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage)
                Log.e(LOGTAG, logMessage)
            }

        }

        if (vuforiaException != null)
            mSessionControl.onInitARDone(vuforiaException)
    }

    // Starts Vuforia, initialize and starts the camera and start the trackers
    fun startAR(camera : Int) //throws SampleApplicationException
    {

    }
}