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
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        if (!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode"
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        mSessionControl.doStartTrackers()

        mCameraRunning = true

        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL)
        }
    }

    // Stops any ongoing initialization, stops Vuforia
    @Throws(SampleApplicationException::class)
    fun stopAR()
    {
        // Cancel potentially running tasks
        if (mInitVuforiaTask != null && mInitVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            mInitVuforiaTask.cancel(true)
            mInitVuforiaTask = null
        }

        if (mLoadTrackerTask != null && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask.cancel(true)
            mLoadTrackerTask = null
        }

        mInitVuforiaTask = null
        mLoadTrackerTask = null

        mStarted = false

        stopCamera()

        // Ensure that all asynchronous operations to initialize Vuforia
        // and loading the tracker datasets do not overlap:
        synchronized(mShutdownLock)
        {
            val unloadTrackersResult: Boolean
            val deinitTrackersResult: Boolean

            // Destroy the tracking data set:
            unloadTrackersResult = mSessionControl.doUnloadTrackersData()

            // Deinitialize the trackers:
            deinitTrackersResult = mSessionControl.doDeinitTrackers()

            // Deinitialize Vuforia SDK:
            Vuforia.deinit()

            if (!unloadTrackersResult)
                throw SampleApplicationException(
                        SampleApplicationException.UNLOADING_TRACKERS_FAILURE,
                        "Failed to unload trackers\' data")

            if (!deinitTrackersResult)
                throw SampleApplicationException(
                        SampleApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
                        "Failed to deinitialize trackers")

        }
    }

    // Resumes Vuforia, restarts the trackers and the camera
    @Throws(SampleApplicationException::class)
    fun resumeAR()
    {
        // Vuforia-specific resume operation
        Vuforia.onResume()

        if (mStarted)
        {
            startAR(mCamera)
        }
    }

    // Pauses Vuforia and stops the camera
    @Throws(SampleApplicationException::class)
    fun pauseAR()
    {
        if (mStarted)
        {
            stopCamera()
        }

        Vuforia.onPause()
    }

    // Manages the configuration changes
    fun onConfigurationChanged()
    {
        Device.getInstance().setConfigurationChanged()
    }

    // Methods to be called to handle lifecycle
    fun onResume()
    {
        Vuforia.onResume()
    }

    fun onPause()
    {
        Vuforia.onPause()
    }

    fun onSurfaceChanged(width: Int, height: Int)
    {
        Vuforia.onSurfaceChanged(width, height)
    }

    fun onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated()
    }
}

// An async task to initialize Vuforia asynchronously.
class InitVuforiaTask : AsyncTask<Void, Int, Boolean>()
{
    // 반드시 오버라이드 해야하는 함수
    override fun doInBackground(vararg params: Void?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
